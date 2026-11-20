package com.huertohogar.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @Schema(description = "El refreshToken que entregó /api/auth/login")
        @NotBlank(message = "El refreshToken es obligatorio.")
        String refreshToken
) {
}
