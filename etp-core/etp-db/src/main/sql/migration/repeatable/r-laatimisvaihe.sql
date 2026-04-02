insert into laatimisvaihe (id, label_fi, label_sv)
values
(0, 'Rakennuslupavaihe, uudisrakennus', 'Bygglov'),
(1, 'Käyttöönottovaihe, uudisrakennus', 'Införandet'),
(2, 'Olemassa oleva rakennus', 'Befintlig byggnad'),
(3, 'Rakennuslupavaihe, laajamittainen perusparannus', 'Rakennuslupavaihe, laajamittainen perusparannus (sv)'),
(4, 'Käyttöönottovaihe, laajamittainen perusparannus', 'Käyttöönottovaihe, laajamittainen perusparannus (sv)')
on conflict (id) do update set
  label_fi = excluded.label_fi,
  label_sv = excluded.label_sv
