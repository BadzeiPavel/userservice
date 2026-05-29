package com.innowise.userservice.config.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtSecrets(
    String secret,
    long accessTokenExpiration,
    long refreshTokenExpiration
) {

}