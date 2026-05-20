package com.rhacarys.racharcontas.domain.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.rhacarys.racharcontas.api.dto.UserUpdateRequest;
import com.rhacarys.racharcontas.domain.exception.BusinessException;
import com.rhacarys.racharcontas.domain.model.User;
import com.rhacarys.racharcontas.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void updateMe_ShouldThrowException_WhenCurrentPasswordDoesNotMatch() {
        User loggedUser = new User();
        loggedUser.setId(UUID.randomUUID());

        User userFromDb = new User();
        userFromDb.setPassword("encoded_password");

        UserUpdateRequest request = new UserUpdateRequest("Nathaniel", "wrong_password", "new_password");

        when(userRepository.findById(loggedUser.getId())).thenReturn(Optional.of(userFromDb));
        when(passwordEncoder.matches("wrong_password", "encoded_password")).thenReturn(false);

        assertThrows(BusinessException.class, () -> userService.updateMe(request, loggedUser));
    }
}