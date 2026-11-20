package com.huertohogar.tienda.common;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato unico de error. El frontend lee "message" (texto para mostrar)
 * y "detalles" (errores por campo, ej: { "correo": "Este correo ya existe" }).
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> detalles
) {
    public static ErrorResponse de(int status, String error, String message) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, null);
    }

    public static ErrorResponse de(int status, String error, String message, Map<String, String> detalles) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, detalles);
    }
}
