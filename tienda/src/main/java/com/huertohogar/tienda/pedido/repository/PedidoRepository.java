package com.huertohogar.tienda.pedido.repository;

import com.huertohogar.tienda.pedido.model.EstadoPedido;
import com.huertohogar.tienda.pedido.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByNumero(String numero);

    List<Pedido> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    List<Pedido> findByCorreoClienteIgnoreCaseOrderByFechaCreacionDesc(String correo);

    List<Pedido> findByEstadoOrderByFechaCreacionDesc(EstadoPedido estado);

    List<Pedido> findAllByOrderByFechaCreacionDesc();
}
