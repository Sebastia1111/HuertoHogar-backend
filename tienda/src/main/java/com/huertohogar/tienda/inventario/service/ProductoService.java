package com.huertohogar.tienda.inventario.service;

import com.huertohogar.tienda.inventario.model.Producto;
import com.huertohogar.tienda.inventario.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductoService {
    @Autowired 
    private ProductoRepository repository;

    public  List<Producto> obtenerTodos() {
        return repository.findAll();
    }

    public Producto guardarProducto(Producto producto) {
        return repository.save(producto);
    }

    public void eliminarProducto(Long id) {
        repository.deleteById(id);
    }
}
