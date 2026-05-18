-- Adiciona a coluna type na tabela expenses
ALTER TABLE expenses ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'PURCHASE';

-- Remove a coluna is_settled da tabela expense_splits
ALTER TABLE expense_splits DROP COLUMN is_settled;
