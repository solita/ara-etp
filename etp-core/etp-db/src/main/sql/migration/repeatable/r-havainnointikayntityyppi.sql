insert into havainnointikayntityyppi(id, label_fi, label_sv, ordinal)
values
(0, 'Paikan päällä käynti', 'Paikan päällä käynti (sv)', 1),
(1, 'Virtuaalikäynti', 'Virtuaalikäynti (sv)', 2)
on conflict (id) do update set
  label_fi = excluded.label_fi,
  label_sv = excluded.label_sv,
  ordinal = excluded.ordinal;
