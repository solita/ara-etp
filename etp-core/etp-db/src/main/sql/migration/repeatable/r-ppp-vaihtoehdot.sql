insert into perusparannuspassi_uusiutuva_energia (id, label_fi, label_sv, ordinal)
values (0, 'Ei ole','Ei ole-sv?', 1),
       (1, 'Aurinkosähkö', 'Aurinkosähkö-sv?',2),
       (2, 'Aurinkolämpö', 'Aurinkolämpö-sv?',3),
       (3, 'Aurinkosähkö ja -lämpö', 'Aurinkosähkö ja -lämpö-sv?', 4),
       (4, 'Tuulivoima', 'Tuulivoima-sv?',5),
       (5, 'Muu', 'Muu-sv?',6)
on conflict (id) do update set label_fi = excluded.label_fi,
                               label_sv = excluded.label_sv,
                               ordinal  = excluded.ordinal;

insert into perusparannuspassi_jaahdytys (id, label_fi, label_sv, ordinal)
values (0, 'Ei ole', 'Ei ole-sv?',1),
       (1, 'Aurinkosuojaukset', 'Aurinkosuojaukset-sv?', 2),
       (2, 'Muu', 'Muu-sv?',3)
on conflict (id) do update set label_fi = excluded.label_fi,
                               label_sv = excluded.label_sv,
                               ordinal  = excluded.ordinal;

insert into perusparannuspassi_liittymismahdollisuus(id, label_fi, label_sv, ordinal)
values (0, 'Kyllä, kaukolämpöön', 'Kyllä, kaukolämpöön-sv?',1),
       (1, 'Kyllä, Kaukojäähdytykseen', 'Kyllä, kaukolämpöön-sv?', 2),
       (2, 'Kyllä, molempiin', 'Kyllä, molempiin-sv?', 3),
       (3,  'Ei kumpaankaan', 'Ei kumpaankaan-sv?',4)
on conflict (id) do update set label_fi = excluded.label_fi,
                               label_sv = excluded.label_sv,
                               ordinal  = excluded.ordinal;
