-- Disable triggers for a while.
ALTER TABLE yritys DISABLE TRIGGER yritys_update_trigger;
ALTER TABLE laatija DISABLE TRIGGER laatija_update_trigger;

-- Create sequences for asiakastunnukset.
CREATE SEQUENCE yritys_laskutus_asiakastunnus_seq START 1;
CREATE SEQUENCE laatija_laskutus_asiakastunnus_seq START 1;

-- Create columns for audit purposes.
ALTER TABLE audit.yritys ADD COLUMN laskutus_asiakastunnus text;
ALTER TABLE audit.laatija ADD COLUMN laskutus_asiakastunnus text;

-- Create columns for asiakastunnukset.
ALTER TABLE yritys ADD COLUMN laskutus_asiakastunnus text UNIQUE;
ALTER TABLE laatija ADD COLUMN laskutus_asiakastunnus text UNIQUE;

-- Set owners and permissions for the new sequences.
ALTER SEQUENCE yritys_laskutus_asiakastunnus_seq OWNED BY yritys.laskutus_asiakastunnus;
ALTER SEQUENCE laatija_laskutus_asiakastunnus_seq OWNED BY laatija.laskutus_asiakastunnus;
GRANT USAGE, SELECT ON SEQUENCE laatija_laskutus_asiakastunnus_seq TO etp_app;
GRANT USAGE, SELECT ON SEQUENCE yritys_laskutus_asiakastunnus_seq TO etp_app;

-- Update audit procedures and enable triggers.
CALL audit.create_audit_procedure('yritys'::name);
CALL audit.create_audit_procedure('laatija'::name);
ALTER TABLE yritys ENABLE TRIGGER yritys_update_trigger;
ALTER TABLE laatija ENABLE TRIGGER laatija_update_trigger;

-- Update asiakastunnus of every existing yritys. Use id, which works nicely
-- since yritys id = tilausasiakas id from old data.
UPDATE yritys SET laskutus_asiakastunnus = 'L1' || lpad(id::text, 8, '0');

-- Set the new asiakastunnus sequence for yritys to the same number that is in
-- the yritys id sequence so that they are incremented as a pair in the future
-- and provide the same value for both the id and asiakastunnus.
SELECT setval('yritys_laskutus_asiakastunnus_seq', (SELECT last_value FROM etp.yritys_id_seq), true);

-- SET asiakastunnus of yritys to NOT NULL and make it use new sequence as default.
ALTER TABLE yritys ALTER COLUMN laskutus_asiakastunnus SET NOT NULL;
ALTER TABLE yritys ALTER COLUMN laskutus_asiakastunnus
  SET DEFAULT 'L1' || lpad(nextval('yritys_laskutus_asiakastunnus_seq')::text, 8, '0');

-- Two things that are only ran if conversion_etp.lasku table exists.
DO LANGUAGE plpgsql $$
  BEGIN
    IF (SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'conversion_etp' AND table_name = 'lasku')) THEN

      -- Set asiakastunnus sequence for laatija according to the max tilausasiakas id belonging
      -- to laatija in old data.
      PERFORM setval('laatija_laskutus_asiakastunnus_seq',
                     (SELECT max(t.id)
                        FROM conversion_etp.laatija l
                        LEFT JOIN conversion_etp.tilausasiakas t ON l.tilausasiakas = t.id),
                     true);

      -- Update asiakastunnus for every existing laatija. Use tilausasiakas id joined from
      -- laatija table (both tables from old data). For laatijat without tilausasiakas row,
      -- fallback to newly set asiakastunnus sequence for laatijat. This is to make sure that
      -- from now on, each laatija has asiakastunnus.
      UPDATE laatija
       SET laskutus_asiakastunnus = 'L0' ||
           lpad(coalesce(
                 (SELECT conversion_laatija.tilausasiakas
                    FROM conversion_etp.laatija conversion_laatija
                    WHERE conversion_laatija.id = laatija.id),
                 nextval('laatija_laskutus_asiakastunnus_seq'))::text,
                8, '0');

      RAISE NOTICE 'LASKUTUS CONVERSION FROM OLD DATA READY';
    ELSE
      -- If old data was not available, set asiakastunnus using the id of laatija and
      -- reset new laatija sequence. This should only happen in every other environment
      -- except production.
      UPDATE laatija SET laskutus_asiakastunnus = 'L0' || lpad(id::text, 8, '0');

      PERFORM setval('laatija_laskutus_asiakastunnus_seq',
                     (SELECT last_value FROM etp.kayttaja_id_seq),
                     true);
      RAISE NOTICE 'LASKUTUS DATA WAS NOT AVAILABLE. SET UP AS NEW DATABASE.';
    END IF;
  END;
$$;

-- SET asiakastunnus of laatija to NOT NULL and make it use new sequence as default.
ALTER TABLE laatija ALTER COLUMN laskutus_asiakastunnus SET NOT NULL;
ALTER TABLE laatija ALTER COLUMN laskutus_asiakastunnus
  SET DEFAULT 'L0' || lpad(nextval('laatija_laskutus_asiakastunnus_seq')::text, 8, '0');
