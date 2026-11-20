package com.huertohogar.usuarios.config;

import com.huertohogar.usuarios.model.Rol;
import com.huertohogar.usuarios.model.Usuario;
import com.huertohogar.usuarios.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cuentas de prueba (las mismas que usa el frontend).
 * Solo se crean si la tabla esta vacia.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public DataLoader(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) {
            return;
        }
        // Mismas cuentas que documenta el frontend (README): clave 12345
        repo.save(crear("111111111", "Admin", "HuertoHogar", "admin@huertohogar.cl", "12345", Rol.ADMIN));
        repo.save(crear("222222222", "Vendedor", "HuertoHogar", "vendedor@huertohogar.cl", "12345", Rol.VENDEDOR));
        repo.save(crear("190110222", "Cliente", "Demo", "cliente@huertohogar.cl", "12345", Rol.CLIENTE));
        log.info("Usuarios de prueba creados (clave 12345): admin@huertohogar.cl, vendedor@huertohogar.cl, cliente@huertohogar.cl");
    }

    private Usuario crear(String run, String nombre, String apellidos, String correo, String pass, Rol rol) {
        return Usuario.builder()
                .run(run).nombre(nombre).apellidos(apellidos).correo(correo)
                .password(encoder.encode(pass))
                .region("Región Metropolitana").comuna("Santiago")
                .direccion("Av. Siempre Viva 123").telefono("+56912345678")
                .rol(rol).activo(true).tokenVersion(0)
                .build();
    }
}
