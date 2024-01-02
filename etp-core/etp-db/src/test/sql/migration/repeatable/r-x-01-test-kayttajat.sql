INSERT INTO kayttaja (etunimi, sukunimi, email, puhelin, rooli_id, henkilotunnus) VALUES
('Pauli', 'Pätevyyden toteaja', 'patevyydentoteaja@solita.fi', '0451234567', 1, '010280-952L'),
('Liisa', 'Specimen-Potex', 'laatija@solita.fi', '0451234567', 0, '010469-999W'),
('Kalevi', 'Specimen-Potex', 'kumppani@solita.fi', '0451234567', 0, '141199-999N'),
('Harri', 'Specimen-Potex', 'harri.lindberg@solita.fi', '0451234567', 0, '010675-9981'),
('Aleksi', 'Kallan', 'aleksi.kallan@solita.fi', '0451234567', 0, '261298-998X'),
('Ulla', 'Specimen-Pirix', 'ulla.laapotti@ara.fi', '0451234567', 0, '040265-9985'),
('Kirsi', 'Juutilainen', 'kirsi.unhonen@ara.fi', '0451234567', 0, '260991-999R'),
('Ari', 'Manninen', 'ari.manninen@ara.fi', '0451234567', 0, '010101-123N'),
('Risto', 'Jesoi', 'risto.jesoi@ara.fi', '0451234567', 0, '180883-998N'),
('Lamitor', 'Laatija', 'laatija@lamit.fi', '0451234567', 0, '110106A998M'),
('Riuska', 'Laatija', 'laatija@granlund.fi', '0451234567', 0, '290574-9981'),
('Cadmatic', 'Laatija', 'laatija@cadmatic.com', '0451234567', 0, '050391-999B'),
('Timbal', 'Laatija', 'laatija@timbal.fi', '0451234567', 0, '120997-9998'),
('Etlas', 'Laatija', 'laatija@etlas.fi', '0451234567', 0, '271258-9988'),
('Caverion', 'Laatija', 'laatija@caverion.fi', '0451234567', 0, '010170-960F'),
('Laskentapalvelut', 'Laatija', 'laatija@dof.fi', '0451234567', 0, '010170-999R'),
('Laskentaohjelmat', 'Laatija', 'laatija@example.com', '0451234567', 0, '081181-9984')
on conflict (email) do update
  set etunimi = excluded.etunimi,
      sukunimi = excluded.sukunimi,
      puhelin = excluded.puhelin,
      rooli_id = excluded.rooli_id,
      henkilotunnus = excluded.henkilotunnus;

INSERT INTO kayttaja (etunimi, sukunimi, email, puhelin, rooli_id, virtu$localid, virtu$organisaatio) VALUES
('Päivi', 'Pääkäyttäjä', 'paakayttaja@solita.fi', '0501234567', 2, 'vvirkamies', 'testivirasto.fi'),
('Paavo', 'Pääkäyttäjä', 'paakayttaja2@solita.fi', '0501234567', 2, 'vvirkamies3', 'testivirasto.fi'),
('Lasse', 'Laskuttaja', 'laskuttaja@solita.fi', '0501234567', 3, 'vvirkamies2', 'testausvirasto.fi')
on conflict (email) do update
  set etunimi = excluded.etunimi,
      sukunimi = excluded.sukunimi,
      puhelin = excluded.puhelin,
      rooli_id = excluded.rooli_id,
      virtu$localid  = excluded.virtu$localid,
      virtu$organisaatio = excluded.virtu$organisaatio,
      valvoja = true;

INSERT INTO laatija (id, patevyystaso, toteamispaivamaara, toteaja, jakeluosoite, postinumero, postitoimipaikka, partner)
SELECT id, 2, date '2020-01-01', 'KIINKO', 'Peltokatu 26', '33100', 'Tampere', email ilike '%kumppani%' FROM kayttaja WHERE email ILIKE '%solita.fi%' AND rooli_id = 0
UNION ALL
SELECT id, 2, date '2020-01-01', 'FISE', 'Kirkkokatu 12', '15140', 'Lahti', false FROM kayttaja WHERE email ILIKE '%ara.fi%' AND rooli_id = 0
UNION ALL
SELECT id, 1, date '2020-10-01', 'FISE', 'Hämeenkatu 1', '33100', 'Tampere', false FROM kayttaja WHERE email NOT ILIKE '%solita.fi%' AND email NOT ILIKE '%ara.fi%' AND rooli_id = 0
on conflict (id) do update
  set patevyystaso = excluded.patevyystaso,
      toteamispaivamaara = excluded.toteamispaivamaara,
      toteaja = excluded.toteaja,
      jakeluosoite = excluded.jakeluosoite,
      postinumero  = excluded.postinumero,
      postitoimipaikka = excluded.postitoimipaikka,
      partner = excluded.partner;
