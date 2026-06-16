package com.rhacarys.racharcontas.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rhacarys.racharcontas.domain.model.Membership;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    @Query("SELECT m FROM Membership m WHERE m.party.id = :partyId AND m.deletedAt IS NULL")
    List<Membership> findByPartyId(@Param("partyId") UUID partyId);

    @Query("SELECT COUNT(m) > 0 FROM Membership m WHERE m.party.id = :partyId AND m.user.id = :userId AND m.deletedAt IS NULL")
    boolean existsByPartyIdAndUserId(@Param("partyId") UUID partyId, @Param("userId") UUID userId);

    @Query("SELECT m FROM Membership m WHERE m.party.id = :partyId AND m.user.id = :userId AND m.deletedAt IS NULL")
    Optional<Membership> findByPartyIdAndUserId(@Param("partyId") UUID partyId, @Param("userId") UUID userId);

    @Query("SELECT m FROM Membership m WHERE m.id IN :ids AND m.party.id = :partyId AND m.deletedAt IS NULL")
    List<Membership> findAllByIdInAndPartyId(@Param("ids") List<UUID> ids, @Param("partyId") UUID partyId);

    @Query("SELECT m FROM Membership m WHERE m.user.id = :userId AND m.deletedAt IS NULL")
    List<Membership> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(m) FROM Membership m WHERE m.party.id = :partyId AND m.deletedAt IS NULL")
    long countByPartyId(@Param("partyId") UUID partyId);

    @Query("SELECT m FROM Membership m WHERE m.party.id = :partyId AND m.updatedAt > :lastSync")
    List<Membership> findModifiedSince(@Param("partyId") UUID partyId, @Param("lastSync") Instant lastSync);
}