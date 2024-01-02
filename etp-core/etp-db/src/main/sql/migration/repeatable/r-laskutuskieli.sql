INSERT INTO laskutuskieli (id, label_fi, label_sv)
VALUES
(0, 'Suomi', 'Finska'),
(1, 'Ruotsi', 'Svenska'),
(2, 'Englanti', 'Engelska')
ON CONFLICT (id) DO UPDATE
SET label_fi = excluded.label_fi,
label_sv = excluded.label_sv
