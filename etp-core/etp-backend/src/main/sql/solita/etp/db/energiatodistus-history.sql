-- name: select-energiatodistus-audits
SELECT audit_energiatodistus.*,
       fullname(etp_kayttaja.*) laatija_fullname,
       (SELECT id FROM audit.energiatodistus
        WHERE audit_energiatodistus.id = audit.energiatodistus.korvattu_energiatodistus_id
          AND transaction_id = audit_energiatodistus.transaction_id) korvaava_energiatodistus_id,
       fullname(audit_kayttaja.*) modifiedby_fullname
FROM audit.energiatodistus audit_energiatodistus
LEFT JOIN etp.energiatodistus etp_energiatodistus ON audit_energiatodistus.id = etp_energiatodistus.id
LEFT JOIN kayttaja etp_kayttaja ON etp_energiatodistus.laatija_id = etp_kayttaja.id
LEFT JOIN kayttaja audit_kayttaja ON audit_energiatodistus.modifiedby_id = audit_kayttaja.id
WHERE audit_energiatodistus.id = :id
  AND etp_energiatodistus.tila_id <> (SELECT poistettu FROM et_tilat)
ORDER BY audit_energiatodistus.modifytime ASC;
