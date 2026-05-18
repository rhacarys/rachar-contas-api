package com.rhacarys.contaconjunta.domain.service;

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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user with the provided credentials.
     * Validates that login is unique before creating the account.
     */
    @Transactional
    public void register(RegisterRequest data) {
        log.debug("Starting user registration for login: {}", data.login());

        if (userRepository.existsByLogin(data.login())) {
            log.warn("Registration failed - login already exists: {}", data.login());
            throw new BusinessException("Login already exists", HttpStatus.CONFLICT);
        }

        String encryptedPassword = passwordEncoder.encode(data.password());

        User newUser = new User();
        newUser.setName(data.name());
        newUser.setLogin(data.login());
        newUser.setPassword(encryptedPassword);

        User savedUser = userRepository.save(newUser);
        log.info("User registered successfully - userId: {}, login: {}", savedUser.getId(), data.login());
    }

    /**
     * Authenticates a user with login credentials and returns a JWT token.
     */
    public String login(LoginRequest data) {
        log.debug("Login attempt for login: {}", data.login());

        var user = userRepository.findByLogin(data.login())
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found: {}", data.login());
                    return new BusinessException("Invalid login or password", HttpStatus.UNAUTHORIZED);
                });

        if (!passwordEncoder.matches(data.password(), user.getPassword())) {
            log.warn("Login failed - invalid password for login: {}", data.login());
            throw new BusinessException("Invalid login or password", HttpStatus.UNAUTHORIZED);
        }

        String token = tokenService.generateToken(user);
        log.info("User login successful - userId: {}, login: {}", user.getId(), data.login());
        return token;
    }
}