package com.fooddelivery.user.application.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fooddelivery.user.domain.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TokenService {

    private final StringRedisTemplate redisTemplate;
    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;

    @Value("${jwt.access-token-expiration:900}") // 15 mins default
    private long accessTokenExpirationSeconds;

    @Value("${jwt.refresh-token-expiration:604800}") // 7 days default
    private long refreshTokenExpirationSeconds;

    public TokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            this.privateKey = (RSAPrivateKey) kp.getPrivate();
            this.publicKey = (RSAPublicKey) kp.getPublic();
            log.info("Programmatically generated RSA KeyPair for JWT signing.");
        } catch (Exception e) {
            log.error("Failed to generate RSA key pair", e);
            throw new IllegalStateException("RSA initialization failed", e);
        }
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer("food-delivery-super-platform")
                .withSubject(user.getId().getValue().toString())
                .withClaim("email", user.getEmail())
                .withClaim("roles", user.getRoles().stream().map(Enum::name).collect(Collectors.toList()))
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(accessTokenExpirationSeconds)))
                .sign(Algorithm.RSA256(publicKey, privateKey));
    }

    public String generateRefreshToken(User user) {
        String refreshToken = UUID.randomUUID().toString();
        String key = "jwt:refresh:" + refreshToken;
        redisTemplate.opsForValue().set(
                key,
                user.getId().getValue().toString(),
                refreshTokenExpirationSeconds,
                TimeUnit.SECONDS
        );
        return refreshToken;
    }

    public String validateRefreshTokenAndGetUserId(String refreshToken) {
        String key = "jwt:refresh:" + refreshToken;
        return redisTemplate.opsForValue().get(key);
    }

    public void invalidateRefreshToken(String refreshToken) {
        String key = "jwt:refresh:" + refreshToken;
        redisTemplate.delete(key);
    }
}
