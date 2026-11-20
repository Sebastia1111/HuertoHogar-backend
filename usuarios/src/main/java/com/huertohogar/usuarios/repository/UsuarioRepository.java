package com.huertohogar.usuarios.repository;

import com.huertohogar.usuarios.model.Rol;
import com.huertohogar.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByRun(String run);

    List<Usuario> findByRol(Rol rol);
}
