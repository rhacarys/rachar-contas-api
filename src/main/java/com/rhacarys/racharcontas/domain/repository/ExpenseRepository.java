package com.rhacarys.racharcontas.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhacarys.racharcontas.domain.model.Expense;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findByPartyIdOrderByDateDesc(UUID partyId);

    @Query("SELECT DISTINCT e FROM Expense e " +
            "JOIN FETCH e.payer " +
            "LEFT JOIN FETCH e.splits s " +
            "LEFT JOIN FETCH s.debtor " +
            "WHERE e.party.id = :partyId")
    List<Expense> findAllByPartyIdWithSplits(@Param("partyId") UUID partyId);
}