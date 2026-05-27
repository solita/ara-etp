insert into perusparannuspassi_uusiutuva_energia (id, label_fi, label_sv, ordinal)
values (0, 'Ei ole','Ei ole', 1),
       (1, 'Aurinkosähkö', 'Solel',2),
       (2, 'Aurinkolämpö', 'Solvärme',3),
       (3, 'Aurinkosähkö ja -lämpö', 'Solel och solvärme', 4),
       (4, 'Tuulivoima', 'Vindkraft',5),
       (5, 'Muu', 'Annan',6)
on conflict (id) do update set label_fi = excluded.label_fi,
                               label_sv = excluded.label_sv,
                               ordinal  = excluded.ordinal;

insert into perusparannuspassi_jaahdytys (id, label_fi, label_sv, ordinal)
values (0, 'Ei ole', 'Finns inte',1),
       (1, 'Aurinkosuojaukset', 'Solavskärmningar', 2),
       (2, 'Muu', 'Annan',3)
on conflict (id) do update set label_fi = excluded.label_fi,
                               label_sv = excluded.label_sv,
                               ordinal  = excluded.ordinal;

insert into perusparannuspassi_liittymismahdollisuus(id, label_fi, label_sv, ordinal)
values (0, 'Kyllä, kaukolämpöön', 'Ja, för fjärrvärme',1),
       (1, 'Kyllä, Kaukojäähdytykseen', 'Ja, för fjärrvärme', 2),
       (2, 'Kyllä, molempiin', 'Ja, för båda', 3),
       (3, 'Ei kumpaankaan', 'Ingendera',4)
on conflict (id) do update set label_fi = excluded.label_fi,
                               label_sv = excluded.label_sv,
                               ordinal  = excluded.ordinal;
