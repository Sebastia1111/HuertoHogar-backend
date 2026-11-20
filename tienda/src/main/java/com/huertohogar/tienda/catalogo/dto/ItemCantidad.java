package com.huertohogar.tienda.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** Línea { codigo, cantidad } como la arma el carrito del frontend. */
public record ItemCantidad(
        @Schema(example = "FR001")
        @NotBlank(message = "El código del producto es obligatorio.")
        @Pattern(regexp = "^[A-Za-z]{2}\\d{3}$", message = "Código de producto inválido, ej: FR001.")
        String codigo,

        @Schema(example = "2")
        @NotNull(message = "La cantidad es obligatoria.")
        @Min(value = 1, message = "La cantidad mínima es 1.")
        @Max(value = 999, message = "La cantidad máxima es 999.")
        Integer cantidad
) {
}
