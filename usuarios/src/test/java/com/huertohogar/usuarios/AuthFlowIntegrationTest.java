package com.huertohogar.usuarios;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integracion con el mismo JSON que envia el frontend:
 * registro -> login -> /me -> roles -> refresh con rotacion -> logout.
 */
@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class AuthFlowIntegrationTest {

    private static final String REGISTRO = """
            {"run":"123456785","nombre":"Ana","apellidos":"Perez","correo":"ana@duocuc.cl","clave":"ana123",
             "region":"Región Metropolitana","comuna":"Santiago","direccion":"Calle Falsa 123"}
            """;

    @Autowired
    private MockMvc mvc;

    private String login(String correo, String clave) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"clave\":\"" + clave + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void flujoCompletoDeAutenticacion() throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(REGISTRO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.usuario.tipo").value("Cliente"));

        // Correo repetido -> 409 con detalle en el campo correo
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(REGISTRO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detalles.correo").exists());

        String respuesta = login("ana@duocuc.cl", "ana123");
        String access = JsonPath.read(respuesta, "$.accessToken");
        String refresh = JsonPath.read(respuesta, "$.refreshToken");

        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("ana@duocuc.cl"));
        // CLIENTE no puede listar usuarios
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + access))
                .andExpect(status().isForbidden());

        // El refresh token entrega tokens nuevos, pero no sirve como access token
        String renovado = mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + refresh))
                .andExpect(status().isUnauthorized());

        // Logout: los tokens anteriores (access y refresh) dejan de servir
        String access2 = JsonPath.read(renovado, "$.accessToken");
        mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + access2))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + access))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminPuedeListarUsuarios() throws Exception {
        String access = JsonPath.read(login("admin@huertohogar.cl", "12345"), "$.accessToken");
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk());
    }

    @Test
    void loginIncorrectoDevuelve401ConMessage() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@huertohogar.cl\",\"clave\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Correo o contraseña incorrectos"));
    }

    @Test
    void registroRechazaDatosInvalidos() throws Exception {
        String malo = """
                {"run":"123456789","nombre":"","correo":"x@huertohogar.cl","clave":"1 2"}
                """;
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(malo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.run").exists())
                .andExpect(jsonPath("$.detalles.correo").exists())
                .andExpect(jsonPath("$.detalles.clave").exists())
                .andExpect(jsonPath("$.detalles.direccion").exists());
    }
}
