package com.rhacarys.contaconjunta.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rhacarys.contaconjunta.api.dto.LoginRequest;
import com.rhacarys.contaconjunta.api.dto.RegisterRequest;
import com.rhacarys.contaconjunta.domain.exception.BusinessException;
import com.rhacarys.contaconjunta.domain.model.User;
import com.rhacarys.contaconjunta.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user with the provided credentials.
     * Validates that login is unique before creating the account.
     */
    @Transactional
    public void register(RegisterRequest data) {
        logger.debug("Starting user registration for login: {}", data.login());
        
        if (userRepository.existsByLogin(data.login())) {
            logger.warn("Registration failed - login already exists: {}", data.login());
            throw new BusinessException("Login already exists", HttpStatus.CONFLICT);
        }

        String encryptedPassword = passwordEncoder.encode(data.password());

        User newUser = new User();
        newUser.setName(data.name());
        newUser.setLogin(data.login());
        newUser.setPassword(encryptedPassword);

        User savedUser = userRepository.save(newUser);
        logger.info("User registered successfully - userId: {}, login: {}", savedUser.getId(), data.login());
    }

    /**
     * Authenticates a user with login credentials and returns a JWT token.
     */
    public String login(LoginRequest data) {
        logger.debug("Login attempt for login: {}", data.login());
        
        var user = userRepository.findByLogin(data.login())
                .orElseThrow(() -> {
                    logger.warn("Login failed - user not found: {}", data.login());
                    return new BusinessException("Invalid login or password", HttpStatus.UNAUTHORIZED);
                });

        if (!passwordEncoder.matches(data.password(), user.getPassword())) {
            logger.warn("Login failed - invalid password for login: {}", data.login());
            throw new BusinessException("Invalid login or password", HttpStatus.UNAUTHORIZED);
        }

        String token = tokenService.generateToken(user);
        logger.info("User login successful - userId: {}, login: {}", user.getId(), data.login());
        return token;
    }
}