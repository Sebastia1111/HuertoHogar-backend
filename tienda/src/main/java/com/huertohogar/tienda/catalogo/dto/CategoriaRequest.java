package com.huertohogar.tienda.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @Schema(description = "Identificador que usa el frontend", example = "hierbas")
        @NotBlank(message = "El slug es obligatorio.")
        @Pattern(regexp = "^[a-z0-9-]{2,30}$", message = "El slug va en minúsculas y sin espacios, ej: frutas.")
        String slug,

        @Schema(description = "Prefijo de los códigos de producto", example = "HB")
        @NotBlank(message = "El código es obligatorio.")
        @Pattern(regexp = "^[A-Za-z]{2,5}$", message = "El código debe tener de 2 a 5 letras, ej: FR.")
        String codigo,

        @Schema(example = "Hierbas Aromáticas")
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(min = 2, max = 60, message = "El nombre debe tener entre 2 y 60 caracteres.")
        String nombre,

        @Schema(example = "Albahaca, menta y cilantro frescos.")
        @Size(max = 1000, message = "La descripción admite máximo 1000 caracteres.")
        String descripcion
) {
}
