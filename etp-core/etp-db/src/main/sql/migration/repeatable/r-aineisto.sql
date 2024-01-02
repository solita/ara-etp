insert into aineisto (id, label_fi, label_sv, valid)
values
(1, 'Pankit', 'Banker', true),
(2, 'Tilastokeskus', 'Statistikcentralen', true),
(3, 'Anonymisoitu', 'Anonymiserad', true)
on conflict (id) do update set
  label_fi = excluded.label_fi,
  label_sv = excluded.label_sv,
  valid = excluded.valid;
