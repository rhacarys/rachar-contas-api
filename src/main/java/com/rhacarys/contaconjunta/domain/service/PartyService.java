package com.rhacarys.contaconjunta.domain.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rhacarys.contaconjunta.api.dto.JoinPartyRequest;
import com.rhacarys.contaconjunta.api.dto.PartyRequest;
import com.rhacarys.contaconjunta.api.dto.PartyResponse;
import com.rhacarys.contaconjunta.domain.exception.BusinessException;
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

    @Transactional
    public PartyResponse createParty(PartyRequest request, User creator) {
        var currency = currencyRepository.findByCode(request.currencyCode().toUpperCase())
                .orElseThrow(() -> new BusinessException("Currency not found", HttpStatus.NOT_FOUND));

        Party party = new Party();
        party.setName(request.name());
        party.setDescription(request.description());
        party.setCurrency(currency);
        party.setCode(generateUniqueCode());

        Party savedParty = partyRepository.save(party);

        Membership membership = new Membership();
        membership.setParty(savedParty);
        membership.setUser(creator);
        membership.setAlias(creator.getName());
        membership.setRole("ADMIN");

        membershipRepository.save(membership);

        return PartyResponse.fromEntity(savedParty);
    }

    public List<PartyResponse> getUserParties(User user) {
        return partyRepository.findAllByUserId(user.getId()).stream()
                .map(PartyResponse::fromEntity)
                .toList();
    }

    @Transactional
    public PartyResponse joinParty(JoinPartyRequest request, User user) {
        Party party = partyRepository.findByCode(request.code().toUpperCase())
                .orElseThrow(() -> new BusinessException("Party not found or invalid code", HttpStatus.NOT_FOUND));

        if (membershipRepository.existsByPartyIdAndUserId(party.getId(), user.getId())) {
            throw new BusinessException("You are already a member of this party", HttpStatus.CONFLICT);
        }

        Membership membership = new Membership();
        membership.setParty(party);
        membership.setUser(user);
        membership.setAlias(request.alias());
        membership.setRole("MEMBER");

        membershipRepository.save(membership);

        return PartyResponse.fromEntity(party);
    }

    private String generateUniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}