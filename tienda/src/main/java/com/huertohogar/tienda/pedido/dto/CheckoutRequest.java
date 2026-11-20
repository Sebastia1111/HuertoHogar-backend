package com.huertohogar.tienda.pedido.dto;

import com.huertohogar.tienda.catalogo.dto.ItemCantidad;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * Lo que envía tiendaApi.finalizarCompra: { correo, items: [{codigo, cantidad}], referencia, fechaEntrega }.
 * El carrito vive en el navegador (localStorage): aquí llegan sus líneas al momento de comprar.
 * Si no viene dirección, se usa la del perfil del cliente (microservicio usuarios).
 */
@Schema(description = "Compra del carrito")
public record CheckoutRequest(
        @Schema(description = "Opcional: si viene, debe ser el mismo correo del token", example = "cliente@huertohogar.cl")
        @Email(message = "Formato de correo inválido.")
        String correo,

        @NotEmpty(message = "Tu carrito está vacío.")
        @Size(max = 50, message = "Un pedido admite máximo 50 productos distintos.")
        List<@Valid ItemCantidad> items,

        @Schema(description = "Número provisorio que genera el frontend", example = "WEB-1732110000000")
        @Size(max = 60, message = "La referencia admite máximo 60 caracteres.")
        String referencia,

        @Schema(description = "Opcional, entre mañana y 30 días más. Por defecto: en 2 días", example = "2026-11-25")
        LocalDate fechaEntrega,

        @Schema(description = "Opcional; por defecto la del perfil")
        @Size(max = 300, message = "La dirección admite máximo 300 caracteres.")
        String direccionEntrega,

        @Size(max = 60, message = "La comuna admite máximo 60 caracteres.")
        String comuna,

        @Size(max = 60, message = "La región admite máximo 60 caracteres.")
        String region
) {
}
