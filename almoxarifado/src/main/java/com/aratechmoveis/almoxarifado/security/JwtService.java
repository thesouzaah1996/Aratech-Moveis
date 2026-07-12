package com.aratechmoveis.almoxarifado.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.chave-secreta}")
    private String JWT_CHAVE_SECRETA;

    private SecretKey chave;

    @PostConstruct
    private void init() {
        byte[] keyByte = JWT_CHAVE_SECRETA.getBytes(StandardCharsets.UTF_8);
        this.chave = new SecretKeySpec(keyByte, "HmacSHA256");
    }

    public <T> T extrairClaims(String token, Function<Claims, T> claimsTFunction) {
        return claimsTFunction.apply(Jwts.parser().verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload());
    }

    public String extrairEmail(String token) {
        return extrairClaims(token, claims -> claims.getSubject());
    }

    public List<String> extrairPerfis(String token) {
        return extrairClaims(token, claims -> claims.get("perfis", List.class));
    }

    public boolean tokenExpirado(String token) {
        return extrairClaims(token, Claims::getExpiration).before(new Date());
    }

    public boolean tokenValido(String token) {
        try {
            return !tokenExpirado(token);
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("Erro ao validar o token: " +  e.getMessage());
            return false;
        }
    }
}
