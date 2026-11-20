package com.huertohogar.tienda.common;

import org.springframework.http.HttpStatus;

import java.util.Map;

/** Error de logica de negocio (correo repetido, credenciales malas, etc.). */
public class ReglaNegocioException extends RuntimeException {

    private final HttpStatus status;
    private final Map<String, String> detalles;

    public ReglaNegocioException(HttpStatus status, String mensaje) {
        this(status, mensaje, null);
    }

    /** Con detalles por campo, para que el formulario marque el campo exacto. */
    public ReglaNegocioException(HttpStatus status, String mensaje, Map<String, String> detalles) {
        super(mensaje);
        this.status = status;
        this.detalles = detalles;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, String> getDetalles() {
        return detalles;
    }
}
