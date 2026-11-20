package com.huertohogar.usuarios.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/** Respuestas JSON para 401 (sin token / token malo) y 403 (rol sin permiso). */
public final class RestAuthHandlers {

    private RestAuthHandlers() {
    }

    public static AuthenticationEntryPoint noAutenticado() {
        return (request, response, ex) -> {
            Object detalle = request.getAttribute(JwtAuthFilter.ATRIBUTO_ERROR);
            String mensaje = detalle != null ? detalle.toString() : "Debes iniciar sesión (falta el token)";
            escribir(response, 401, "No autenticado", mensaje);
        };
    }

    public static AccessDeniedHandler accesoDenegado() {
        return (request, response, ex) ->
                escribir(response, 403, "Acceso denegado", "Tu rol no tiene permiso para este recurso");
    }

    private static void escribir(HttpServletResponse response, int status, String error, String mensaje)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String json = "{\"timestamp\":\"" + LocalDateTime.now() + "\",\"status\":" + status
                + ",\"error\":\"" + error + "\",\"message\":\"" + mensaje.replace("\"", "'") + "\"}";
        response.getWriter().write(json);
    }

}
