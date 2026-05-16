package com.rhacarys.contaconjunta.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhacarys.contaconjunta.domain.model.Party;

@Repository
public interface PartyRepository extends JpaRepository<Party, UUID> {
    Optional<Party> findByCode(String code);

    @Query("SELECT p FROM Party p JOIN Membership m ON m.party.id = p.id WHERE m.user.id = :userId")
    List<Party> findAllByUserId(@Param("userId") UUID userId);
}