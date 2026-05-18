package com.rhacarys.contaconjunta.domain.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.rhacarys.contaconjunta.domain.model.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    /**
     * Generates a JWT token for the authenticated user.
     * Token expires after 2 hours.
     */
    public String generateToken(User user) {
        log.debug("Generating JWT token for userId: {}", user.getId());

        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("conta-conjunta-api")
                    .withSubject(user.getLogin())
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);

            log.debug("JWT token generated successfully for login: {}", user.getLogin());
            return token;
        } catch (JWTCreationException exception) {
            log.error("Error while generating JWT token for userId: {}", user.getId(), exception);
            throw new RuntimeException("Error while generating token", exception);
        }
    }

    /**
     * Validates JWT token and returns the login (subject) if valid.
     * Returns empty string if token is invalid or expired.
     */
    public String validateToken(String token) {
        log.debug("Validating JWT token");

        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String login = JWT.require(algorithm)
                    .withIssuer("conta-conjunta-api")
                    .build()
                    .verify(token)
                    .getSubject();

            log.debug("JWT token validated successfully for login: {}", login);
            return login;
        } catch (JWTVerificationException exception) {
            log.warn("JWT token validation failed: {}", exception.getMessage());
            return "";
        }
    }

    /**
     * Generates token expiration time as current time + 2 hours (7200 seconds).
     */
    private Instant genExpirationDate() {
        return Instant.now().plusSeconds(7200);
    }
}