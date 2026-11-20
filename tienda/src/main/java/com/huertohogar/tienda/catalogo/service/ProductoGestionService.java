package com.huertohogar.tienda.catalogo.service;

import com.huertohogar.tienda.catalogo.dto.AjusteStockRequest;
import com.huertohogar.tienda.catalogo.dto.ItemCantidad;
import com.huertohogar.tienda.catalogo.dto.MovimientoResponse;
import com.huertohogar.tienda.catalogo.dto.ProductoRequest;
import com.huertohogar.tienda.catalogo.dto.ProductoResponse;
import com.huertohogar.tienda.catalogo.dto.ResumenInventarioResponse;
import com.huertohogar.tienda.catalogo.model.Categoria;
import com.huertohogar.tienda.catalogo.model.MovimientoStock;
import com.huertohogar.tienda.catalogo.model.ProductoCatalogo;
import com.huertohogar.tienda.catalogo.model.TipoMovimiento;
import com.huertohogar.tienda.catalogo.repository.CategoriaRepository;
import com.huertohogar.tienda.catalogo.repository.MovimientoStockRepository;
import com.huertohogar.tienda.catalogo.repository.ProductoCatalogoRepository;
import com.huertohogar.tienda.common.RecursoNoEncontradoException;
import com.huertohogar.tienda.common.ReglaNegocioException;
import com.huertohogar.tienda.inventario.model.Producto;
import com.huertohogar.tienda.inventario.repository.ProductoRepository;
import com.huertohogar.tienda.pedido.repository.DetallePedidoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Logica de negocio del inventario: CRUD de productos, control de stock con historial
 * y descuento de stock transaccional para las compras.
 * Usa el ProductoRepository original de inventario sin modificarlo.
 */
@Service
public class ProductoGestionService {

    private final ProductoRepository productoRepository;
    private final EntityManager entityManager;
    private final ProductoCatalogoRepository catalogoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MovimientoStockRepository movimientoRepository;
    private final DetallePedidoRepository detallePedidoRepository;

    public ProductoGestionService(ProductoRepository productoRepository,
                                  EntityManager entityManager,
                                  ProductoCatalogoRepository catalogoRepository,
                                  CategoriaRepository categoriaRepository,
                                  MovimientoStockRepository movimientoRepository,
                                  DetallePedidoRepository detallePedidoRepository) {
        this.productoRepository = productoRepository;
        this.entityManager = entityManager;
        this.catalogoRepository = catalogoRepository;
        this.categoriaRepository = categoriaRepository;
        this.movimientoRepository = movimientoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
    }

    // ===================== Consultas =====================

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(String categoria, String q, boolean soloDisponibles,
                                         boolean stockBajo, boolean incluirInactivos) {
        String cat = (categoria == null || categoria.isBlank()) ? null : categoria.trim().toLowerCase();
        String texto = (q == null || q.isBlank()) ? null : "%" + q.trim().toLowerCase() + "%";
        return catalogoRepository.buscar(cat, texto, soloDisponibles, stockBajo, incluirInactivos)
                .stream().map(ProductoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long productoId) {
        return ProductoResponse.desde(fichaPorProductoId(productoId));
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorCodigo(String codigo, boolean incluirInactivos) {
        ProductoCatalogo pc = fichaPorCodigo(codigo);
        if (!pc.isActivo() && !incluirInactivos) {
            throw new RecursoNoEncontradoException("No existe el producto " + codigo);
        }
        return ProductoResponse.desde(pc);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> alertasStockBajo() {
        return listar(null, null, false, true, false);
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponse> movimientos(Long productoId) {
        fichaPorProductoId(productoId);
        return movimientoRepository.findByProductoIdOrderByFechaDescIdDesc(productoId)
                .stream().map(MovimientoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public ResumenInventarioResponse resumen() {
        List<ProductoCatalogo> todos = catalogoRepository.findAll();
        long activos = 0, unidades = 0, valor = 0, bajo = 0, sinStock = 0;
        for (ProductoCatalogo pc : todos) {
            if (!pc.isActivo()) {
                continue;
            }
            Producto p = pc.getProducto();
            int stock = stockDe(p);
            activos++;
            unidades += stock;
            valor += Math.round((p.getPrecio() == null ? 0 : p.getPrecio()) * stock);
            if (stock <= pc.getStockMinimo()) {
                bajo++;
            }
            if (stock == 0) {
                sinStock++;
            }
        }
        return new ResumenInventarioResponse(todos.size(), activos, unidades, valor, bajo, sinStock,
                categoriaRepository.count());
    }

    // ===================== CRUD =====================

    @Transactional
    public ProductoResponse crear(ProductoRequest req, String usuario) {
        String codigo = req.codigo().trim().toUpperCase();
        if (catalogoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "Ya existe un producto con el código " + codigo,
                    Map.of("codigo", "Código repetido"));
        }
        Categoria categoria = categoriaPorSlug(req.categoria());

        Producto producto = productoRepository.save(
                new Producto(null, req.nombre().trim(), req.descripcion(), req.precio(), req.stock()));

        ProductoCatalogo pc = new ProductoCatalogo();
        pc.setCodigo(codigo);
        pc.setProducto(producto);
        aplicarFicha(pc, req, categoria);
        catalogoRepository.save(pc);

        if (req.stock() > 0) {
            registrar(producto, TipoMovimiento.ENTRADA, req.stock(), 0, req.stock(), "Stock inicial", null, usuario);
        }
        return ProductoResponse.desde(pc);
    }

    @Transactional
    public ProductoResponse actualizar(Long productoId, ProductoRequest req, String usuario) {
        ProductoCatalogo pc = fichaPorProductoId(productoId);
        String codigo = req.codigo().trim().toUpperCase();
        if (!pc.getCodigo().equalsIgnoreCase(codigo) && catalogoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "Ya existe un producto con el código " + codigo,
                    Map.of("codigo", "Código repetido"));
        }
        Categoria categoria = categoriaPorSlug(req.categoria());
        Producto producto = bloquear(productoId);
        int anterior = stockDe(producto);

        producto.setNombre(req.nombre().trim());
        producto.setDescripcion(req.descripcion());
        producto.setPrecio(req.precio());
        producto.setStock(req.stock());
        pc.setCodigo(codigo);
        aplicarFicha(pc, req, categoria);

        if (anterior != req.stock()) {
            registrar(producto, TipoMovimiento.AJUSTE, Math.abs(req.stock() - anterior), anterior, req.stock(),
                    "Edición del producto", null, usuario);
        }
        return ProductoResponse.desde(pc);
    }

    /**
     * Por defecto es una baja logica (activo = false): el producto deja de venderse pero
     * se conserva su historial. Con definitivo=true se borra (solo si nunca se vendio).
     */
    @Transactional
    public void eliminar(Long productoId, boolean definitivo) {
        ProductoCatalogo pc = fichaPorProductoId(productoId);
        if (!definitivo) {
            pc.setActivo(false);
            return;
        }
        if (detallePedidoRepository.existsByProductoId(productoId)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "El producto tiene pedidos asociados: no se puede borrar, solo desactivar");
        }
        movimientoRepository.eliminarDeProducto(productoId);
        catalogoRepository.delete(pc);
        productoRepository.deleteById(productoId);
    }

    // ===================== Stock =====================

    /** Fija el stock a un valor (conteo fisico desde el panel admin). */
    @Transactional
    public ProductoResponse actualizarStock(Long productoId, int nuevoStock, String motivo, String usuario) {
        ProductoCatalogo pc = fichaPorProductoId(productoId);
        Producto producto = bloquear(productoId);
        int anterior = stockDe(producto);
        producto.setStock(nuevoStock);
        registrar(producto, TipoMovimiento.AJUSTE, Math.abs(nuevoStock - anterior), anterior, nuevoStock,
                motivo == null || motivo.isBlank() ? "Ajuste desde administración" : motivo, null, usuario);
        return ProductoResponse.desde(pc);
    }

    /** Suma (ENTRADA) o resta (SALIDA) unidades. */
    @Transactional
    public ProductoResponse ajustar(Long productoId, AjusteStockRequest req, String usuario) {
        if (req.tipo() != TipoMovimiento.ENTRADA && req.tipo() != TipoMovimiento.SALIDA) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "El tipo debe ser ENTRADA o SALIDA",
                    Map.of("tipo", "Usa ENTRADA o SALIDA"));
        }
        ProductoCatalogo pc = fichaPorProductoId(productoId);
        Producto producto = bloquear(productoId);
        int anterior = stockDe(producto);
        int nuevo = req.tipo() == TipoMovimiento.ENTRADA ? anterior + req.cantidad() : anterior - req.cantidad();
        if (nuevo < 0) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "Stock insuficiente: hay " + anterior + " y se intenta sacar " + req.cantidad());
        }
        producto.setStock(nuevo);
        registrar(producto, req.tipo(), req.cantidad(), anterior, nuevo, req.motivo(), req.referencia(), usuario);
        return ProductoResponse.desde(pc);
    }

    /** Linea ya descontada: ficha del producto y cantidad vendida. */
    public record LineaDescontada(ProductoCatalogo ficha, int cantidad) {
    }

    /**
     * Valida y descuenta el stock de todas las lineas dentro de la transaccion actual.
     * Bloquea las filas en orden de id (evita interbloqueos) y no descuenta nada si
     * alguna linea no tiene stock suficiente. Lo usa tambien el checkout de pedidos.
     */
    @Transactional
    public List<LineaDescontada> descontarConBloqueo(List<ItemCantidad> items, String referencia, String usuario) {
        // Une lineas repetidas del mismo producto
        Map<String, Integer> cantidades = new LinkedHashMap<>();
        for (ItemCantidad item : items) {
            cantidades.merge(item.codigo().trim().toUpperCase(), item.cantidad(), Integer::sum);
        }

        List<ProductoCatalogo> fichas = new ArrayList<>();
        for (String codigo : cantidades.keySet()) {
            ProductoCatalogo pc = fichaPorCodigo(codigo);
            if (!pc.isActivo()) {
                throw new ReglaNegocioException(HttpStatus.CONFLICT, pc.getProducto().getNombre() + " ya no está disponible");
            }
            fichas.add(pc);
        }
        fichas.sort(Comparator.comparing(pc -> pc.getProducto().getId()));

        // 1) Bloquear y validar todo antes de modificar
        Map<String, String> sinStock = new LinkedHashMap<>();
        for (ProductoCatalogo pc : fichas) {
            Producto p = bloquear(pc.getProducto().getId());
            int pedido = cantidades.get(pc.getCodigo());
            if (stockDe(p) < pedido) {
                sinStock.put(pc.getCodigo(), "Solo hay " + stockDe(p) + " unidades de " + p.getNombre());
            }
        }
        if (!sinStock.isEmpty()) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "Stock insuficiente: " + String.join("; ", sinStock.values()), sinStock);
        }

        // 2) Descontar y registrar el movimiento
        List<LineaDescontada> resultado = new ArrayList<>();
        for (ProductoCatalogo pc : fichas) {
            Producto p = pc.getProducto();
            int cantidad = cantidades.get(pc.getCodigo());
            int anterior = stockDe(p);
            p.setStock(anterior - cantidad);
            registrar(p, TipoMovimiento.VENTA, cantidad, anterior, anterior - cantidad, "Venta", referencia, usuario);
            resultado.add(new LineaDescontada(pc, cantidad));
        }
        return resultado;
    }

    /** Devuelve stock (pedido cancelado). */
    @Transactional
    public void reponer(Long productoId, int cantidad, String referencia, String usuario) {
        Producto p = bloquear(productoId);
        int anterior = stockDe(p);
        p.setStock(anterior + cantidad);
        registrar(p, TipoMovimiento.DEVOLUCION, cantidad, anterior, anterior + cantidad,
                "Pedido cancelado", referencia, usuario);
    }

    // ===================== Apoyo =====================

    public ProductoCatalogo fichaPorCodigo(String codigo) {
        return catalogoRepository.findByCodigoIgnoreCase(codigo.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el producto " + codigo));
    }

    private ProductoCatalogo fichaPorProductoId(Long productoId) {
        return catalogoRepository.findByProductoId(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto " + productoId + " no encontrado"));
    }

    /**
     * Relee el producto desde la BD con bloqueo de fila (SELECT ... FOR UPDATE):
     * otra compra simultanea espera a que esta termine, asi nunca se vende stock que no existe.
     */
    private Producto bloquear(Long productoId) {
        Producto p = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto " + productoId + " no encontrado"));
        entityManager.refresh(p, LockModeType.PESSIMISTIC_WRITE);
        return p;
    }

    private Categoria categoriaPorSlug(String slug) {
        return categoriaRepository.findBySlugIgnoreCase(slug.trim())
                .orElseThrow(() -> new ReglaNegocioException(HttpStatus.BAD_REQUEST,
                        "La categoría '" + slug + "' no existe", Map.of("categoria", "Categoría no existe")));
    }

    private void aplicarFicha(ProductoCatalogo pc, ProductoRequest req, Categoria categoria) {
        pc.setCategoria(categoria);
        pc.setStockMinimo(req.stockMinimo() == null ? 10 : req.stockMinimo());
        pc.setUnidad(req.unidad());
        pc.setOrigen(req.origen());
        pc.setEmoji(req.emoji());
        pc.setPracticasSostenibles(req.practicasSostenibles());
        pc.setImagenUrl(req.imagenUrl());
        pc.setActivo(req.activo() == null || req.activo());
    }

    private static int stockDe(Producto p) {
        return p.getStock() == null ? 0 : p.getStock();
    }

    private void registrar(Producto p, TipoMovimiento tipo, int cantidad, int anterior, int nuevo,
                           String motivo, String referencia, String usuario) {
        MovimientoStock m = new MovimientoStock();
        m.setProducto(p);
        m.setTipo(tipo);
        m.setCantidad(cantidad);
        m.setStockAnterior(anterior);
        m.setStockNuevo(nuevo);
        m.setMotivo(motivo);
        m.setReferencia(referencia);
        m.setUsuario(usuario);
        movimientoRepository.save(m);
    }
}
