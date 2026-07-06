package com.fooddelivery.common.infrastructure.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.*;

/**
 * JWT token validator using RS256 algorithm and JWKS endpoint for public key retrieval.
 * Validates signature, expiry, issuer, and audience claims.
 */
@Slf4j
@Component
public class JwtTokenValidator {

    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;
    private final String issuer;
    private final String audience;

    public JwtTokenValidator(
            @Value("${security.jwt.jwks-uri:http://localhost:8080/realms/fooddelivery/protocol/openid-connect/certs}") String jwksUri,
            @Value("${security.jwt.issuer:http://localhost:8080/realms/fooddelivery}") String issuer,
            @Value("${security.jwt.audience:food-delivery-api}") String audience) throws Exception {
        this.issuer = issuer;
        this.audience = audience;
        this.jwtProcessor = buildJwtProcessor(jwksUri);
        log.info("JWT validator initialized with JWKS URI: {}, issuer: {}, audience: {}", jwksUri, issuer, audience);
    }

    private ConfigurableJWTProcessor<SecurityContext> buildJwtProcessor(String jwksUri) throws Exception {
        ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();

        JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(new URL(jwksUri));
        JWSKeySelector<SecurityContext> keySelector =
                new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource);
        processor.setJWSKeySelector(keySelector);

        JWTClaimsSet requiredClaims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .build();
        Set<String> requiredClaimNames = Set.of("sub", "iat", "exp", "iss");
        processor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
                requiredClaims, requiredClaimNames));

        return processor;
    }

    /**
     * Validates the given JWT token and returns the claims if valid.
     * @param token the raw JWT token string (without "Bearer " prefix)
     * @return the validated JWT claims
     * @throws JwtValidationException if validation fails
     */
    public JWTClaimsSet validate(String token) {
        try {
            JWTClaimsSet claims = jwtProcessor.process(token, null);
            validateExpiry(claims);
            validateAudience(claims);
            log.debug("JWT token validated successfully for subject: {}", claims.getSubject());
            return claims;
        } catch (JwtValidationException e) {
            throw e;
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            throw new JwtValidationException("Invalid JWT token: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the subject (user ID) from a JWT token without full validation.
     * For use in logging only. Always use validate() for security decisions.
     */
    public Optional<String> extractSubject(String token) {
        try {
            JWTClaimsSet claims = validate(token);
            return Optional.ofNullable(claims.getSubject());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Extracts all claims from a validated token.
     */
    public TokenClaims extractClaims(String token) {
        JWTClaimsSet claims = validate(token);
        return new TokenClaims(
                claims.getSubject(),
                safeGetStringClaim(claims, "email"),
                safeGetStringClaim(claims, "preferred_username"),
                safeGetListClaim(claims, "roles"),
                claims.getIssuer(),
                claims.getExpirationTime(),
                claims.getIssueTime()
        );
    }

    /**
     * Returns true if the token is valid and not expired.
     */
    public boolean isValid(String token) {
        try {
            validate(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void validateExpiry(JWTClaimsSet claims) {
        Date expiration = claims.getExpirationTime();
        if (expiration == null || expiration.before(new Date())) {
            throw new JwtValidationException("JWT token has expired");
        }
    }

    private void validateAudience(JWTClaimsSet claims) {
        List<String> audiences = claims.getAudience();
        if (audiences != null && !audiences.isEmpty() && !audiences.contains(audience)) {
            throw new JwtValidationException(
                    "JWT audience validation failed. Expected: " + audience + ", got: " + audiences);
        }
    }

    private String safeGetStringClaim(JWTClaimsSet claims, String name) {
        try {
            return claims.getStringClaim(name);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> safeGetListClaim(JWTClaimsSet claims, String name) {
        try {
            Object value = claims.getClaim(name);
            if (value instanceof List<?> list) {
                return list.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .toList();
            }
            return List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Immutable record holding extracted token claims.
     */
    public record TokenClaims(
            String subject,
            String email,
            String username,
            List<String> roles,
            String issuer,
            Date expiresAt,
            Date issuedAt
    ) {
        public boolean hasRole(String role) {
            return roles != null && roles.contains(role);
        }
    }

    /**
     * Exception thrown when JWT validation fails.
     */
    public static class JwtValidationException extends RuntimeException {
        public JwtValidationException(String message) {
            super(message);
        }
        public JwtValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
