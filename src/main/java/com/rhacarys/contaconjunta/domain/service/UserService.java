package com.rhacarys.contaconjunta.domain.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final BalanceService balanceService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Updates the current user's profile information and password.
     */
    @Transactional
    public UserResponse updateMe(UserUpdateRequest request, User loggedUser) {
        logger.debug("Starting user profile update for userId: {}", loggedUser.getId());
        
        User user = getUserById(loggedUser.getId());
        user.setName(request.name());
        updatePasswordIfNeeded(user, request);

        User updatedUser = userRepository.save(user);
        logger.info("User profile updated successfully - userId: {}, newName: {}", 
            loggedUser.getId(), request.name());
        
        return UserResponse.fromEntity(updatedUser);
    }

    /**
     * Deletes the user account after validating zero balance across all parties.
     * Account is anonymized rather than fully deleted to preserve financial history.
     */
    @Transactional
    public void deleteMe(User loggedUser) {
        logger.debug("Starting account deletion for userId: {}", loggedUser.getId());
        
        User user = getUserById(loggedUser.getId());
        List<Membership> memberships = membershipRepository.findByUserId(user.getId());

        validateNoPendingBalances(user, memberships);
        anonymizeAccount(user, memberships);

        userRepository.save(user);
        logger.info("User account deleted (anonymized) - userId: {}, login: {}", 
            user.getId(), user.getLogin());
    }

    private User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
    }

    /**
     * Updates user password after validating current password matches.
     * Password update is optional - if not provided, this method returns early.
     */
    private void updatePasswordIfNeeded(User user, UserUpdateRequest request) {
        if (request.newPassword() == null || request.newPassword().isBlank()) {
            logger.debug("No password update requested for userId: {}", user.getId());
            return;
        }

        if (request.currentPassword() == null || request.currentPassword().isBlank()) {
            logger.warn("Password update attempted without current password for userId: {}", user.getId());
            throw new BusinessException("Current password is required to set a new one", HttpStatus.BAD_REQUEST);
        }

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            logger.warn("Password update failed - current password mismatch for userId: {}", user.getId());
            throw new BusinessException("Current password does not match", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        logger.info("User password updated successfully - userId: {}", user.getId());
    }

    /**
     * Validates that user has zero balance in all parties before allowing account deletion.
     * Throws exception if any pending balances exist.
     */
    private void validateNoPendingBalances(User user, List<Membership> memberships) {
        logger.debug("Validating zero balance across {} memberships for userId: {}", 
            memberships.size(), user.getId());
        
        for (Membership membership : memberships) {
            PartyBalanceResponse balancesResponse = balanceService.calculateBalances(membership.getParty().getId(),
                    user);

            boolean hasPendingBalance = balancesResponse.balances().stream()
                    .filter(b -> b.membershipId().equals(membership.getId()))
                    .anyMatch(b -> b.balance().compareTo(BigDecimal.ZERO) != 0);

            if (hasPendingBalance) {
                logger.warn("Account deletion blocked - pending balance in party: {}, userId: {}", 
                    membership.getParty().getId(), user.getId());
                throw new BusinessException(
                        "Cannot delete account. You have a non-zero balance in the party: "
                                + membership.getParty().getName(),
                        HttpStatus.CONFLICT);
            }
        }
    }

    /**
     * Anonymizes user account and all party memberships.
     * Used when deleting account to preserve transaction history.
     */
    private void anonymizeAccount(User user, List<Membership> memberships) {
        user.setName("Deleted User");
        user.setLogin("deleted-" + user.getId());
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        memberships.forEach(membership -> membership.setAlias("Deleted User"));
        logger.debug("Account anonymized - userId: {}", user.getId());
    }
}