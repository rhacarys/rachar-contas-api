package com.rhacarys.contaconjunta.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rhacarys.contaconjunta.domain.model.Party;

@Repository
public interface PartyRepository extends JpaRepository<Party, UUID> {
    Optional<Party> findByCode(String code);
}