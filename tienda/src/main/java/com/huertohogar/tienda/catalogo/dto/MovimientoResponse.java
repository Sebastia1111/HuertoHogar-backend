package com.huertohogar.tienda.catalogo.dto;

import com.huertohogar.tienda.catalogo.model.MovimientoStock;
import com.huertohogar.tienda.catalogo.model.TipoMovimiento;

import java.time.LocalDateTime;

public record MovimientoResponse(
        Long id,
        TipoMovimiento tipo,
        int cantidad,
        int stockAnterior,
        int stockNuevo,
        String motivo,
        String referencia,
        String usuario,
        LocalDateTime fecha
) {
    public static MovimientoResponse desde(MovimientoStock m) {
        return new MovimientoResponse(m.getId(), m.getTipo(), m.getCantidad(), m.getStockAnterior(),
                m.getStockNuevo(), m.getMotivo(), m.getReferencia(), m.getUsuario(), m.getFecha());
    }
}
