package com.aratechmoveis.login.security;

import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    @Value("${jwt.chave-secreta}")
    private String JWT_CHAVE_SECRETA;

    @Value("${jwt.tempo-expiracao}")
    private long JWT_TEMPO_EXPIRACAO;

    private SecretKey chave;

    @PostConstruct
    private void init() {
        byte[] keyByte = JWT_CHAVE_SECRETA.getBytes(StandardCharsets.UTF_8);
        this.chave = new SecretKeySpec(keyByte, "HmacSHA256");
    }

    public String gerarToken(String email, List<String> perfis) {
        return Jwts.builder()
                .subject(email)
                .claim("perfis", perfis)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + JWT_TEMPO_EXPIRACAO))
                .signWith(chave)
                .compact();
    }
}
