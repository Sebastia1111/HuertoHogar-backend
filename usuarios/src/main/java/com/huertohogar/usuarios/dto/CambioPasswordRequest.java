package com.huertohogar.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CambioPasswordRequest(
        @Schema(example = "12345")
        @NotBlank(message = "La contraseña actual es obligatoria.")
        String claveActual,

        @Schema(description = "Entre 4 y 10 caracteres, sin espacios", example = "nueva123")
        @NotBlank(message = "La nueva contraseña es obligatoria.")
        @Pattern(regexp = "^\\S{4,10}$", message = "La contraseña debe tener entre 4 y 10 caracteres, sin espacios.")
        String claveNueva
) {
}
