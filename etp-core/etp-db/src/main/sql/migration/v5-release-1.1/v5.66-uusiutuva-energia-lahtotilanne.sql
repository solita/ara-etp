alter table perusparannuspassi
  add column rpt$uusiutuva_energia_lahtotilanne integer references perusparannuspassi_uusiutuva_energia(id);
