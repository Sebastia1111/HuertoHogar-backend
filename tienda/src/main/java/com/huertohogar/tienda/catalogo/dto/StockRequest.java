package com.huertohogar.tienda.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** PATCH /productos/{id}/stock desde el panel admin: fija el stock a un valor (conteo físico). */
public record StockRequest(
        @Schema(example = "120")
        @NotNull(message = "El stock es obligatorio.")
        @Min(value = 0, message = "El stock no puede ser negativo.")
        @Max(value = 100_000, message = "El stock no puede superar 100.000 unidades.")
        Integer stock,

        @Schema(example = "Conteo físico de bodega")
        @Size(max = 200, message = "El motivo admite máximo 200 caracteres.")
        String motivo
) {
}
