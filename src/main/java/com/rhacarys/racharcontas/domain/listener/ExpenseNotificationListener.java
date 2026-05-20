package com.rhacarys.racharcontas.domain.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.rhacarys.racharcontas.domain.event.ExpenseCreatedEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * Listener responsible for consuming financial events and triggering notifications asynchronously.
 */
@Slf4j
@Component
public class ExpenseNotificationListener {

    /**
     * Handles the ExpenseCreatedEvent asynchronously.
     * Simulates sending push notifications or emails to all debtors involved.
     */
    @Async
    @EventListener
    public void handleExpenseCreated(ExpenseCreatedEvent event) {
        log.info("Processing expense notification for party: {} [Expense ID: {}]", event.partyId(), event.expenseId());

        try {
            // Simula o envio de uma notificação externa (Push)
            Thread.sleep(1000); 
            
            log.info("Successfully dispatched notifications to {} members for the expense '{}' paid by {}", 
                    event.debtorIds().size(), event.description(), event.payerName());
                    
        } catch (InterruptedException e) {
            log.error("Failed to dispatch notifications for expense: {}", event.expenseId(), e);
            Thread.currentThread().interrupt();
        }
    }
}