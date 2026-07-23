-- U1__campaigns_leads_schema.sql
-- Drop indexes
DROP INDEX IF EXISTS idx_leads_phone_number;
DROP INDEX IF EXISTS idx_leads_username;

-- Drop tables
DROP TABLE IF EXISTS leads;
DROP TABLE IF EXISTS spintax_configs;
DROP TABLE IF EXISTS campaigns;
