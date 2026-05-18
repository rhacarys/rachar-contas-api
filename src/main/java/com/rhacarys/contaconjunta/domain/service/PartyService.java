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

@Service
@RequiredArgsConstructor
public class PartyService {

    private final PartyRepository partyRepository;
    private final CurrencyRepository currencyRepository;
    private final MembershipRepository membershipRepository;
    private final BalanceService balanceService;

    @Transactional
    public PartyResponse createParty(PartyRequest request, User creator) {
        Party party = buildAndSaveParty(request);
        createMembership(party, creator, creator.getName(), "ADMIN");

        return PartyResponse.fromEntity(party);
    }

    @Transactional
    public PartyResponse joinParty(JoinPartyRequest request, User user) {
        Party party = getPartyByCode(request.code());
        validateNotAlreadyMember(party.getId(), user.getId());

        createMembership(party, user, request.alias(), "MEMBER");

        return PartyResponse.fromEntity(party);
    }

    @Transactional
    public void leaveParty(UUID partyId, User user) {
        Membership membership = getMembership(partyId, user.getId());

        validateZeroBalance(partyId, user, membership.getId(),
                "You cannot leave the party unless your balance is exactly zero");

        membershipRepository.delete(membership);
    }

    @Transactional
    public PartyResponse updateParty(UUID partyId, PartyRequest request, User loggedUser) {
        validateAdminRole(partyId, loggedUser.getId());

        Party party = getPartyById(partyId);
        updatePartyDetails(party, request);

        return PartyResponse.fromEntity(partyRepository.save(party));
    }

    @Transactional
    public void kickMember(UUID partyId, UUID membershipIdToKick, User loggedUser) {
        validateAdminRole(partyId, loggedUser.getId());
        Membership memberToKick = getMembershipByIdAndParty(membershipIdToKick, partyId);

        validateZeroBalance(partyId, loggedUser, membershipIdToKick,
                "Cannot remove member unless their balance is exactly zero");

        membershipRepository.delete(memberToKick);
    }

    public List<PartyResponse> getUserParties(User user) {
        return partyRepository.findAllByUserId(user.getId()).stream()
                .map(PartyResponse::fromEntity)
                .toList();
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
    }

    private void validateZeroBalance(UUID partyId, User loggedUser, UUID targetMembershipId, String errorMessage) {
        PartyBalanceResponse balancesResponse = balanceService.calculateBalances(partyId, loggedUser);

        boolean hasPendingBalance = balancesResponse.balances().stream()
                .filter(b -> b.membershipId().equals(targetMembershipId))
                .anyMatch(b -> b.balance().compareTo(BigDecimal.ZERO) != 0);

        if (hasPendingBalance) {
            throw new BusinessException(errorMessage, HttpStatus.CONFLICT);
        }
    }

    private void validateAdminRole(UUID partyId, UUID userId) {
        Membership membership = getMembership(partyId, userId);
        if (!"ADMIN".equals(membership.getRole())) {
            throw new BusinessException("Only ADMINs can perform this action", HttpStatus.FORBIDDEN);
        }
    }

    private void validateNotAlreadyMember(UUID partyId, UUID userId) {
        if (membershipRepository.existsByPartyIdAndUserId(partyId, userId)) {
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

    private String generateUniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}