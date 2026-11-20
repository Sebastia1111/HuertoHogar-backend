package com.huertohogar.usuarios.dto;

/**
 * Respuesta de login/registro/refresh. Nombres iguales a los que lee session.js del frontend:
 * accessToken, refreshToken, tokenType, expiresIn (segundos) y usuario.
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UsuarioResponse usuario
) {
}
