package com.rhacarys.contaconjunta.domain.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rhacarys.contaconjunta.api.dto.JoinPartyRequest;
import com.rhacarys.contaconjunta.api.dto.PartyBalanceResponse;
import com.rhacarys.contaconjunta.api.dto.PartyRequest;
import com.rhacarys.contaconjunta.api.dto.PartyResponse;
import com.rhacarys.contaconjunta.domain.exception.BusinessException;
import com.rhacarys.contaconjunta.domain.model.Currency;
import com.rhacarys.contaconjunta.domain.model.Membership;
import com.rhacarys.contaconjunta.domain.model.Party;
import com.rhacarys.contaconjunta.domain.model.User;
import com.rhacarys.contaconjunta.domain.repository.CurrencyRepository;
import com.rhacarys.contaconjunta.domain.repository.MembershipRepository;
import com.rhacarys.contaconjunta.domain.repository.PartyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartyService {

    private final PartyRepository partyRepository;
    private final CurrencyRepository currencyRepository;
    private final MembershipRepository membershipRepository;
    private final BalanceService balanceService;

    /**
     * Creates a new party and adds the creator as ADMIN member.
     */
    @Transactional
    public PartyResponse createParty(PartyRequest request, User creator) {
        log.debug("Creating new party - userId: {}, partyName: {}", creator.getId(), request.name());

        Party party = buildAndSaveParty(request);
        createMembership(party, creator, creator.getName(), "ADMIN");

        log.info("Party created successfully - partyId: {}, code: {}, creatorId: {}",
                party.getId(), party.getCode(), creator.getId());

        return PartyResponse.fromEntity(party);
    }

    /**
     * Allows a user to join an existing party using a unique party code.
     */
    @Transactional
    public PartyResponse joinParty(JoinPartyRequest request, User user) {
        log.debug("User attempting to join party - userId: {}, code: {}", user.getId(), request.code());

        Party party = getPartyByCode(request.code());
        validateNotAlreadyMember(party.getId(), user.getId());

        createMembership(party, user, request.alias(), "MEMBER");

        log.info("User joined party successfully - partyId: {}, userId: {}, alias: {}",
                party.getId(), user.getId(), request.alias());

        return PartyResponse.fromEntity(party);
    }

    /**
     * Removes user from party after validating zero balance.
     * Automatically deletes party if no members remain.
     */
    @Transactional
    public void leaveParty(UUID partyId, User user) {
        log.debug("User leaving party - partyId: {}, userId: {}", partyId, user.getId());

        Membership membership = getMembership(partyId, user.getId());
        validateZeroBalance(partyId, user, membership.getId(),
                "You cannot leave the party unless your balance is exactly zero");

        membershipRepository.delete(membership);
        deletePartyIfEmpty(partyId);

        log.info("User left party - partyId: {}, userId: {}", partyId, user.getId());
    }

    /**
     * Updates party details. Only party admins can perform this action.
     */
    @Transactional
    public PartyResponse updateParty(UUID partyId, PartyRequest request, User loggedUser) {
        log.debug("Updating party - partyId: {}, userId: {}", partyId, loggedUser.getId());

        validateAdminRole(partyId, loggedUser.getId());

        Party party = getPartyById(partyId);
        updatePartyDetails(party, request);

        Party updatedParty = partyRepository.save(party);
        log.info("Party updated - partyId: {}, newName: {}", partyId, request.name());

        return PartyResponse.fromEntity(updatedParty);
    }

    /**
     * Removes a member from the party. Only admins can kick members.
     * Target member must have zero balance before removal.
     */
    @Transactional
    public void kickMember(UUID partyId, UUID membershipIdToKick, User loggedUser) {
        log.debug("Admin attempting to kick member - partyId: {}, membershipId: {}, adminId: {}",
                partyId, membershipIdToKick, loggedUser.getId());

        validateAdminRole(partyId, loggedUser.getId());
        Membership memberToKick = getMembershipByIdAndParty(membershipIdToKick, partyId);

        validateZeroBalance(partyId, loggedUser, membershipIdToKick,
                "Cannot remove member unless their balance is exactly zero");

        membershipRepository.delete(memberToKick);
        log.info("Member kicked from party - partyId: {}, membershipId: {}, kickedBy: {}",
                partyId, membershipIdToKick, loggedUser.getId());
    }

    public List<PartyResponse> getUserParties(User user) {
        log.debug("Fetching parties for userId: {}", user.getId());

        List<PartyResponse> parties = partyRepository.findAllByUserId(user.getId()).stream()
                .map(PartyResponse::fromEntity)
                .toList();

        log.debug("User is member of {} parties - userId: {}", parties.size(), user.getId());
        return parties;
    }

    private Party buildAndSaveParty(PartyRequest request) {
        Currency currency = getCurrencyByCode(request.currencyCode());

        Party party = new Party();
        party.setName(request.name());
        party.setDescription(request.description());
        party.setCurrency(currency);
        party.setCode(generateUniqueCode());

        return partyRepository.save(party);
    }

    private void updatePartyDetails(Party party, PartyRequest request) {
        party.setName(request.name());
        party.setDescription(request.description());

        if (request.currencyCode() != null) {
            party.setCurrency(getCurrencyByCode(request.currencyCode()));
        }
    }

    private void createMembership(Party party, User user, String alias, String role) {
        Membership membership = new Membership();
        membership.setParty(party);
        membership.setUser(user);
        membership.setAlias(alias);
        membership.setRole(role);

        membershipRepository.save(membership);
        log.debug("Membership created - partyId: {}, userId: {}, role: {}",
                party.getId(), user.getId(), role);
    }

    /**
     * Validates that target member has exactly zero balance before allowing
     * removal.
     * Ensures no debts are left unresolved when user leaves or is removed.
     */
    private void validateZeroBalance(UUID partyId, User loggedUser, UUID targetMembershipId, String errorMessage) {
        PartyBalanceResponse balancesResponse = balanceService.calculateBalances(partyId, loggedUser);

        boolean hasPendingBalance = balancesResponse.balances().stream()
                .filter(b -> b.membershipId().equals(targetMembershipId))
                .anyMatch(b -> b.balance().compareTo(BigDecimal.ZERO) != 0);

        if (hasPendingBalance) {
            log.warn("Zero balance validation failed - membershipId: {}, partyId: {}",
                    targetMembershipId, partyId);
            throw new BusinessException(errorMessage, HttpStatus.CONFLICT);
        }
    }

    /**
     * Validates that the user has ADMIN role in the party.
     */
    private void validateAdminRole(UUID partyId, UUID userId) {
        Membership membership = getMembership(partyId, userId);
        if (!"ADMIN".equals(membership.getRole())) {
            log.warn("Admin role validation failed - userId: {}, partyId: {}", userId, partyId);
            throw new BusinessException("Only ADMINs can perform this action", HttpStatus.FORBIDDEN);
        }
    }

    private void validateNotAlreadyMember(UUID partyId, UUID userId) {
        if (membershipRepository.existsByPartyIdAndUserId(partyId, userId)) {
            log.warn("User already member of party - userId: {}, partyId: {}", userId, partyId);
            throw new BusinessException("You are already a member of this party", HttpStatus.CONFLICT);
        }
    }

    private Party getPartyById(UUID partyId) {
        return partyRepository.findById(partyId)
                .orElseThrow(() -> new BusinessException("Party not found", HttpStatus.NOT_FOUND));
    }

    private Party getPartyByCode(String code) {
        return partyRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new BusinessException("Party not found or invalid code", HttpStatus.NOT_FOUND));
    }

    private Membership getMembership(UUID partyId, UUID userId) {
        return membershipRepository.findByPartyIdAndUserId(partyId, userId)
                .orElseThrow(() -> new BusinessException("You are not a member of this party", HttpStatus.FORBIDDEN));
    }

    private Membership getMembershipByIdAndParty(UUID membershipId, UUID partyId) {
        return membershipRepository.findById(membershipId)
                .filter(m -> m.getParty().getId().equals(partyId))
                .orElseThrow(() -> new BusinessException("Member not found in this party", HttpStatus.NOT_FOUND));
    }

    private Currency getCurrencyByCode(String code) {
        return currencyRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new BusinessException("Currency not found", HttpStatus.NOT_FOUND));
    }

    private void deletePartyIfEmpty(UUID partyId) {
        long memberCount = membershipRepository.countByPartyId(partyId);

        if (memberCount == 0) {
            Party party = getPartyById(partyId);
            partyRepository.delete(party);
            log.info("Empty party deleted - partyId: {}", partyId);
        }
    }

    private String generateUniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}