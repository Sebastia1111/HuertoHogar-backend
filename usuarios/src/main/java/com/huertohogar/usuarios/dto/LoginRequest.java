package com.huertohogar.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Lo que envía LoginForm.jsx: { correo, clave }. El ejemplo de Swagger ya trae la cuenta admin. */
@Schema(description = "Credenciales de inicio de sesión")
public record LoginRequest(
        @Schema(example = "admin@huertohogar.cl")
        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "Formato de correo inválido.")
        @Size(max = 100, message = "El correo admite máximo 100 caracteres.")
        String correo,

        @Schema(example = "12345")
        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(max = 10, message = "La contraseña admite máximo 10 caracteres.")
        String clave
) {
}
