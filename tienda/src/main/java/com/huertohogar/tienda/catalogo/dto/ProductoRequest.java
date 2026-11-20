package com.huertohogar.tienda.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Producto nuevo o editado (ADMIN / VENDEDOR). */
@Schema(description = "Datos de un producto del inventario")
public record ProductoRequest(
        @Schema(description = "2 letras de la categoría + 3 números", example = "FR004")
        @NotBlank(message = "El código es obligatorio.")
        @Pattern(regexp = "^[A-Za-z]{2}\\d{3}$", message = "El código debe tener 2 letras y 3 números, ej: FR001.")
        String codigo,

        @Schema(example = "Frutillas de Chiloé")
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres.")
        String nombre,

        @Schema(example = "Frutillas dulces de temporada.")
        @Size(max = 255, message = "La descripción admite máximo 255 caracteres.")
        String descripcion,

        @Schema(description = "Precio en pesos chilenos, IVA incluido", example = "2500")
        @NotNull(message = "El precio es obligatorio.")
        @Positive(message = "El precio debe ser mayor a 0.")
        @Max(value = 10_000_000, message = "El precio no puede superar $10.000.000.")
        Double precio,

        @Schema(example = "80")
        @NotNull(message = "El stock es obligatorio.")
        @Min(value = 0, message = "El stock no puede ser negativo.")
        @Max(value = 100_000, message = "El stock no puede superar 100.000 unidades.")
        Integer stock,

        @Schema(description = "Bajo este stock aparece una alerta; por defecto 10", example = "15")
        @Min(value = 0, message = "El stock mínimo no puede ser negativo.")
        @Max(value = 100_000, message = "El stock mínimo no puede superar 100.000 unidades.")
        Integer stockMinimo,

        @Schema(example = "bandeja de 500g")
        @Size(max = 40, message = "La unidad admite máximo 40 caracteres.")
        String unidad,

        @Schema(description = "frutas, verduras, organicos o lacteos", example = "frutas")
        @NotBlank(message = "La categoría es obligatoria.")
        String categoria,

        @Schema(example = "Isla de Chiloé")
        @Size(max = 100, message = "El origen admite máximo 100 caracteres.")
        String origen,

        @Schema(example = "🍓")
        @Size(max = 16, message = "El emoji admite máximo 16 caracteres.")
        String emoji,

        @Schema(example = "Cultivo sin pesticidas, cosecha manual.")
        @Size(max = 500, message = "Las prácticas sostenibles admiten máximo 500 caracteres.")
        String practicasSostenibles,

        @Schema(description = "URL completa o ruta que empiece con /", example = "/img/frutillas.jpg")
        @Pattern(regexp = "^$|^(https?://|/)\\S+$", message = "La imagen debe ser una URL (http/https) o una ruta que empiece con /.")
        @Size(max = 300, message = "La URL de la imagen admite máximo 300 caracteres.")
        String imagenUrl,

        @Schema(description = "Opcional; por defecto true", example = "true")
        Boolean activo
) {
}
