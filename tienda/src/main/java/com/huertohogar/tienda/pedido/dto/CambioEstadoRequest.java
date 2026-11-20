package com.huertohogar.tienda.pedido.dto;

import com.huertohogar.tienda.pedido.model.EstadoPedido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CambioEstadoRequest(
        @Schema(description = "PENDIENTE -> EN_PREPARACION -> EN_CAMINO -> ENTREGADO, o CANCELADO", example = "EN_PREPARACION")
        @NotNull(message = "El estado es obligatorio.")
        EstadoPedido estado,

        @Schema(example = "Pedido armado en bodega")
        @Size(max = 200, message = "El comentario admite máximo 200 caracteres.")
        String comentario
) {
}
