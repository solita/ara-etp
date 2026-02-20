INSERT INTO ilmastoselvitys_laadintaperuste (id, label_fi, label_sv, ordinal)
VALUES
    (0, 'Uusi rakennus', 'Uusi rakennus(sv)', 1),
    (1, 'Perusparannus A+-luokkaan', 'Perusparannus A+-luokkaan(sv)', 2)

ON conflict (id) do UPDATE set
    label_fi = excluded.label_fi,
    label_sv = excluded.label_sv,
    ordinal = excluded.ordinal;
