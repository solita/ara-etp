insert into laatimisvaihe (id, label_fi, label_sv, versio)
values
(0, 'Rakennuslupavaihe, uudisrakennus', 'Bygglov', 2018),
(1, 'Käyttöönottovaihe, uudisrakennus', 'Införandet', 2018),
(2, 'Olemassa oleva rakennus', 'Befintlig byggnad', 2018),
(3, 'Rakennuslupavaihe, laajamittainen perusparannus', 'Rakennuslupavaihe, laajamittainen perusparannus (sv)', 2026),
(4, 'Käyttöönottovaihe, laajamittainen perusparannus', 'Käyttöönottovaihe, laajamittainen perusparannus (sv)', 2026)
on conflict (id) do update set
  label_fi = excluded.label_fi,
  label_sv = excluded.label_sv,
  versio = excluded.versio
