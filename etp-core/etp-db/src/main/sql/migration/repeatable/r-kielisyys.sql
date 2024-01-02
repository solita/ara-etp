insert into kielisyys (id, label_fi, label_sv)
values
(0, 'Suomi', 'Finska'),
(1, 'Ruotsi', 'Svenska'),
(2, 'Kaksikielinen', 'Tvåspråkig')
on conflict (id) do update set
  label_fi = excluded.label_fi,
  label_sv = excluded.label_sv
