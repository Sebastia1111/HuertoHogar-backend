package com.huertohogar.usuarios.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger: guía de uso en la portada y botón "Authorize" para pegar el token. */
@Configuration
public class OpenApiConfig {

    private static final String GUIA = """
            Registro, inicio de sesión con JWT, perfil y gestión de usuarios por rol.

            **Cómo probar en 3 pasos**
            1. Abre **1. Sesión → POST /api/auth/login**, presiona *Try it out* y luego *Execute* \
            (el ejemplo ya trae `admin@huertohogar.cl` / `12345`).
            2. Copia el `accessToken` de la respuesta.
            3. Presiona **Authorize** (arriba a la derecha), pega el token y confirma. Queda guardado aunque recargues.

            Cuentas de prueba (clave `12345`): `admin@huertohogar.cl`, `vendedor@huertohogar.cl`, `cliente@huertohogar.cl`.

            El mismo token sirve en el Swagger de tienda: http://localhost:8083/swagger-ui.html
            """;

    @Bean
    public OpenAPI usuariosOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("HuertoHogar · Microservicio Usuarios (puerto 8082)")
                        .version("1.0")
                        .description(GUIA))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Pega solo el accessToken, sin la palabra Bearer.")));
    }
}
