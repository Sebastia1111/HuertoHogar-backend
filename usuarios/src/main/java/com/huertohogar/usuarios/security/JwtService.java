package com.huertohogar.usuarios.security;

import com.huertohogar.usuarios.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * Genera y verifica los tokens JWT (firmados con HMAC-SHA).
 * - access token (15 min): se envia en cada peticion. La tienda lo verifica con la misma clave.
 * - refresh token (7 dias): solo sirve en /api/auth/refresh para obtener tokens nuevos.
 * Ninguno se guarda en la base de datos: al cerrar sesion se sube la "version" del usuario
 * (claim "ver") y todos los tokens anteriores dejan de ser aceptados.
 */
@Service
public class JwtService {

    public static final String CLAIM_UID = "uid";
    public static final String CLAIM_ROL = "rol";
    public static final String CLAIM_NOMBRE = "nombre";
    public static final String CLAIM_VERSION = "ver";
    public static final String CLAIM_TIPO = "tipo";
    public static final String TIPO_ACCESS = "access";
    public static final String TIPO_REFRESH = "refresh";

    /** Datos que trae un refresh token. */
    public record DatosRefresh(Long usuarioId, int version) {
    }

    private final SecretKey clave;
    private final String issuer;
    private final long expiracionMs;
    private final long refreshExpiracionMs;

    public JwtService(@Value("${jwt.secret}") String secretoBase64,
                      @Value("${jwt.issuer}") String issuer,
                      @Value("${jwt.expiracion-ms}") long expiracionMs,
                      @Value("${jwt.refresh-expiracion-dias}") long refreshDias) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretoBase64));
        this.issuer = issuer;
        this.expiracionMs = expiracionMs;
        this.refreshExpiracionMs = refreshDias * 24L * 60 * 60 * 1000;
    }

    public String generarAccessToken(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getCorreo())
                .issuer(issuer)
                .claim(CLAIM_TIPO, TIPO_ACCESS)
                .claim(CLAIM_UID, usuario.getId())
                .claim(CLAIM_ROL, usuario.getRol().name())
                .claim(CLAIM_NOMBRE, usuario.getNombre())
                .claim(CLAIM_VERSION, usuario.getTokenVersion())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)
                .compact();
    }

    public String generarRefreshToken(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(usuario.getCorreo())
                .issuer(issuer)
                .claim(CLAIM_TIPO, TIPO_REFRESH)
                .claim(CLAIM_UID, usuario.getId())
                .claim(CLAIM_VERSION, usuario.getTokenVersion())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + refreshExpiracionMs))
                .signWith(clave)
                .compact();
    }

    /**
     * Verifica un access token: firma, emisor, expiracion y que no sea un refresh token.
     * Lanza ExpiredJwtException si expiro o JwtException si es invalido.
     */
    public UsuarioAutenticado validar(String token) throws JwtException {
        Claims claims = leer(token, TIPO_ACCESS);
        Number uid = claims.get(CLAIM_UID, Number.class);
        String rol = claims.get(CLAIM_ROL, String.class);
        if (uid == null || rol == null) {
            throw new JwtException("Token sin datos de usuario");
        }
        return new UsuarioAutenticado(uid.longValue(), claims.getSubject(), rol, version(claims));
    }

    /** Verifica un refresh token (lanza JwtException si es invalido, expiro o es un access token). */
    public DatosRefresh validarRefresh(String token) throws JwtException {
        Claims claims = leer(token, TIPO_REFRESH);
        Number uid = claims.get(CLAIM_UID, Number.class);
        if (uid == null) {
            throw new JwtException("Token sin datos de usuario");
        }
        return new DatosRefresh(uid.longValue(), version(claims));
    }

    public long getExpiracionSegundos() {
        return expiracionMs / 1000;
    }

    private Claims leer(String token, String tipoEsperado) {
        Claims claims = Jwts.parser()
                .verifyWith(clave)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        if (!tipoEsperado.equals(claims.get(CLAIM_TIPO, String.class))) {
            throw new JwtException("Tipo de token incorrecto");
        }
        return claims;
    }

    private static int version(Claims claims) {
        Number version = claims.get(CLAIM_VERSION, Number.class);
        return version == null ? 0 : version.intValue();
    }
}
