package com.huertohogar.tienda.catalogo.dto;

import com.huertohogar.tienda.catalogo.model.TipoMovimiento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Suma (ENTRADA) o resta (SALIDA) unidades al stock. */
public record AjusteStockRequest(
        @Schema(description = "ENTRADA o SALIDA", example = "ENTRADA")
        @NotNull(message = "El tipo es obligatorio (ENTRADA o SALIDA).")
        TipoMovimiento tipo,

        @Schema(example = "30")
        @NotNull(message = "La cantidad es obligatoria.")
        @Min(value = 1, message = "La cantidad mínima es 1.")
        @Max(value = 100_000, message = "La cantidad máxima es 100.000.")
        Integer cantidad,

        @Schema(example = "Llegada de proveedor")
        @Size(max = 200, message = "El motivo admite máximo 200 caracteres.")
        String motivo,

        @Schema(example = "Guía 4521")
        @Size(max = 60, message = "La referencia admite máximo 60 caracteres.")
        String referencia
) {
}
