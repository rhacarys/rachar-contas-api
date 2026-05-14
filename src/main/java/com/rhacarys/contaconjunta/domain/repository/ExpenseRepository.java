package com.rhacarys.contaconjunta.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rhacarys.contaconjunta.domain.model.Expense;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findByPartyIdOrderByDateDesc(UUID partyId);
}