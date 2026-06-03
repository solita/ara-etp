ALTER TABLE Perusparannuspassi
  ADD COLUMN rpt$uusiutuva_energia_lahtotilanne integer references perusparannuspassi_uusiutuva_energia(id);
