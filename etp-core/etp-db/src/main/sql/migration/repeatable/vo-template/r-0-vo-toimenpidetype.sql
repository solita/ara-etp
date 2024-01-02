insert into vo_toimenpidetype (id, label_fi, label_sv, ordinal)
values
(0,  'Katsottu', 'Läst', 1),
(1,  'Poikkeama', 'Avvikelse', 2),
(2,  'Valvonnan aloitus', 'Inledande av kontroll', 3),
(3,  'Tietopyyntö', 'Begäran om inlämning', 4),
(4,  'Tietopyyntö / Vastaus', 'Begäran om inlämning / svar', 5),
(5,  'Tietopyyntö / Kehotus', 'Begäran om inlämning / uppmaning', 6),
(6,  'Tietopyyntö / Varoitus', 'Begäran om inlämning / varning', 7),
(7,  'Valvontamuistio', 'Övervaknings-pm', 8),
(8,  'Valvontamuistio / Vastaus', 'Övervaknings-pm / svar', 9),
(9,  'Valvontamuistio / Kehotus', 'Övervaknings-pm / uppmaning', 10),
(10, 'Valvontamuistio / Varoitus', 'Övervaknings-pm  / varning', 11),
(11, 'Lisäselvityspyyntö', 'Begäran om tilläggsutredning', 12),
(12, 'Lisäselvityspyyntö / Vastaus', 'Begäran om tilläggsutredning / svar', 13),
(13, 'Kieltopäätös', 'Förbudsbeslut', 14),
(14, 'Valvonnan lopetus', 'Avslutande av kontroll', 15)
on conflict (id) do update set
  label_fi = excluded.label_fi,
  label_sv = excluded.label_sv,
  ordinal = excluded.ordinal;