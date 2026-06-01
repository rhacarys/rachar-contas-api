-- Adiciona controle de Soft Delete para permitir a sincronização de remoções feitas offline
ALTER TABLE parties ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE memberships ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE expenses ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE expense_splits ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;

-- Adiciona controle de versão (Timestamp) para resolução de conflitos (Last-Write-Wins)
ALTER TABLE parties ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE memberships ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE expenses ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE expense_splits ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- Cria índices para otimizar as consultas de sincronização que buscarão os dados mais recentes
CREATE INDEX idx_parties_updated_at ON parties(updated_at);
CREATE INDEX idx_memberships_updated_at ON memberships(updated_at);
CREATE INDEX idx_expenses_updated_at ON expenses(updated_at);
CREATE INDEX idx_expense_splits_updated_at ON expense_splits(updated_at);
