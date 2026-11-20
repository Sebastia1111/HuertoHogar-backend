package com.huertohogar.tienda.config;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Abrir http://localhost:8083 lleva directo a Swagger. */
@Hidden
@Controller
public class InicioController {

    @GetMapping("/")
    public String inicio() {
        return "redirect:/swagger-ui.html";
    }
}
