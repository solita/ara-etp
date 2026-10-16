-- AE-2898: tilaaja is no longer processed. Clear existing data, keep the column.
-- Disable the audit trigger so that clearing does not create new audit rows.
ALTER TABLE energiatodistus DISABLE TRIGGER energiatodistus_update_trigger;

UPDATE energiatodistus SET pt$tilaaja = NULL WHERE pt$tilaaja IS NOT NULL;
UPDATE audit.energiatodistus SET pt$tilaaja = NULL WHERE pt$tilaaja IS NOT NULL;

ALTER TABLE energiatodistus ENABLE TRIGGER energiatodistus_update_trigger;
