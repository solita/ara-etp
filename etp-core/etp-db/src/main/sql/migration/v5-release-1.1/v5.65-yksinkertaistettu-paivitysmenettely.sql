-- AE-2759: Add yksinkertaistettu_paivitysmenettely column to energiatodistus
ALTER TABLE energiatodistus
  ADD COLUMN yksinkertaistettu_paivitysmenettely boolean NOT NULL DEFAULT false;

-- Add to audit table as well
ALTER TABLE audit.energiatodistus
  ADD COLUMN yksinkertaistettu_paivitysmenettely boolean;

-- Recreate audit procedure to include the new column
call audit.create_audit_procedure('energiatodistus'::name);
