package com.huertohogar.usuarios.dto;

import com.huertohogar.usuarios.model.Rol;
import com.huertohogar.usuarios.model.Usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Datos publicos del usuario (nunca incluye la contrasena). "tipo" es el texto que muestra el Header. */
public record UsuarioResponse(
        Long id,
        String run,
        String nombre,
        String apellidos,
        String correo,
        LocalDate fechaNacimiento,
        String region,
        String comuna,
        String direccion,
        String telefono,
        Rol rol,
        String tipo,
        boolean activo,
        LocalDateTime fechaRegistro
) {
    public static UsuarioResponse desde(Usuario u) {
        String tipo = switch (u.getRol()) {
            case ADMIN -> "Administrador";
            case VENDEDOR -> "Vendedor";
            case CLIENTE -> "Cliente";
        };
        return new UsuarioResponse(u.getId(), u.getRun(), u.getNombre(), u.getApellidos(), u.getCorreo(),
                u.getFechaNacimiento(), u.getRegion(), u.getComuna(), u.getDireccion(), u.getTelefono(),
                u.getRol(), tipo, u.isActivo(), u.getFechaRegistro());
    }
}
