package com.huertohogar.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Lo que el propio usuario puede cambiar de su perfil (PerfilPage.jsx).
 * No puede cambiar su rol, correo ni RUN. Mismas reglas que el registro.
 */
@Schema(description = "Datos editables del perfil propio")
public record PerfilUpdateRequest(
        @Schema(example = "Ana")
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres.")
        String nombre,

        @Schema(example = "Pérez Soto")
        @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres.")
        String apellidos,

        @Schema(example = "2002-05-14")
        @Past(message = "La fecha de nacimiento debe ser anterior a hoy.")
        LocalDate fechaNacimiento,

        @Schema(example = "Región Metropolitana")
        @NotBlank(message = "Debes seleccionar una región.")
        @Size(max = 60, message = "La región admite máximo 60 caracteres.")
        String region,

        @Schema(example = "Providencia")
        @NotBlank(message = "Debes seleccionar una comuna.")
        @Size(max = 60, message = "La comuna admite máximo 60 caracteres.")
        String comuna,

        @Schema(description = "Dirección de entrega", example = "Av. Providencia 1234, depto 56")
        @NotBlank(message = "La dirección es obligatoria.")
        @Size(min = 5, max = 300, message = "La dirección debe tener entre 5 y 300 caracteres.")
        String direccion,

        @Schema(example = "+56912345678")
        @Pattern(regexp = "^$|^(\\+?56)?9\\d{8}$", message = "Teléfono inválido, ej: +56912345678.")
        String telefono
) {
}
