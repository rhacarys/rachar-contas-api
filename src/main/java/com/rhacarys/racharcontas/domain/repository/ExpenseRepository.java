package com.rhacarys.racharcontas.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhacarys.racharcontas.domain.model.Expense;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    @Query("SELECT e FROM Expense e WHERE e.party.id = :partyId AND e.deletedAt IS NULL ORDER BY e.date DESC")
    List<Expense> findByPartyIdOrderByDateDesc(@Param("partyId") UUID partyId);

    @Query("SELECT DISTINCT e FROM Expense e " +
            "JOIN FETCH e.payer p " +
            "LEFT JOIN FETCH e.splits s " +
            "LEFT JOIN FETCH s.debtor d " +
            "WHERE e.party.id = :partyId AND e.deletedAt IS NULL " +
            "AND p.deletedAt IS NULL")
    List<Expense> findAllByPartyIdWithSplits(@Param("partyId") UUID partyId);

    @Query("SELECT DISTINCT e FROM Expense e " +
            "LEFT JOIN FETCH e.splits s " +
            "WHERE e.party.id = :partyId AND e.updatedAt > :lastSync")
    List<Expense> findModifiedSince(@Param("partyId") UUID partyId, @Param("lastSync") Instant lastSync);
}