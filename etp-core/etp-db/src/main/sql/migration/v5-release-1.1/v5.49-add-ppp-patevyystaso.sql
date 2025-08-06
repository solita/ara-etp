-- Add new patevyystaso for PPP certification

INSERT INTO patevyystaso (id, label_fi, label_sv, valid)
VALUES 
  (3, 'Perustaso + PPP', 'Basnivå + PPP', true),
  (4, 'Ylempi + PPP', 'Högre nivå + PPP', true)
ON CONFLICT (id) DO UPDATE SET
  label_fi = EXCLUDED.label_fi,
  label_sv = EXCLUDED.label_sv,
  valid = EXCLUDED.valid;
