package com.huertohogar.tienda.catalogo.repository;

import com.huertohogar.tienda.catalogo.model.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {

    List<MovimientoStock> findByProductoIdOrderByFechaDescIdDesc(Long productoId);

    @Modifying
    @Query("delete from MovimientoStock m where m.producto.id = :productoId")
    void eliminarDeProducto(@Param("productoId") Long productoId);
}
