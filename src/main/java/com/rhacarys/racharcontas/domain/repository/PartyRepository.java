package com.rhacarys.racharcontas.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rhacarys.racharcontas.domain.model.Party;

public interface PartyRepository extends JpaRepository<Party, UUID> {

    @Query("SELECT p FROM Party p WHERE p.code = :code AND p.deletedAt IS NULL")
    Optional<Party> findByCode(@Param("code") String code);

    @Query("SELECT p FROM Party p JOIN Membership m ON m.party.id = p.id " +
            "WHERE m.user.id = :userId AND p.deletedAt IS NULL AND m.deletedAt IS NULL")
    List<Party> findAllByUserId(@Param("userId") UUID userId);

    @Query("SELECT p FROM Party p WHERE p.id = :partyId AND p.updatedAt > :lastSync")
    Optional<Party> findModifiedSince(@Param("partyId") UUID partyId, @Param("lastSync") Instant lastSync);
}