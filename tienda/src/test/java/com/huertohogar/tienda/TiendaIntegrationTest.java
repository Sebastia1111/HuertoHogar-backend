package com.huertohogar.tienda;

import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integracion de la tienda con las mismas rutas y JSON que usa el frontend.
 * El token se firma aqui con la misma clave que usa el microservicio usuarios.
 */
@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class TiendaIntegrationTest {

    private static final String SECRETO = "MEwreeJ2WVKjMTo6eMI6B3KPoHvDFj1T536DgWsgtxzryJ0wUsUNbSYdV+LN2GiT";

    @Autowired
    private MockMvc mvc;

    private String token(long uid, String correo, String rol) {
        return "Bearer " + Jwts.builder()
                .subject(correo)
                .issuer("huertohogar-usuarios")
                .claim("uid", uid)
                .claim("rol", rol)
                .claim("nombre", "Prueba")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRETO)))
                .compact();
    }

    @Test
    void catalogoPublicoConFiltros() throws Exception {
        mvc.perform(get("/api/tienda/catalogo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(9))
                .andExpect(jsonPath("$[0].codigo").value("FR001"))
                .andExpect(jsonPath("$[0].categoria").value("frutas"))
                .andExpect(jsonPath("$[0].stockMinimo").value(20));
        mvc.perform(get("/api/tienda/catalogo").param("categoria", "lacteos"))
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/inventario/productos").param("q", "miel"))
                .andExpect(jsonPath("$[0].codigo").value("PO001"));
        mvc.perform(get("/api/tienda/catalogo/VR001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.origen").value("Región de O'Higgins"));
    }

    @Test
    void permisosPorRol() throws Exception {
        String cliente = token(1, "cliente@huertohogar.cl", "CLIENTE");
        String vendedor = token(2, "vendedor@huertohogar.cl", "VENDEDOR");
        String stock = "{\"stock\":90,\"motivo\":\"Conteo\"}";

        mvc.perform(patch("/api/inventario/productos/2/stock").contentType(MediaType.APPLICATION_JSON).content(stock))
                .andExpect(status().isUnauthorized());
        mvc.perform(patch("/api/inventario/productos/2/stock").header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON).content(stock))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/inventario/productos/2/stock").header("Authorization", vendedor)
                        .contentType(MediaType.APPLICATION_JSON).content(stock))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(90));
        // Queda registrado en el historial
        mvc.perform(get("/api/inventario/productos/2/movimientos").header("Authorization", vendedor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("AJUSTE"));
        // VENDEDOR no puede eliminar (solo ADMIN)
        mvc.perform(delete("/api/inventario/productos/2").header("Authorization", vendedor))
                .andExpect(status().isForbidden());
        // Token falso
        mvc.perform(get("/api/tienda/pedidos").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token inválido"));
    }

    @Test
    void checkoutDescuentaStockYGeneraBoletaConDescuentoDuoc() throws Exception {
        String alumno = token(50, "alumno@duocuc.cl", "CLIENTE");
        // FR003 Platanos: $800, stock 250. Con 20% Duoc -> $640 c/u
        String body = "{\"correo\":\"alumno@duocuc.cl\",\"items\":[{\"codigo\":\"FR003\",\"cantidad\":5}],"
                + "\"referencia\":\"HH-1001\"}";
        String respuesta = mvc.perform(post("/api/tienda/carrito/checkout").header("Authorization", alumno)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pedido.subtotal").value(4000))
                .andExpect(jsonPath("$.pedido.descuento").value(800))
                .andExpect(jsonPath("$.pedido.total").value(3200))
                .andExpect(jsonPath("$.pedido.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.productos[0].codigo").value("FR003"))
                .andExpect(jsonPath("$.productos[0].stock").value(245))
                .andReturn().getResponse().getContentAsString();
        String numero = JsonPath.read(respuesta, "$.numero");

        // Aparece en su historial
        mvc.perform(get("/api/tienda/pedidos").header("Authorization", alumno))
                .andExpect(jsonPath("$[0].numero").value(numero));
        // Otro cliente no puede verlo
        mvc.perform(get("/api/tienda/pedidos/" + numero).header("Authorization", token(51, "otro@gmail.com", "CLIENTE")))
                .andExpect(status().isNotFound());
        // No puede comprar a nombre de otro correo
        mvc.perform(post("/api/tienda/carrito/checkout").header("Authorization", alumno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"otro@gmail.com\",\"items\":[{\"codigo\":\"FR003\",\"cantidad\":1}]}"))
                .andExpect(status().isForbidden());

        // El personal avanza el estado; el cliente ya no puede cancelar
        String admin = token(99, "admin@huertohogar.cl", "ADMIN");
        mvc.perform(patch("/api/tienda/pedidos/" + numero + "/estado").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"EN_PREPARACION\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.historial.length()").value(2));
        mvc.perform(post("/api/tienda/pedidos/" + numero + "/cancelar").header("Authorization", alumno))
                .andExpect(status().isConflict());
        // Saltarse estados no se permite
        mvc.perform(patch("/api/tienda/pedidos/" + numero + "/estado").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"ENTREGADO\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void checkoutSinStockNoDescuentaNada() throws Exception {
        String cliente = token(60, "cliente2@gmail.com", "CLIENTE");
        // PO001 tiene 50: pedir 9999 debe fallar sin tocar FR001
        String body = "{\"items\":[{\"codigo\":\"FR001\",\"cantidad\":1},{\"codigo\":\"PO001\",\"cantidad\":999}]}";
        mvc.perform(post("/api/tienda/carrito/checkout").header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detalles.PO001").exists());
        mvc.perform(get("/api/tienda/catalogo/FR001"))
                .andExpect(jsonPath("$.stock").value(150));
    }

    @Test
    void cancelarPedidoDevuelveStock() throws Exception {
        String cliente = token(70, "cliente3@gmail.com", "CLIENTE");
        // PL001 Leche: $950, stock 120
        String respuesta = mvc.perform(post("/api/tienda/carrito/checkout").header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"codigo\":\"PL001\",\"cantidad\":2}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(1900))
                .andExpect(jsonPath("$.productos[0].stock").value(118))
                .andReturn().getResponse().getContentAsString();

        String numero = JsonPath.read(respuesta, "$.numero");
        mvc.perform(post("/api/tienda/pedidos/" + numero + "/cancelar").header("Authorization", cliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));
        mvc.perform(get("/api/tienda/catalogo/PL001"))
                .andExpect(jsonPath("$.stock").value(120));
    }

    @Test
    void checkoutSinProductosResponde400() throws Exception {
        mvc.perform(post("/api/tienda/carrito/checkout").header("Authorization", token(80, "c4@gmail.com", "CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"items\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.items").exists());
    }

    @Test
    void refreshTokenNoSirveEnLaTienda() throws Exception {
        String refresh = "Bearer " + Jwts.builder()
                .subject("cliente@huertohogar.cl")
                .issuer("huertohogar-usuarios")
                .claim("tipo", "refresh")
                .claim("uid", 1)
                .claim("rol", "CLIENTE")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRETO)))
                .compact();
        mvc.perform(get("/api/tienda/pedidos").header("Authorization", refresh))
                .andExpect(status().isUnauthorized());
    }
}
