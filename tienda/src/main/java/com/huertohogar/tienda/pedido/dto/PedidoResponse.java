package com.huertohogar.tienda.pedido.dto;

import com.huertohogar.tienda.common.Precios;
import com.huertohogar.tienda.pedido.model.DetallePedido;
import com.huertohogar.tienda.pedido.model.EstadoPedido;
import com.huertohogar.tienda.pedido.model.HistorialEstadoPedido;
import com.huertohogar.tienda.pedido.model.Pedido;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Pedido con formato de boleta: detalle, subtotal, descuento, neto, IVA, total y seguimiento. */
public record PedidoResponse(
        String numero,
        LocalDateTime fecha,
        EstadoPedido estado,
        String estadoDescripcion,
        String correoCliente,
        String nombreCliente,
        List<Linea> items,
        long subtotal,
        long descuento,
        long neto,
        long iva,
        long total,
        boolean descuentoDuoc,
        LocalDate fechaEntregaPreferida,
        String direccionEntrega,
        String comuna,
        String region,
        String referenciaCliente,
        List<Seguimiento> historial
) {

    public record Linea(String codigo, String nombre, String unidad, long precioUnitario,
                        long precioFinal, int cantidad, long subtotal) {
        static Linea desde(DetallePedido d) {
            return new Linea(d.getCodigo(), d.getNombre(), d.getUnidad(), d.getPrecioUnitario(),
                    d.getPrecioFinal(), d.getCantidad(), d.getSubtotal());
        }
    }

    public record Seguimiento(EstadoPedido estado, String descripcion, String comentario,
                              String usuario, LocalDateTime fecha) {
        static Seguimiento desde(HistorialEstadoPedido h) {
            return new Seguimiento(h.getEstado(), h.getEstado().getDescripcion(), h.getComentario(),
                    h.getUsuario(), h.getFecha());
        }
    }

    public static PedidoResponse desde(Pedido p) {
        return new PedidoResponse(p.getNumero(), p.getFechaCreacion(), p.getEstado(),
                p.getEstado().getDescripcion(), p.getCorreoCliente(), p.getNombreCliente(),
                p.getDetalles().stream().map(Linea::desde).toList(),
                p.getSubtotal(), p.getDescuento(), p.getNeto(), p.getIva(), p.getTotal(),
                Precios.tieneDescuentoDuoc(p.getCorreoCliente()), p.getFechaEntregaPreferida(),
                p.getDireccionEntrega(), p.getComuna(), p.getRegion(), p.getReferenciaCliente(),
                p.getHistorial().stream().map(Seguimiento::desde).toList());
    }
}
