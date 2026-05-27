INSERT INTO ilmastoselvitys_laadintaperuste (id, label_fi, label_sv, ordinal)
VALUES
    (0, 'Uusi rakennus', 'Ny byggnad', 1),
    (1, 'Perusparannus A+-luokkaan', 'Renovering till A+-klass', 2)

ON conflict (id) do UPDATE set
    label_fi = excluded.label_fi,
    label_sv = excluded.label_sv,
    ordinal = excluded.ordinal;
