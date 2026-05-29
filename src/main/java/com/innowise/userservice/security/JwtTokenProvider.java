package com.innowise.userservice.security;

import com.innowise.userservice.config.jwt.JwtSecrets;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private final SecretKey key;

  public JwtTokenProvider(JwtSecrets jwtSecrets) {
    this.key = Keys.hmacShaKeyFor(jwtSecrets.secret().getBytes(StandardCharsets.UTF_8));
  }

  public Claims validateToken(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}