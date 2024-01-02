-- Makes sure that laskutus related sequences have identical state with related
-- id sequences.
DO LANGUAGE plpgsql $$
  BEGIN
    IF (SELECT NOT is_called FROM etp.yritys_id_seq) THEN
      PERFORM setval('yritys_laskutus_asiakastunnus_seq',
                     (SELECT last_value FROM etp.yritys_id_seq),
                     false);
    END IF;

    IF (SELECT NOT is_called FROM etp.kayttaja_id_seq) THEN
      PERFORM setval('laatija_laskutus_asiakastunnus_seq',
                     (SELECT last_value FROM etp.kayttaja_id_seq),
                     false);
    END IF;
  END;
$$;
