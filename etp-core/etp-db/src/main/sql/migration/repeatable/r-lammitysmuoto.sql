INSERT INTO lammitysmuoto (id, label_fi, label_sv, ordinal)
VALUES
(0, 'Kaukolämpö', 'Fjärrvärme', 1),
(1, 'Sähkö', 'El', 2),
(2, 'Puu', 'Trä', 3),
(3, 'Pelletti', 'Pellet', 4),
(4, 'Öljy', 'Olja', 5),
(5, 'Kaasu', 'Gas', 6),
(6, 'Maalämpöpumppu', 'Jordvärmepump', 7),
(7, 'Vesi-ilmalämpöpumppu', 'Vatten-luftvärmepump', 8),
(8, 'Poistoilmalämpöpumppu', 'Frånluftsvärmepump', 9),
(9, 'Muu lämmitysjärjestelmä', 'Annat uppvärmningssystem', 10)
ON CONFLICT (id) DO UPDATE
SET
label_fi = excluded.label_fi,
label_sv = excluded.label_sv;
