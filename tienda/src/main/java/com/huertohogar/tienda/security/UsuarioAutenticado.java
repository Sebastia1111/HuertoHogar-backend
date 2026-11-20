package com.huertohogar.tienda.security;

/** Datos del usuario que vienen dentro del JWT emitido por el microservicio usuarios. */
public record UsuarioAutenticado(Long id, String correo, String rol, String nombre) {

    public boolean esAdmin() {
        return "ADMIN".equals(rol);
    }

    public boolean esPersonal() {
        return "ADMIN".equals(rol) || "VENDEDOR".equals(rol);
    }
}
