package com.huertohogar.usuarios;

import com.huertohogar.usuarios.model.Rol;
import com.huertohogar.usuarios.model.Usuario;
import com.huertohogar.usuarios.security.JwtService;
import com.huertohogar.usuarios.security.UsuarioAutenticado;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRETO = "MEwreeJ2WVKjMTo6eMI6B3KPoHvDFj1T536DgWsgtxzryJ0wUsUNbSYdV+LN2GiT";
    private static final String OTRO_SECRETO = "c2VjcmV0by1kaWZlcmVudGUtcGFyYS1wcm9iYXItZmlybWEtaW52YWxpZGEtMTIzNDU2";

    private Usuario usuario() {
        Usuario u = new Usuario();
        u.setId(7L);
        u.setCorreo("cliente@gmail.com");
        u.setNombre("Cliente");
        u.setRol(Rol.CLIENTE);
        return u;
    }

    @Test
    void generaYValidaToken() {
        JwtService jwt = new JwtService(SECRETO, "huertohogar-usuarios", 60_000, 7);
        UsuarioAutenticado datos = jwt.validar(jwt.generarAccessToken(usuario()));
        assertEquals(7L, datos.id());
        assertEquals("cliente@gmail.com", datos.correo());
        assertEquals("CLIENTE", datos.rol());
    }

    @Test
    void rechazaTokenFirmadoConOtraClave() {
        String token = new JwtService(OTRO_SECRETO, "huertohogar-usuarios", 60_000, 7).generarAccessToken(usuario());
        JwtService jwt = new JwtService(SECRETO, "huertohogar-usuarios", 60_000, 7);
        assertThrows(JwtException.class, () -> jwt.validar(token));
    }

    @Test
    void rechazaTokenExpirado() {
        JwtService jwt = new JwtService(SECRETO, "huertohogar-usuarios", -1_000, 7);
        String token = jwt.generarAccessToken(usuario());
        assertThrows(ExpiredJwtException.class, () -> jwt.validar(token));
    }

    @Test
    void refreshYAccessNoSonIntercambiables() {
        JwtService jwt = new JwtService(SECRETO, "huertohogar-usuarios", 60_000, 7);
        String refresh = jwt.generarRefreshToken(usuario());
        String access = jwt.generarAccessToken(usuario());

        assertEquals(7L, jwt.validarRefresh(refresh).usuarioId());
        assertThrows(JwtException.class, () -> jwt.validar(refresh));
        assertThrows(JwtException.class, () -> jwt.validarRefresh(access));
    }
}
