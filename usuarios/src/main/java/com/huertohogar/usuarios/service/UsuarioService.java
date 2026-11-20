package com.huertohogar.usuarios.service;

import com.huertohogar.usuarios.dto.CambioPasswordRequest;
import com.huertohogar.usuarios.dto.PerfilUpdateRequest;
import com.huertohogar.usuarios.dto.UsuarioAdminRequest;
import com.huertohogar.usuarios.dto.UsuarioResponse;
import com.huertohogar.usuarios.exception.RecursoNoEncontradoException;
import com.huertohogar.usuarios.exception.ReglaNegocioException;
import com.huertohogar.usuarios.model.Rol;
import com.huertohogar.usuarios.model.Usuario;
import com.huertohogar.usuarios.repository.UsuarioRepository;
import com.huertohogar.usuarios.validation.RunValidator;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------- Perfil propio ----------

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPerfil(Long id) {
        return UsuarioResponse.desde(buscar(id));
    }

    @Transactional
    public UsuarioResponse actualizarPerfil(Long id, PerfilUpdateRequest req) {
        Usuario u = buscar(id);
        u.setNombre(req.nombre().trim());
        u.setApellidos(req.apellidos());
        u.setFechaNacimiento(req.fechaNacimiento());
        u.setRegion(req.region());
        u.setComuna(req.comuna());
        u.setDireccion(req.direccion());
        u.setTelefono(req.telefono());
        return UsuarioResponse.desde(u);
    }

    @Transactional
    public void cambiarPassword(Long id, CambioPasswordRequest req) {
        Usuario u = buscar(id);
        if (!passwordEncoder.matches(req.claveActual(), u.getPassword())) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "La contraseña actual no es correcta",
                    Map.of("claveActual", "La contraseña actual no es correcta"));
        }
        u.setPassword(passwordEncoder.encode(req.claveNueva()));
        // Por seguridad se cierran todas las sesiones: hay que volver a iniciar sesion
        u.setTokenVersion(u.getTokenVersion() + 1);
    }

    // ---------- Gestion (ADMIN) ----------

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(Rol rol) {
        List<Usuario> usuarios = (rol == null) ? usuarioRepository.findAll() : usuarioRepository.findByRol(rol);
        return usuarios.stream().map(UsuarioResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.desde(buscar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioAdminRequest req) {
        if (req.clave() == null || req.clave().isBlank()) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST,
                    "La contraseña es obligatoria y debe tener entre 4 y 10 caracteres");
        }
        String correo = req.correo().trim().toLowerCase();
        String run = RunValidator.normalizar(req.run());
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo");
        }
        if (usuarioRepository.existsByRun(run)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese RUN");
        }
        Usuario u = new Usuario();
        u.setRun(run);
        u.setCorreo(correo);
        u.setPassword(passwordEncoder.encode(req.clave()));
        u.setActivo(req.activo() == null || req.activo());
        copiarDatos(req, u);
        return UsuarioResponse.desde(usuarioRepository.save(u));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioAdminRequest req, Long idAdminActual) {
        Usuario u = buscar(id);
        String correo = req.correo().trim().toLowerCase();
        String run = RunValidator.normalizar(req.run());

        if (!u.getCorreo().equalsIgnoreCase(correo) && usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo");
        }
        if (!u.getRun().equals(run) && usuarioRepository.existsByRun(run)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese RUN");
        }
        boolean esElMismo = u.getId().equals(idAdminActual);
        if (esElMismo && (req.rol() != Rol.ADMIN || Boolean.FALSE.equals(req.activo()))) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST,
                    "No puedes quitarte el rol ADMIN ni desactivar tu propia cuenta");
        }

        u.setRun(run);
        u.setCorreo(correo);
        if (req.clave() != null && !req.clave().isBlank()) {
            u.setPassword(passwordEncoder.encode(req.clave()));
            u.setTokenVersion(u.getTokenVersion() + 1);
        }
        if (req.activo() != null) {
            u.setActivo(req.activo());
            if (!req.activo()) {
                u.setTokenVersion(u.getTokenVersion() + 1);
            }
        }
        copiarDatos(req, u);
        return UsuarioResponse.desde(u);
    }

    @Transactional
    public void eliminar(Long id, Long idAdminActual) {
        if (id.equals(idAdminActual)) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "No puedes eliminar tu propia cuenta");
        }
        Usuario u = buscar(id);
        usuarioRepository.delete(u);
    }

    private void copiarDatos(UsuarioAdminRequest req, Usuario u) {
        u.setNombre(req.nombre().trim());
        u.setApellidos(req.apellidos());
        u.setFechaNacimiento(req.fechaNacimiento());
        u.setRegion(req.region());
        u.setComuna(req.comuna());
        u.setDireccion(req.direccion());
        u.setTelefono(req.telefono());
        u.setRol(req.rol());
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario " + id + " no encontrado"));
    }
}
