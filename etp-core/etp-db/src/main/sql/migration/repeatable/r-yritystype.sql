insert into yritystype (id, label_fi, label_sv, ordinal)
values
(1, 'Elinkeinoelämä (03)', 'Affärsliv (03)', 2),
(2, 'Paikallishallinto (01)', 'Lokal förvaltning (01)', 3)
on conflict (id) do update set
  label_fi = excluded.label_fi,
  label_sv = excluded.label_sv,
  ordinal = excluded.ordinal;
