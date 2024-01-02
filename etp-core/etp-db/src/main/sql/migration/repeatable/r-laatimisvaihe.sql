insert into laatimisvaihe (id, label_fi, label_sv)
values
(0, 'Rakennuslupa', 'Bygglov'),
(1, 'Käyttöönotto', 'Införandet'),
(2, 'Olemassa oleva rakennus', 'Befintlig byggnad')
on conflict (id) do update set
  label_fi = excluded.label_fi,
  label_sv = excluded.label_sv
