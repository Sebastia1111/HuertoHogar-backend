package com.huertohogar.tienda.common;

import com.huertohogar.tienda.security.UsuarioAutenticado;
import org.springframework.http.HttpStatus;

/** Reglas de propiedad: un cliente solo puede operar con sus propios datos. */
public final class Acceso {

    private Acceso() {
    }

    /**
     * El frontend envia "correo" en algunas peticiones. La identidad real sale del token:
     * si el correo enviado no es el del token (y no es ADMIN), se rechaza.
     */
    public static void verificarCorreo(UsuarioAutenticado usuario, String correo) {
        if (correo != null && !correo.isBlank()
                && !correo.equalsIgnoreCase(usuario.correo()) && !usuario.esAdmin()) {
            throw new ReglaNegocioException(HttpStatus.FORBIDDEN, "No puedes operar con datos de otro usuario");
        }
    }
}
