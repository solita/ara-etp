insert into vk_toimenpidetype (id, label_fi, label_sv, ordinal, valid, manually_deliverable, allow_comments)
values
(0, 'Valvonnan aloitus', 'Valvonnan aloitus (sv)', 1, true, false, false),
(1, 'Tietopyyntö 2021', 'Begäran om uppgifter 2021', 2, false, false, false),
(2, 'Kehotus', ' Uppmaning', 3, true, false, false),
(3, 'Varoitus', 'Varning', 4, true, false, false),
(4, 'Käskypäätös', 'Käskypäätös (sv)', 5, false, false, false),
(5, 'Valvonnan lopetus', 'Valvonnan lopetus (sv)', 6, true, false, false),
(6, 'HaO-käsittely', 'HaO-käsittely (sv)', 7, true, false, true),
(7, 'Käskypäätös / kuulemiskirje', 'Käskypäätös / kuulemiskirje (sv)', 8, true, true, true),
(8, 'Käskypäätös / varsinainen päätös', 'Käskypäätös / varsinainen päätös (sv)', 9, true, true, true),
(9, 'Käskypäätös / tiedoksianto (ensimmäinen postitus)', 'Käskypäätös / tiedoksianto (ensimmäinen postitus) (sv)', 10, true, true, true),
(10, 'Käskypäätös / tiedoksianto (toinen postitus)', 'Käskypäätös / tiedoksianto (toinen postitus) (sv)', 11, true, true, true),
(11, 'Käskypäätös / tiedoksianto (Haastemies)', 'Käskypäätös / tiedoksianto (Haastemies) (sv)', 12, true, true, true),
(12, 'Käskypäätös / valitusajan odotus ja umpeutuminen', 'Käskypäätös / valitusajan odotus ja umpeutuminen (sv)', 13, true, false, true),
(14, 'Sakkopäätös / kuulemiskirje', 'Sakkopäätös / kuulemiskirje (sv)', 15, true, true, true),
(15, 'Sakkopäätös / varsinainen päätös', 'Sakkopäätös / varsinainen päätös (sv)', 16, true, true, true),
(16, 'Sakkopäätös / tiedoksianto (ensimmäinen postitus)', 'Sakkopäätös / tiedoksianto (ensimmäinen postitus) (sv)', 17, true, true, true),
(17, 'Sakkopäätös / tiedoksianto (toinen postitus)', 'Sakkopäätös / tiedoksianto (toinen postitus) (sv)', 18, true, true, true),
(18, 'Sakkopäätös / tiedoksianto (Haastemies)', 'Sakkopäätös / tiedoksianto (Haastemies) (sv)', 19, true, true, true),
(19, 'Sakkopäätös / valitusajan odotus ja umpeutuminen', 'Sakkopäätös / valitusajan odotus ja umpeutuminen (sv)', 20, true, false, true),
(21, 'Sakkoluettelon lähetys menossa', 'Sakkoluettelon lähetys menossa (sv)', 22, true, false, true)
on conflict (id) do update set
  label_fi = excluded.label_fi,
  label_sv = excluded.label_sv,
  ordinal = excluded.ordinal,
  valid = excluded.valid,
  manually_deliverable = excluded.manually_deliverable,
  allow_comments = excluded.allow_comments;
