package com.huertohogar.usuarios.dto;

import com.huertohogar.usuarios.validation.RunValido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Formulario de registro del frontend (RegisterForm.jsx). Mismas reglas:
 * RUN con digito verificador, correo @gmail.com o @duocuc.cl (los @huertohogar.cl son solo internos),
 * clave de 4 a 10 caracteres sin espacios y region/comuna/direccion obligatorias.
 */
@Schema(description = "Datos para crear una cuenta de cliente")
public record RegistroRequest(
        @Schema(description = "RUN con o sin puntos y guion", example = "12.345.678-5")
        @NotBlank(message = "El RUN es obligatorio.")
        @RunValido
        String run,

        @Schema(example = "Ana")
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres.")
        String nombre,

        @Schema(example = "Pérez Soto")
        @NotBlank(message = "Los apellidos son obligatorios.")
        @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres.")
        String apellidos,

        @Schema(description = "Solo @gmail.com o @duocuc.cl (con @duocuc.cl hay 20% de descuento)", example = "ana.perez@duocuc.cl")
        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "Formato de correo inválido.")
        @Size(max = 100, message = "El correo admite máximo 100 caracteres.")
        @Pattern(regexp = "(?i)^[^@\\s]+@(gmail\\.com|duocuc\\.cl)$",
                message = "Solo aceptamos correos @gmail.com o @duocuc.cl (los @huertohogar.cl son de uso administrativo).")
        String correo,

        @Schema(description = "Entre 4 y 10 caracteres, sin espacios", example = "ana123")
        @NotBlank(message = "La contraseña es obligatoria.")
        @Pattern(regexp = "^\\S{4,10}$", message = "La contraseña debe tener entre 4 y 10 caracteres, sin espacios.")
        String clave,

        @Schema(description = "Opcional, formato AAAA-MM-DD", example = "2002-05-14")
        @Past(message = "La fecha de nacimiento debe ser anterior a hoy.")
        LocalDate fechaNacimiento,

        @Schema(example = "Región Metropolitana")
        @NotBlank(message = "Debes seleccionar una región.")
        @Size(max = 60, message = "La región admite máximo 60 caracteres.")
        String region,

        @Schema(example = "Santiago")
        @NotBlank(message = "Debes seleccionar una comuna.")
        @Size(max = 60, message = "La comuna admite máximo 60 caracteres.")
        String comuna,

        @Schema(description = "Dirección de entrega", example = "Av. Providencia 1234, depto 56")
        @NotBlank(message = "La dirección es obligatoria.")
        @Size(min = 5, max = 300, message = "La dirección debe tener entre 5 y 300 caracteres.")
        String direccion,

        @Schema(description = "Opcional", example = "+56912345678")
        @Pattern(regexp = "^$|^(\\+?56)?9\\d{8}$", message = "Teléfono inválido, ej: +56912345678.")
        String telefono
) {
}
