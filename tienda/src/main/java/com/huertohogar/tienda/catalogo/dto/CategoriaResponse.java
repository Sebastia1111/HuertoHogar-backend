package com.huertohogar.tienda.catalogo.dto;

import com.huertohogar.tienda.catalogo.model.Categoria;

public record CategoriaResponse(Long id, String slug, String codigo, String nombre, String descripcion) {

    public static CategoriaResponse desde(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getSlug(), c.getCodigo(), c.getNombre(), c.getDescripcion());
    }
}
