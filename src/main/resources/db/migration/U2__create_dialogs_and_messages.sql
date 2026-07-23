-- Flyway Rollback Migration U1: Drop Dialogs and Messages Tables
DROP INDEX IF EXISTS idx_messages_dialog_id;
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS dialogs;
