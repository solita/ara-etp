INSERT INTO ilmanvaihtotyyppi (id, label_fi, label_sv, ordinal)
VALUES
(0, 'Painovoimainen ilmanvaihtojärjestelmä', 'Självdragsventilationssystem', 1),
(1, 'Koneellinen poistoilmanvaihtojärjestelmä', 'Maskinellt frånluftsventilationssystem', 2),
(2, 'Koneellinen tulo- ja poistoilmanvaihtojärjestelmä', 'Maskinellt till- och frånluftsventilationssystem', 4),
(3, 'Poistoilmalämpöpumppu (vain käyttötarkoitusluokka 1)', 'Frånluftsvärmepump (endast användningsklass 1)', 6),
(4, 'Painovoimainen/koneellinen poisto', 'Naturlig/maskinell frånluftsventilation', 7),
(5, 'Painovoimainen/koneellinen tulo- ja poisto', 'Naturlig/maskinell till- och frånluftsventilation', 8),
(6, 'Muu ilmanvaihtojärjestelmä', 'Annat ventilationssystem', 9),
(7, 'Koneellinen poistoilmanvaihtojärjestelmä lämmöntalteenotolla', 'Mekaniskt frånluftsventilationssystem med värmeåtervinning', 3),
(8, 'Koneellinen tulo- ja poistoilmanvaihtojärjestelmä lämmöntalteenotolla', 'Mekaniskt till- och frånluftsventilationssystem med värmeåtervinning', 5)

ON CONFLICT (id) DO UPDATE
SET
label_fi = excluded.label_fi,
label_sv = excluded.label_sv;
