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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterRequest data) {
        if (userRepository.existsByLogin(data.login())) {
            throw new BusinessException("Login already exists", HttpStatus.CONFLICT);
        }

        String encryptedPassword = passwordEncoder.encode(data.password());

        User newUser = new User();
        newUser.setName(data.name());
        newUser.setLogin(data.login());
        newUser.setPassword(encryptedPassword);

        userRepository.save(newUser);
    }

    public String login(LoginRequest data) {
        var user = userRepository.findByLogin(data.login())
                .orElseThrow(() -> new BusinessException("Invalid login or password", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(data.password(), user.getPassword())) {
            throw new BusinessException("Invalid login or password", HttpStatus.UNAUTHORIZED);
        }

        return tokenService.generateToken(user);
    }
}