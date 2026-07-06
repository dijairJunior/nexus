package br.com.waps.nexus.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {
    private static final String SECRET = "nexus-waps-chave-secreta-minimo-256-bit-aqui";
    private static final long EXPIRACAO_MS = 1000 * 60 * 60 * 8;

    private SecretKey getChave() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String gerarToker(String login) {
        return Jwts.builder()
                .subject(login)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRACAO_MS))
                .signWith(getChave())
                .compact();
    }

    public String extrairLogin(String token) {
        return extrairClaims(token).getSubject();
    }

    public boolean tokenValido(String token, UserDetails userDetails) {
        String Login = extrairLogin(token);
        return Login.equals(userDetails.getUsername()) && !tokenExpirado(token);
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(getChave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean tokenExpirado(String token) {
        return extrairClaims(token).getExpiration().before(new Date());
    }

    public String renovarToken(String tokenAtual) {
        String login = extrairLogin(tokenAtual); // valida assinatura + extrai login
        return gerarToker(login); // gera novo token com nova expiração
    }
}