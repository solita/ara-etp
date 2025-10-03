insert into perusparannuspassi_uusiutuva_energia (id, label_fi, ordinal)
values (0, 'Ei ole', 1),
       (1, 'Aurinkosähkö', 2),
       (2, 'Aurinkolämpö', 3),
       (3, 'Aurinkosähkö ja -lämpö', 4),
       (4, 'Tuulivoima', 5),
       (5, 'Muu', 6)
on conflict (id) do update set label_fi = excluded.label_fi,
                               ordinal  = excluded.ordinal;

insert into perusparannuspassi_jaahdytys (id, label_fi, ordinal)
values (0, 'Ei ole', 1),
       (1, 'Aurinkosuojaukset', 2),
       (2, 'Muu', 3)
on conflict (id) do update set label_fi = excluded.label_fi,
                               ordinal  = excluded.ordinal;

insert into perusparannuspassi_liittymismahdollisuus(id, label_fi, ordinal)
values (0, 'Kyllä, kaukolämpöön', 1),
       (1, 'Kyllä, Kaukojäähdytykseen', 2),
       (2, 'Kyllä, molempiin', 3),
       (3, 'Ei kumpaankaan', 4)
on conflict (id) do update set label_fi = excluded.label_fi,
                               ordinal  = excluded.ordinal;
