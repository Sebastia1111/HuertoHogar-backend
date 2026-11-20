package com.huertohogar.tienda.pedido.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Comunicacion entre microservicios: tienda le pide a "usuarios" el perfil del cliente
 * (direccion de entrega) reenviando su mismo token.
 * 1. Primero pregunta a Eureka donde esta "usuarios".
 * 2. Si Eureka esta apagado o aun no lo conoce, usa la direccion fija app.usuarios.url.
 * 3. Si usuarios tampoco responde, la compra igual se realiza (direccion "por confirmar").
 * Por eso Eureka es opcional y el orden en que se inician los servicios no importa.
 */
@Component
public class UsuariosClient {

    private static final Logger log = LoggerFactory.getLogger(UsuariosClient.class);

    /** Datos del perfil que necesita el pedido. */
    public record PerfilEntrega(String nombre, String direccion, String comuna, String region) {
    }

    private final ObjectProvider<DiscoveryClient> discoveryClient;
    private final String serviceId;
    private final String urlRespaldo;
    private final RestClient restClient;

    public UsuariosClient(ObjectProvider<DiscoveryClient> discoveryClient,
                          @Value("${app.usuarios.service-id:usuarios}") String serviceId,
                          @Value("${app.usuarios.url:http://localhost:8082}") String urlRespaldo) {
        this.discoveryClient = discoveryClient;
        this.serviceId = serviceId;
        this.urlRespaldo = urlRespaldo;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    public Optional<PerfilEntrega> perfil(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return Optional.empty();
        }
        try {
            String url = direccionUsuarios() + "/api/auth/me";
            Map<String, Object> datos = restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            if (datos == null) {
                return Optional.empty();
            }
            String nombre = (texto(datos.get("nombre")) + " " + texto(datos.get("apellidos"))).trim();
            return Optional.of(new PerfilEntrega(nombre.isEmpty() ? null : nombre, textoONull(datos.get("direccion")),
                    textoONull(datos.get("comuna")), textoONull(datos.get("region"))));
        } catch (Exception e) {
            log.warn("No se pudo consultar el perfil en '{}': {}", serviceId, e.getMessage());
            return Optional.empty();
        }
    }

    /** Direccion de usuarios: la que informa Eureka o, si no hay, la de respaldo. */
    private String direccionUsuarios() {
        try {
            DiscoveryClient discovery = discoveryClient.getIfAvailable();
            List<ServiceInstance> instancias = discovery == null ? List.of() : discovery.getInstances(serviceId);
            if (!instancias.isEmpty()) {
                return instancias.get(0).getUri().toString();
            }
        } catch (Exception e) {
            log.debug("Eureka no disponible: {}", e.getMessage());
        }
        log.info("Eureka no conoce '{}': se usa la dirección de respaldo {}", serviceId, urlRespaldo);
        return urlRespaldo;
    }

    private static String texto(Object valor) {
        return valor == null ? "" : valor.toString();
    }

    private static String textoONull(Object valor) {
        return valor == null || valor.toString().isBlank() ? null : valor.toString();
    }
}
