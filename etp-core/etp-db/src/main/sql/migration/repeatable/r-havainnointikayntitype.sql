insert into havainnointikayntitype(id, label_fi, label_sv, ordinal)
values
(1, 'Paikan päällä käynti', 'Paikan päällä käynti (sv)', 1),
(2, 'Virtuaalikäynti', 'Virtuaalikäynti (sv)', 2)
on conflict (id) do update set
  label_fi = excluded.label_fi,
  label_sv = excluded.label_sv,
  ordinal = excluded.ordinal;
