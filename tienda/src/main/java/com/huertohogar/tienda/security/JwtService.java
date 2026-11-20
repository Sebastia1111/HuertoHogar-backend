package com.huertohogar.tienda.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

/**
 * La tienda NO emite tokens: solo verifica los que firma el microservicio usuarios
 * (misma clave HMAC compartida). Asi no necesita llamar a usuarios en cada peticion.
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final String issuer;

    public JwtService(@Value("${jwt.secret}") String secretoBase64,
                      @Value("${jwt.issuer}") String issuer) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretoBase64));
        this.issuer = issuer;
    }

    public UsuarioAutenticado validar(String token) throws JwtException {
        Claims claims = Jwts.parser()
                .verifyWith(clave)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        // Un refresh token solo sirve en usuarios (/api/auth/refresh), nunca para comprar o administrar
        if ("refresh".equals(claims.get("tipo", String.class))) {
            throw new JwtException("Se envió un refresh token en lugar de un access token");
        }
        Number uid = claims.get("uid", Number.class);
        String rol = claims.get("rol", String.class);
        if (uid == null || rol == null) {
            throw new JwtException("Token sin datos de usuario");
        }
        return new UsuarioAutenticado(uid.longValue(), claims.getSubject(), rol,
                claims.get("nombre", String.class));
    }
}
