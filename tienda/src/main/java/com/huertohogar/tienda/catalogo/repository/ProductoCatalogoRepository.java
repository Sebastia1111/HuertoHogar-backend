package com.huertohogar.tienda.catalogo.repository;

import com.huertohogar.tienda.catalogo.model.ProductoCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoCatalogoRepository extends JpaRepository<ProductoCatalogo, Long> {

    Optional<ProductoCatalogo> findByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);

    Optional<ProductoCatalogo> findByProductoId(Long productoId);

    boolean existsByCategoriaId(Long categoriaId);

    /**
     * Busqueda con filtros opcionales (un parametro null no filtra):
     * categoria (slug), texto en nombre/descripcion/origen, solo con stock,
     * solo stock bajo y si se incluyen productos dados de baja.
     */
    @Query("""
            select pc from ProductoCatalogo pc
            join fetch pc.producto p
            join fetch pc.categoria c
            where (:incluirInactivos = true or pc.activo = true)
              and (:categoria is null or lower(c.slug) = :categoria)
              and (:texto is null
                   or lower(p.nombre) like :texto
                   or lower(p.descripcion) like :texto
                   or lower(pc.origen) like :texto
                   or lower(pc.codigo) like :texto)
              and (:soloDisponibles = false or p.stock > 0)
              and (:stockBajo = false or p.stock <= pc.stockMinimo)
            order by pc.codigo
            """)
    List<ProductoCatalogo> buscar(@Param("categoria") String categoria,
                                  @Param("texto") String texto,
                                  @Param("soloDisponibles") boolean soloDisponibles,
                                  @Param("stockBajo") boolean stockBajo,
                                  @Param("incluirInactivos") boolean incluirInactivos);
}
