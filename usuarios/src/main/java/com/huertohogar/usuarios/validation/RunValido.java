package com.huertohogar.usuarios.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valida un RUN chileno (con o sin puntos y guion: se normalizan antes)
 * con su digito verificador (modulo 11). Misma regla que el frontend.
 */
@Documented
@Constraint(validatedBy = RunValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface RunValido {
    String message() default "RUN inválido: revisa el dígito verificador (ej: 12.345.678-5 o 123456785).";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
