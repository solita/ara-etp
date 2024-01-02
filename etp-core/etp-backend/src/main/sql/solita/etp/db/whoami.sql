-- name: select-whoami
SELECT k.id id, k.etunimi etunimi, k.sukunimi sukunimi, k.email email, k.puhelin puhelin,
       k.rooli_id rooli, k.cognito_id cognitoid, k.virtu$localid,
       k.virtu$organisaatio, k.henkilotunnus henkilotunnus,
       k.organisaatio,
       k.api_key_hash api_key_hash, k.verifytime,
       coalesce(l.partner, false) as partner
FROM kayttaja k
LEFT JOIN laatija l ON l.id = k.id
WHERE (not k.passivoitu) AND
      ((k.email  = :email) OR
       (k.henkilotunnus = :henkilotunnus) OR
       ((k.virtu$localid = :virtu_localid) AND
        (k.virtu$organisaatio = :virtu_organisaatio)));

-- name: update-kayttaja-with-whoami!
UPDATE kayttaja SET login = now(), cognito_id = :cognitoid WHERE id = :id
