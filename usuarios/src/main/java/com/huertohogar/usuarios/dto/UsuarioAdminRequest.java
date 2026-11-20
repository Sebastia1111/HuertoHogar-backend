package com.huertohogar.usuarios.dto;

import com.huertohogar.usuarios.model.Rol;
import com.huertohogar.usuarios.validation.RunValido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Creación / edición de usuarios por un ADMIN (PanelUsuarios.jsx).
 * La clave es obligatoria al crear y opcional al editar (vacía = no se cambia).
 * El admin puede usar cualquier dominio de correo (ej: @huertohogar.cl para personal interno).
 */
@Schema(description = "Usuario creado o editado por un administrador")
public record UsuarioAdminRequest(
        @Schema(example = "22.222.222-2")
        @NotBlank(message = "El RUN es obligatorio.") @RunValido
        String run,

        @Schema(example = "Valentina")
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres.")
        String nombre,

        @Schema(example = "Rojas")
        @Size(max = 100, message = "Los apellidos admiten máximo 100 caracteres.")
        String apellidos,

        @Schema(example = "valentina@huertohogar.cl")
        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "Formato de correo inválido.")
        @Size(max = 100, message = "El correo admite máximo 100 caracteres.")
        String correo,

        @Schema(description = "Obligatoria al crear; vacía al editar = no se cambia", example = "vend123")
        @Pattern(regexp = "^$|^\\S{4,10}$", message = "La contraseña debe tener entre 4 y 10 caracteres, sin espacios.")
        String clave,

        @Past(message = "La fecha de nacimiento debe ser anterior a hoy.")
        LocalDate fechaNacimiento,

        @Size(max = 60, message = "La región admite máximo 60 caracteres.")
        String region,

        @Size(max = 60, message = "La comuna admite máximo 60 caracteres.")
        String comuna,

        @Size(max = 300, message = "La dirección admite máximo 300 caracteres.")
        String direccion,

        @Pattern(regexp = "^$|^(\\+?56)?9\\d{8}$", message = "Teléfono inválido, ej: +56912345678.")
        String telefono,

        @Schema(example = "VENDEDOR")
        @NotNull(message = "El rol es obligatorio.")
        Rol rol,

        @Schema(description = "Opcional; por defecto true", example = "true")
        Boolean activo
) {
}
