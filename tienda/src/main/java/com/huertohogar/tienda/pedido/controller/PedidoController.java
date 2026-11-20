package com.huertohogar.tienda.pedido.controller;

import com.huertohogar.tienda.common.Acceso;
import com.huertohogar.tienda.pedido.dto.CambioEstadoRequest;
import com.huertohogar.tienda.pedido.dto.CheckoutRequest;
import com.huertohogar.tienda.pedido.dto.CheckoutResponse;
import com.huertohogar.tienda.pedido.dto.PedidoResponse;
import com.huertohogar.tienda.pedido.model.EstadoPedido;
import com.huertohogar.tienda.pedido.service.PedidoService;
import com.huertohogar.tienda.pedido.client.UsuariosClient;
import com.huertohogar.tienda.pedido.client.UsuariosClient.PerfilEntrega;
import com.huertohogar.tienda.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tienda")
@Tag(name = "2. Compras y pedidos", description = "Comprar, ver boletas y seguir el estado de los pedidos. Requiere token")
public class PedidoController {

    private final PedidoService service;
    private final UsuariosClient usuariosClient;

    public PedidoController(PedidoService service, UsuariosClient usuariosClient) {
        this.service = service;
        this.usuariosClient = usuariosClient;
    }

    @PostMapping("/carrito/checkout")
    @Operation(summary = "Finalizar compra · con token",
            description = "Crea la boleta y descuenta el stock en una sola transacción: si falta stock de un producto, "
                    + "no se guarda nada (409). Los correos @duocuc.cl reciben 20% de descuento.")
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest req,
                                                     @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                     @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado yo) {
        Acceso.verificarCorreo(yo, req.correo());
        // Fuera de la transacción: pide a usuarios (Eureka o dirección de respaldo) la dirección del perfil
        PerfilEntrega perfil = (req.direccionEntrega() == null || req.direccionEntrega().isBlank())
                ? usuariosClient.perfil(authorization).orElse(null)
                : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(service.checkout(yo, req, perfil));
    }

    @GetMapping("/pedidos")
    @Operation(summary = "Mis pedidos · con token", description = "Historial de compras del dueño del token, del más nuevo al más antiguo. "
            + "Un ADMIN puede ver el de otro cliente con `?correo=`.")
    public List<PedidoResponse> misPedidos(@RequestParam(required = false) String correo,
                                           @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado yo) {
        return service.misPedidos(yo, correo);
    }

    @GetMapping("/pedidos/todos")
    @Operation(summary = "Todos los pedidos · ADMIN o VENDEDOR", description = "Filtro opcional, ej: `?estado=PENDIENTE`")
    public List<PedidoResponse> todos(@RequestParam(required = false) EstadoPedido estado) {
        return service.todos(estado);
    }

    @GetMapping("/pedidos/{numero}")
    @Operation(summary = "Ver boleta y seguimiento · con token", description = "Un cliente solo ve sus propios pedidos.")
    public PedidoResponse obtener(@Parameter(example = "HH-1001") @PathVariable String numero,
                                  @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado yo) {
        return service.obtener(numero, yo);
    }

    @PatchMapping("/pedidos/{numero}/estado")
    @Operation(summary = "Cambiar estado del pedido · ADMIN o VENDEDOR",
            description = "Solo avanza: PENDIENTE → EN_PREPARACION → EN_CAMINO → ENTREGADO. "
                    + "CANCELADO (antes del despacho) devuelve el stock.")
    public PedidoResponse cambiarEstado(@PathVariable String numero, @Valid @RequestBody CambioEstadoRequest req,
                                        @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado yo) {
        return service.cambiarEstado(numero, req.estado(), req.comentario(), yo);
    }

    @PostMapping("/pedidos/{numero}/cancelar")
    @Operation(summary = "Cancelar mi pedido · con token", description = "Solo mientras está PENDIENTE. Devuelve el stock.")
    public PedidoResponse cancelar(@PathVariable String numero,
                                   @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado yo) {
        return service.cancelar(numero, yo);
    }
}
