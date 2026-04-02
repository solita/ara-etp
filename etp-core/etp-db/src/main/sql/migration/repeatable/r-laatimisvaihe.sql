-- Version 2018: 3 rows with short-form labels
insert into laatimisvaihe (id, label_fi, label_sv, versio)
values
(0, 'Rakennuslupa', 'Bygglov', 2018),
(1, 'Käyttöönotto', 'Införandet', 2018),
(2, 'Olemassa oleva rakennus', 'Befintlig byggnad', 2018)
on conflict (id, versio) do update set
  label_fi = excluded.label_fi,
  label_sv = excluded.label_sv;

-- Version 2026: 5 rows with long-form labels
insert into laatimisvaihe (id, label_fi, label_sv, versio)
values
(0, 'Rakennuslupavaihe, uudisrakennus', 'Bygglovsfasen, nybyggnad', 2026),
(1, 'Käyttöönottovaihe, uudisrakennus', 'Införandefasen, nybyggnad', 2026),
(2, 'Olemassa oleva rakennus', 'Befintlig byggnad', 2026),
(3, 'Rakennuslupavaihe, laajamittainen perusparannus', 'Bygglovsfasen, omfattande grundlig renovering', 2026),
(4, 'Käyttöönottovaihe, laajamittainen perusparannus', 'Införandefasen, omfattande grundlig renovering', 2026)
on conflict (id, versio) do update set
  label_fi = excluded.label_fi,
  label_sv = excluded.label_sv;
