package com.huertohogar.usuarios.security;

/** Datos del usuario que vienen dentro del JWT (queda como "principal" en Spring Security). */
public record UsuarioAutenticado(Long id, String correo, String rol, int version) {
}
