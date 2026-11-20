package com.huertohogar.tienda.config;

import com.huertohogar.tienda.catalogo.model.Categoria;
import com.huertohogar.tienda.catalogo.model.MovimientoStock;
import com.huertohogar.tienda.catalogo.model.ProductoCatalogo;
import com.huertohogar.tienda.catalogo.model.TipoMovimiento;
import com.huertohogar.tienda.catalogo.repository.CategoriaRepository;
import com.huertohogar.tienda.catalogo.repository.MovimientoStockRepository;
import com.huertohogar.tienda.catalogo.repository.ProductoCatalogoRepository;
import com.huertohogar.tienda.inventario.model.Producto;
import com.huertohogar.tienda.inventario.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carga inicial con las categorias y los 9 productos del caso HuertoHogar.
 * Son los mismos datos del catalogo de respaldo del frontend (src/data/productos.js).
 * Solo se ejecuta si la tabla de categorias esta vacia (en MySQL, solo el primer arranque).
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final CategoriaRepository categoriaRepository;
    private final ProductoCatalogoRepository catalogoRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoStockRepository movimientoRepository;

    public DataLoader(CategoriaRepository categoriaRepository,
                      ProductoCatalogoRepository catalogoRepository,
                      ProductoRepository productoRepository,
                      MovimientoStockRepository movimientoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.catalogoRepository = catalogoRepository;
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categoriaRepository.count() > 0) {
            return;
        }
        Categoria frutas = categoriaRepository.save(new Categoria(null, "frutas", "FR", "Frutas Frescas",
                "Frutas cultivadas y cosechadas en su punto óptimo de madurez, directo del campo a tu hogar."));
        Categoria verduras = categoriaRepository.save(new Categoria(null, "verduras", "VR", "Verduras Orgánicas",
                "Verduras cultivadas sin pesticidas ni químicos, con sabor auténtico y natural."));
        Categoria organicos = categoriaRepository.save(new Categoria(null, "organicos", "PO", "Productos Orgánicos",
                "Productos elaborados con ingredientes naturales y procesados de manera responsable."));
        Categoria lacteos = categoriaRepository.save(new Categoria(null, "lacteos", "PL", "Productos Lácteos",
                "Lácteos de granjas locales con producción responsable y de calidad."));

        agregar("FR001", frutas, "Manzanas Fuji", 1200, 150, 20, "kilo", "🍎", "Valle del Maule",
                "Manzanas Fuji crujientes y dulces, cultivadas en el Valle del Maule.",
                "Cultivo de temporada con manejo integrado de plagas y cosecha manual.");
        agregar("FR002", frutas, "Naranjas Valencia", 1000, 200, 20, "kilo", "🍊", "Coquimbo",
                "Jugosas y ricas en vitamina C, ideales para zumos frescos.",
                "Riego por goteo y recolección en el punto óptimo de madurez.");
        agregar("FR003", frutas, "Plátanos Cavendish", 800, 250, 25, "kilo", "🍌", "Ecuador",
                "Plátanos maduros y dulces, ricos en potasio y energía.",
                "Proveedores con certificación de comercio justo.");
        agregar("VR001", verduras, "Zanahorias Orgánicas", 900, 100, 15, "kilo", "🥕", "Región de O'Higgins",
                "Zanahorias crujientes cultivadas sin pesticidas.",
                "Producción orgánica certificada, sin pesticidas ni fertilizantes sintéticos.");
        agregar("VR002", verduras, "Espinacas Frescas", 700, 80, 15, "bolsa de 500g", "🥬", "Santiago",
                "Espinacas frescas y nutritivas para ensaladas.",
                "Cultivo hidropónico con uso reducido de agua.");
        agregar("VR003", verduras, "Pimientos Tricolores", 1500, 120, 15, "kilo", "🫑", "Valle Central",
                "Pimientos rojos, amarillos y verdes, ricos en antioxidantes y vitaminas.",
                "Manejo integrado de plagas y envases reutilizables.");
        agregar("PO001", organicos, "Miel Orgánica", 5000, 50, 10, "frasco de 500g", "🍯", "Apicultores locales",
                "Miel pura producida por apicultores locales.",
                "Apicultura regenerativa que protege la polinización local.");
        agregar("PO003", organicos, "Quinua Orgánica", 4500, 40, 10, "bolsa de 500g", "🌾", "Altiplano chileno",
                "Grano ancestral con alto contenido proteico, ideal para ensaladas y guisos.",
                "Cultivo rotativo de pequeños agricultores, sin pesticidas.");
        agregar("PL001", lacteos, "Leche Entera", 950, 120, 20, "litro", "🥛", "Granjas locales",
                "Leche entera fresca rica en calcio y nutrientes.",
                "Ganadería de pastoreo con bienestar animal y trazabilidad por lote.");

        log.info("Catálogo inicial cargado: {} categorías y {} productos",
                categoriaRepository.count(), catalogoRepository.count());
    }

    private void agregar(String codigo, Categoria categoria, String nombre, double precio, int stock, int stockMinimo,
                         String unidad, String emoji, String origen, String descripcion, String practicas) {
        Producto p = productoRepository.save(new Producto(null, nombre, descripcion, precio, stock));
        ProductoCatalogo pc = new ProductoCatalogo();
        pc.setCodigo(codigo);
        pc.setProducto(p);
        pc.setCategoria(categoria);
        pc.setStockMinimo(stockMinimo);
        pc.setUnidad(unidad);
        pc.setEmoji(emoji);
        pc.setOrigen(origen);
        pc.setPracticasSostenibles(practicas);
        pc.setActivo(true);
        catalogoRepository.save(pc);

        MovimientoStock m = new MovimientoStock();
        m.setProducto(p);
        m.setTipo(TipoMovimiento.ENTRADA);
        m.setCantidad(stock);
        m.setStockAnterior(0);
        m.setStockNuevo(stock);
        m.setMotivo("Stock inicial");
        m.setUsuario("sistema");
        movimientoRepository.save(m);
    }
}
