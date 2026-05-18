package com.rhacarys.contaconjunta.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rhacarys.contaconjunta.domain.model.Membership;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, UUID> {
    List<Membership> findByPartyId(UUID partyId);

    boolean existsByPartyIdAndUserId(UUID partyId, UUID userId);

    Optional<Membership> findByPartyIdAndUserId(UUID partyId, UUID userId);

    List<Membership> findAllByIdInAndPartyId(List<UUID> ids, UUID partyId);

    List<Membership> findByUserId(UUID userId);

    long countByPartyId(UUID partyId);
}