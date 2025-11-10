-- name: select-kuukauden-laskutus
SELECT e.id energiatodistus_id, e.allekirjoitusaika, e.laskuriviviite,
       k.id laatija_id, k.etunimi || ' ' || k.sukunimi laatija_nimi,
       k.henkilotunnus,
       y.id yritys_id, y.ytunnus, v.valittajatunnus, y.verkkolaskuosoite,
       y.type_id,

       -- Fields depending on who should be invoiced.
       CASE WHEN e.laskutettava_yritys_id IS NULL THEN k.etunimi ||
                                                       ' ' ||
                                                       k.sukunimi
            WHEN e.laskutettava_yritys_id IS NOT NULL THEN y.nimi
       END nimi,

       CASE WHEN e.laskutettava_yritys_id IS NULL THEN l.laskutus_asiakastunnus
       WHEN e.laskutettava_yritys_id IS NOT NULL THEN y.laskutus_asiakastunnus
       END laskutus_asiakastunnus,

       CASE WHEN e.laskutettava_yritys_id IS NULL THEN l.laskutuskieli
            WHEN e.laskutettava_yritys_id IS NOT NULL THEN y.laskutuskieli
       END laskutuskieli,

       CASE WHEN e.laskutettava_yritys_id IS NULL THEN l.jakeluosoite
            WHEN e.laskutettava_yritys_id IS NOT NULL THEN y.jakeluosoite
       END jakeluosoite,

       CASE WHEN e.laskutettava_yritys_id IS NULL THEN l.vastaanottajan_tarkenne
            WHEN e.laskutettava_yritys_id IS NOT NULL THEN y.vastaanottajan_tarkenne
       END vastaanottajan_tarkenne,

       CASE WHEN e.laskutettava_yritys_id IS NULL THEN l.postinumero
            WHEN e.laskutettava_yritys_id IS NOT NULL THEN y.postinumero
       END postinumero,

       CASE WHEN e.laskutettava_yritys_id IS NULL THEN l.postitoimipaikka
            WHEN e.laskutettava_yritys_id IS NOT NULL THEN y.postitoimipaikka
       END postitoimipaikka,

       CASE WHEN e.laskutettava_yritys_id IS NULL THEN l.maa
            WHEN e.laskutettava_yritys_id IS NOT NULL THEN y.maa
       END maa
FROM energiatodistus e
  LEFT JOIN laatija l ON e.laatija_id = l.id
  LEFT JOIN kayttaja k ON l.id = k.id
  LEFT JOIN yritys y ON e.laskutettava_yritys_id = y.id
  LEFT JOIN verkkolaskuoperaattori v ON y.verkkolaskuoperaattori = v.id
  LEFT JOIN energiatodistus korvattu ON e.korvattu_energiatodistus_id = korvattu.id
WHERE
    -- Check that allekirjoitus is within the previous calendar month
    e.allekirjoitusaika IS NOT NULL AND
    e.allekirjoitusaika < date_trunc('month', now()) AND
    date_trunc('month', now()) - interval '1 month' <= e.allekirjoitusaika AND

    -- Must not have been invoiced already
    e.laskutusaika IS NULL AND

    -- A bunch of rules to exclude a somewhat complicated exception that allows an author
    -- to reissue a certificate within 7 days without it being invoiced again.
    (-- The certificate does not replace another
     e.korvattu_energiatodistus_id IS NULL OR

     -- The new certificate replaced an old one, which was at least 7 days old at the time of signing
     date_trunc('day', e.allekirjoitusaika) - interval '7 days' > korvattu.allekirjoitusaika OR

     -- The replaced certificate has been destroyed (implies it was old)
     korvattu.tila_id = (SELECT tuhottu FROM et_tilat) OR

     -- The replaced certificate was made by a different author
     korvattu.laatija_id != e.laatija_id);


-- name: mark-as-laskutettu!
UPDATE energiatodistus SET laskutusaika = now() WHERE id IN (:ids);
