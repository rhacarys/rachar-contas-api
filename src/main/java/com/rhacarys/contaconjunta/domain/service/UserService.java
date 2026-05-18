package com.rhacarys.contaconjunta.domain.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rhacarys.contaconjunta.api.dto.PartyBalanceResponse;
import com.rhacarys.contaconjunta.api.dto.UserResponse;
import com.rhacarys.contaconjunta.api.dto.UserUpdateRequest;
import com.rhacarys.contaconjunta.domain.exception.BusinessException;
import com.rhacarys.contaconjunta.domain.model.Membership;
import com.rhacarys.contaconjunta.domain.model.User;
import com.rhacarys.contaconjunta.domain.repository.MembershipRepository;
import com.rhacarys.contaconjunta.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final BalanceService balanceService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse updateMe(UserUpdateRequest request, User loggedUser) {
        User user = getUserById(loggedUser.getId());

        user.setName(request.name());
        updatePasswordIfNeeded(user, request);

        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Transactional
    public void deleteMe(User loggedUser) {
        User user = getUserById(loggedUser.getId());
        List<Membership> memberships = membershipRepository.findByUserId(user.getId());

        validateNoPendingBalances(user, memberships);
        anonymizeAccount(user, memberships);

        userRepository.save(user);
    }

    private User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
    }

    private void updatePasswordIfNeeded(User user, UserUpdateRequest request) {
        if (request.newPassword() == null || request.newPassword().isBlank()) {
            return;
        }

        if (request.currentPassword() == null || request.currentPassword().isBlank()) {
            throw new BusinessException("Current password is required to set a new one", HttpStatus.BAD_REQUEST);
        }

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException("Current password does not match", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }

    private void validateNoPendingBalances(User user, List<Membership> memberships) {
        for (Membership membership : memberships) {
            PartyBalanceResponse balancesResponse = balanceService.calculateBalances(membership.getParty().getId(),
                    user);

            boolean hasPendingBalance = balancesResponse.balances().stream()
                    .filter(b -> b.membershipId().equals(membership.getId()))
                    .anyMatch(b -> b.balance().compareTo(BigDecimal.ZERO) != 0);

            if (hasPendingBalance) {
                throw new BusinessException(
                        "Cannot delete account. You have a non-zero balance in the party: "
                                + membership.getParty().getName(),
                        HttpStatus.CONFLICT);
            }
        }
    }

    private void anonymizeAccount(User user, List<Membership> memberships) {
        user.setName("Deleted User");
        user.setLogin("deleted-" + user.getId());
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        memberships.forEach(membership -> membership.setAlias("Deleted User"));
    }
}