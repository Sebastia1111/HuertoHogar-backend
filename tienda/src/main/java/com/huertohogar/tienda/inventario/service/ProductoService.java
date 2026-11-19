<<<<<<< HEAD
package com.huertohogar.tienda.service;

import com.huertohogar.tienda.model.Producto;
import com.huertohogar.tienda.repository.ProductoRepository;
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
=======
package com.huertohogar.tienda.service;

import com.huertohogar.tienda.model.Producto;
import com.huertohogar.tienda.repository.ProductoRepository;
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
>>>>>>> b065651850294b39a3182b1afc518b359affaf29
