package com.huertohogar.tienda.controller;

import com.huertohogar.tienda.model.Producto;
import com.huertohogar.tienda.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@CrossOrigin(origins = "*")
@Tag(name = "inventario", description = "Endpoints para la gestión de productos en el inventario")
public class ProductoController {

    @Autowired
    private ProductoService service;

    @GetMapping
    @Operation(summary = "Obtener todos los productos", description = "Devuelve una lista de todos los productos en el inventario")
    public List<Producto> obtenerTodos() {
        return service.obtenerTodos();
    }

    @PostMapping
    @Operation(summary = "Agregar un nuevo producto", description = "Agrega un nuevo producto al inventario")
    public Producto agregarProducto(@RequestBody Producto producto) {
        return service.guardarProducto(producto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un producto", description = "Elimina un producto del inventario por su ID")
    public void eliminarProducto(@PathVariable Long id) {
        service.eliminarProducto(id);
    }

}
