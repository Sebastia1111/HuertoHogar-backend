package com.huertohogar.usuarios.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RunValidator implements ConstraintValidator<RunValido, String> {

    @Override
    public boolean isValid(String run, ConstraintValidatorContext context) {
        if (run == null) {
            return true; // @NotBlank se encarga de los nulos
        }
        return esRunValido(run);
    }

    /** Quita puntos, guion y espacios, y deja la K en mayuscula: "19.011.022-k" -> "19011022K". */
    public static String normalizar(String run) {
        return run == null ? null : run.replaceAll("[.\\-\\s]", "").toUpperCase();
    }

    /** Algoritmo modulo 11 */
    public static boolean esRunValido(String run) {
        String valor = normalizar(run);
        if (!valor.matches("^\\d{6,8}[0-9K]$")) {
            return false;
        }
        String cuerpo = valor.substring(0, valor.length() - 1);
        char dvIngresado = valor.charAt(valor.length() - 1);

        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
            multiplicador = (multiplicador == 7) ? 2 : multiplicador + 1;
        }
        int resto = 11 - (suma % 11);
        char dvCalculado = switch (resto) {
            case 11 -> '0';
            case 10 -> 'K';
            default -> Character.forDigit(resto, 10);
        };
        return dvCalculado == dvIngresado;
    }
}
