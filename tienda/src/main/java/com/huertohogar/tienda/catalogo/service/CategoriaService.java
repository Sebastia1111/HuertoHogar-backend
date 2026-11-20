package com.huertohogar.tienda.catalogo.service;

import com.huertohogar.tienda.catalogo.dto.CategoriaRequest;
import com.huertohogar.tienda.catalogo.dto.CategoriaResponse;
import com.huertohogar.tienda.catalogo.model.Categoria;
import com.huertohogar.tienda.catalogo.repository.CategoriaRepository;
import com.huertohogar.tienda.catalogo.repository.ProductoCatalogoRepository;
import com.huertohogar.tienda.common.RecursoNoEncontradoException;
import com.huertohogar.tienda.common.ReglaNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoCatalogoRepository catalogoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ProductoCatalogoRepository catalogoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.catalogoRepository = catalogoRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll().stream().map(CategoriaResponse::desde).toList();
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest req) {
        validarUnicos(req, null);
        Categoria c = new Categoria(null, req.slug().trim().toLowerCase(), req.codigo().trim().toUpperCase(),
                req.nombre().trim(), req.descripcion());
        return CategoriaResponse.desde(categoriaRepository.save(c));
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest req) {
        Categoria c = buscar(id);
        validarUnicos(req, c);
        c.setSlug(req.slug().trim().toLowerCase());
        c.setCodigo(req.codigo().trim().toUpperCase());
        c.setNombre(req.nombre().trim());
        c.setDescripcion(req.descripcion());
        return CategoriaResponse.desde(c);
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria c = buscar(id);
        if (catalogoRepository.existsByCategoriaId(id)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "No se puede eliminar: la categoría tiene productos asociados");
        }
        categoriaRepository.delete(c);
    }

    private void validarUnicos(CategoriaRequest req, Categoria actual) {
        if ((actual == null || !actual.getSlug().equalsIgnoreCase(req.slug()))
                && categoriaRepository.existsBySlugIgnoreCase(req.slug())) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "Ya existe una categoría con ese slug");
        }
        if ((actual == null || !actual.getCodigo().equalsIgnoreCase(req.codigo()))
                && categoriaRepository.existsByCodigoIgnoreCase(req.codigo())) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "Ya existe una categoría con ese código");
        }
        if ((actual == null || !actual.getNombre().equalsIgnoreCase(req.nombre().trim()))
                && categoriaRepository.existsByNombreIgnoreCase(req.nombre().trim())) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre");
        }
    }

    private Categoria buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría " + id + " no encontrada"));
    }
}
