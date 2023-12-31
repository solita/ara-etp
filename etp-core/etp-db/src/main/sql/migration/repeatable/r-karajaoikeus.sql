insert into karajaoikeus (id, ordinal, label_fi, label_sv)
values (0, 1, 'Ahvenanmaan käräjäoikeus', 'Ålands tingsrätter'),
       (1, 2, 'Etelä-Karjalan käräjäoikeus', 'Södrä Karelens tingsrätter'),
       (2, 3, 'Etelä-Pohjanmaan käräjäoikeus', ' tingsrätter'),
       (3, 4, 'Etelä-Savon käräjäoikeus', 'Södrä Savolax tingsrätter'),
       (4, 5, 'Helsingin käräjäoikeus', 'Helsingfors tingsrätter'),
       (5, 6, 'Itä-Uudenmaan käräjäoikeus', 'Östra Nylands tingsrätter'),
       (6, 7, 'Kainuun käräjäoikeus', 'Kajanalands tingsrätter'),
       (7, 8, 'Kanta-Hämeen käräjäoikeus', 'Egentliga Tavastlands tingsrätter'),
       (8, 9, 'Keski-Suomen käräjäoikeus', 'Mellersta Finlands tingsrätter'),
       (9, 10, 'Kymenlaakson käräjäoikeus', 'Kymmenedalens tingsrätter'),
       (10, 11, 'Lapin käräjäoikeus', 'Lapplands tingsrätter'),
       (11, 12, 'Länsi-Uudenmaan käräjäoikeus', 'Västrä Nylands tingsrätter'),
       (12, 13, 'Oulun käräjäoikeus', 'Uleåborgs tingsrätter'),
       (13, 14, 'Pirkanmaan käräjäoikeus', 'Birkalands tingsrätter'),
       (14, 15, 'Pohjanmaan käräjäoikeus', 'Österbottens tingsrätter'),
       (15, 16, 'Pohjois-Karjalan käräjäoikeus', 'Norra Karelens tingsrätter'),
       (16, 17, 'Pohjois-Savon käräjäoikeus', 'Norra Savolax tingsrätter'),
       (17, 18, 'Päijät-Hameen käräjäoikeus', 'Päijänne-Tavastlands tingsrätter'),
       (18, 19, 'Satakunnan käräjäoikeus', 'Satakunta tingsrätter'),
       (19, 20, 'Varsinais-Suomen käräjäoikeus', 'Egentliga Finlands tingsrätter')
on conflict (id) do update set label_fi = excluded.label_fi,
                               label_sv = excluded.label_sv,
                               ordinal  = excluded.ordinal;
