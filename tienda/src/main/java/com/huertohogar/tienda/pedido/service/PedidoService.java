package com.huertohogar.tienda.pedido.service;

import com.huertohogar.tienda.catalogo.dto.ItemCantidad;
import com.huertohogar.tienda.catalogo.dto.ProductoResponse;
import com.huertohogar.tienda.catalogo.model.ProductoCatalogo;
import com.huertohogar.tienda.catalogo.service.ProductoGestionService;
import com.huertohogar.tienda.catalogo.service.ProductoGestionService.LineaDescontada;
import com.huertohogar.tienda.common.Precios;
import com.huertohogar.tienda.common.RecursoNoEncontradoException;
import com.huertohogar.tienda.common.ReglaNegocioException;
import com.huertohogar.tienda.inventario.model.Producto;
import com.huertohogar.tienda.pedido.dto.CheckoutRequest;
import com.huertohogar.tienda.pedido.dto.CheckoutResponse;
import com.huertohogar.tienda.pedido.dto.PedidoResponse;
import com.huertohogar.tienda.pedido.model.DetallePedido;
import com.huertohogar.tienda.pedido.model.EstadoPedido;
import com.huertohogar.tienda.pedido.model.HistorialEstadoPedido;
import com.huertohogar.tienda.pedido.model.Pedido;
import com.huertohogar.tienda.pedido.repository.PedidoRepository;
import com.huertohogar.tienda.pedido.client.UsuariosClient.PerfilEntrega;
import com.huertohogar.tienda.security.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Procesamiento y seguimiento de pedidos:
 * checkout transaccional (crea boleta + descuenta stock, todo o nada),
 * historial del cliente, cambio de estados y cancelacion con devolucion de stock.
 */
@Service
public class PedidoService {

    private static final int DIAS_ENTREGA_POR_DEFECTO = 2;
    private static final int DIAS_ENTREGA_MAXIMO = 30;

    private final PedidoRepository pedidoRepository;
    private final ProductoGestionService productoService;

    public PedidoService(PedidoRepository pedidoRepository,
                         ProductoGestionService productoService) {
        this.pedidoRepository = pedidoRepository;
        this.productoService = productoService;
    }

    // ===================== Checkout =====================

    @Transactional
    public CheckoutResponse checkout(UsuarioAutenticado usuario, CheckoutRequest req, PerfilEntrega perfil) {
        List<ItemCantidad> lineas = req.items();
        if (lineas == null || lineas.isEmpty()) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "Tu carrito está vacío");
        }

        LocalDate fechaEntrega = validarFechaEntrega(req.fechaEntrega());

        // 1) Crear el pedido para obtener su numero de boleta
        Pedido pedido = new Pedido();
        pedido.setNumero("TMP-" + UUID.randomUUID());
        pedido.setUsuarioId(usuario.id());
        pedido.setCorreoCliente(usuario.correo());
        pedido.setNombreCliente(perfil != null && perfil.nombre() != null ? perfil.nombre() : usuario.nombre());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setFechaEntregaPreferida(fechaEntrega);
        pedido.setDireccionEntrega(primero(req.direccionEntrega(), perfil == null ? null : perfil.direccion()));
        pedido.setComuna(primero(req.comuna(), perfil == null ? null : perfil.comuna()));
        pedido.setRegion(primero(req.region(), perfil == null ? null : perfil.region()));
        pedido.setReferenciaCliente(req.referencia());
        pedidoRepository.saveAndFlush(pedido);
        pedido.setNumero("HH-" + (1000 + pedido.getId()));

        // 2) Descontar stock con bloqueo (si falta stock se lanza error y se deshace todo)
        List<LineaDescontada> vendidas = productoService.descontarConBloqueo(lineas, pedido.getNumero(), usuario.correo());

        // 3) Detalle de la boleta con precios del momento
        long subtotal = 0;
        long total = 0;
        for (LineaDescontada linea : vendidas) {
            ProductoCatalogo ficha = linea.ficha();
            Producto p = ficha.getProducto();
            long precioLista = Precios.precioFinal(p.getPrecio(), null);
            long precioFinal = Precios.precioFinal(p.getPrecio(), usuario.correo());

            DetallePedido d = new DetallePedido();
            d.setPedido(pedido);
            d.setProducto(p);
            d.setCodigo(ficha.getCodigo());
            d.setNombre(p.getNombre());
            d.setUnidad(ficha.getUnidad());
            d.setPrecioUnitario(precioLista);
            d.setPrecioFinal(precioFinal);
            d.setCantidad(linea.cantidad());
            d.setSubtotal(precioFinal * linea.cantidad());
            pedido.getDetalles().add(d);

            subtotal += precioLista * linea.cantidad();
            total += precioFinal * linea.cantidad();
        }
        long neto = Precios.neto(total);
        pedido.setSubtotal(subtotal);
        pedido.setDescuento(subtotal - total);
        pedido.setTotal(total);
        pedido.setNeto(neto);
        pedido.setIva(total - neto);
        agregarHistorial(pedido, EstadoPedido.PENDIENTE, "Pedido recibido", usuario.correo());

        List<ProductoResponse> productos = vendidas.stream().map(l -> ProductoResponse.desde(l.ficha())).toList();
        PedidoResponse boleta = PedidoResponse.desde(pedido);
        return new CheckoutResponse(boleta.numero(), boleta.total(), boleta, productos);
    }

    // ===================== Consultas =====================

    /** Historial de compras del usuario. Un ADMIN puede consultar el de otro correo. */
    @Transactional(readOnly = true)
    public List<PedidoResponse> misPedidos(UsuarioAutenticado usuario, String correo) {
        List<Pedido> pedidos = (correo != null && !correo.isBlank() && usuario.esAdmin()
                && !correo.equalsIgnoreCase(usuario.correo()))
                ? pedidoRepository.findByCorreoClienteIgnoreCaseOrderByFechaCreacionDesc(correo)
                : pedidoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.id());
        return pedidos.stream().map(PedidoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtener(String numero, UsuarioAutenticado usuario) {
        return PedidoResponse.desde(buscarVisible(numero, usuario));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> todos(EstadoPedido estado) {
        List<Pedido> pedidos = estado == null
                ? pedidoRepository.findAllByOrderByFechaCreacionDesc()
                : pedidoRepository.findByEstadoOrderByFechaCreacionDesc(estado);
        return pedidos.stream().map(PedidoResponse::desde).toList();
    }

    // ===================== Estados =====================

    /** ADMIN / VENDEDOR avanzan el pedido: PENDIENTE -> EN_PREPARACION -> EN_CAMINO -> ENTREGADO. */
    @Transactional
    public PedidoResponse cambiarEstado(String numero, EstadoPedido nuevo, String comentario, UsuarioAutenticado usuario) {
        Pedido pedido = buscar(numero);
        if (!pedido.getEstado().puedeCambiarA(nuevo)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "No se puede pasar de " + pedido.getEstado() + " a " + nuevo,
                    Map.of("estado", "Transición no permitida"));
        }
        if (nuevo == EstadoPedido.CANCELADO) {
            devolverStock(pedido, usuario.correo());
        }
        pedido.setEstado(nuevo);
        agregarHistorial(pedido, nuevo, comentario == null || comentario.isBlank() ? nuevo.getDescripcion() : comentario,
                usuario.correo());
        return PedidoResponse.desde(pedido);
    }

    /** El cliente puede cancelar mientras el pedido esta PENDIENTE; el personal tambien en preparacion. */
    @Transactional
    public PedidoResponse cancelar(String numero, UsuarioAutenticado usuario) {
        Pedido pedido = buscarVisible(numero, usuario);
        boolean permitido = pedido.getEstado() == EstadoPedido.PENDIENTE
                || (usuario.esPersonal() && pedido.getEstado() == EstadoPedido.EN_PREPARACION);
        if (!permitido) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "El pedido ya no se puede cancelar (estado " + pedido.getEstado() + ")");
        }
        devolverStock(pedido, usuario.correo());
        pedido.setEstado(EstadoPedido.CANCELADO);
        agregarHistorial(pedido, EstadoPedido.CANCELADO, "Cancelado por " + usuario.correo(), usuario.correo());
        return PedidoResponse.desde(pedido);
    }

    // ===================== Apoyo =====================

    private void devolverStock(Pedido pedido, String usuario) {
        for (DetallePedido d : pedido.getDetalles()) {
            if (d.getProducto() != null) {
                productoService.reponer(d.getProducto().getId(), d.getCantidad(), pedido.getNumero(), usuario);
            }
        }
    }

    private void agregarHistorial(Pedido pedido, EstadoPedido estado, String comentario, String usuario) {
        HistorialEstadoPedido h = new HistorialEstadoPedido();
        h.setPedido(pedido);
        h.setEstado(estado);
        h.setComentario(comentario);
        h.setUsuario(usuario);
        h.setFecha(LocalDateTime.now());
        pedido.getHistorial().add(h);
    }

    private LocalDate validarFechaEntrega(LocalDate fecha) {
        LocalDate hoy = LocalDate.now();
        if (fecha == null) {
            return hoy.plusDays(DIAS_ENTREGA_POR_DEFECTO);
        }
        if (!fecha.isAfter(hoy) || fecha.isAfter(hoy.plusDays(DIAS_ENTREGA_MAXIMO))) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST,
                    "La fecha de entrega debe ser entre mañana y " + DIAS_ENTREGA_MAXIMO + " días más",
                    Map.of("fechaEntrega", "Fecha fuera de rango"));
        }
        return fecha;
    }

    private Pedido buscar(String numero) {
        return pedidoRepository.findByNumero(numero.trim().toUpperCase())
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido " + numero + " no encontrado"));
    }

    /** Un cliente solo ve sus pedidos (a los demas responde 404 para no revelar que existen). */
    private Pedido buscarVisible(String numero, UsuarioAutenticado usuario) {
        Pedido pedido = buscar(numero);
        if (!usuario.esPersonal() && !pedido.getUsuarioId().equals(usuario.id())) {
            throw new RecursoNoEncontradoException("Pedido " + numero + " no encontrado");
        }
        return pedido;
    }

    private static String primero(String a, String b) {
        return (a != null && !a.isBlank()) ? a.trim() : b;
    }
}
