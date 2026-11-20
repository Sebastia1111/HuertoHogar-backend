package com.huertohogar.tienda.config;

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
            Catálogo, compras con boleta, pedidos e inventario de HuertoHogar.

            **Cómo probar en 3 pasos**
            1. Inicia sesión en el Swagger de usuarios: http://localhost:8082/swagger-ui.html \
            (**POST /api/auth/login**, el ejemplo ya trae `admin@huertohogar.cl` / `12345`).
            2. Copia el `accessToken` de la respuesta.
            3. Vuelve aquí, presiona **Authorize** (arriba a la derecha), pega el token y confirma.

            Lo marcado **público** funciona sin token. Una compra de prueba: \
            **2. Compras y pedidos → POST /api/tienda/carrito/checkout** con el ejemplo que trae.
            """;

    @Bean
    public OpenAPI tiendaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("HuertoHogar · Microservicio Tienda (puerto 8083)")
                        .version("1.0")
                        .description(GUIA))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Pega solo el accessToken (lo entrega usuarios), sin la palabra Bearer.")));
    }
}
