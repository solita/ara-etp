insert into laatija_yritys_tila (id, label_fi)
values
  (0, 'Ehdotus'),
  (1, 'Hyväksytty'),
  (2, 'Poistettu')
on conflict (id) do update set
  label_fi = excluded.label_fi;
