package com.huertohogar.usuarios;

import com.huertohogar.usuarios.validation.RunValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunValidatorTest {

    @Test
    void aceptaRunsValidos() {
        assertTrue(RunValidator.esRunValido("123456785"));
        assertTrue(RunValidator.esRunValido("111111111"));
        assertTrue(RunValidator.esRunValido("190110222"));
        assertTrue(RunValidator.esRunValido("12.345.678-5")); // con puntos y guion tambien sirve
    }

    @Test
    void rechazaRunsInvalidos() {
        assertFalse(RunValidator.esRunValido("123456789"));   // digito verificador malo
        assertFalse(RunValidator.esRunValido("12AB5678K"));    // letras en el cuerpo
        assertFalse(RunValidator.esRunValido("12345"));        // muy corto
    }
}
