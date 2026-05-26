insert into rooli (id, label_fi, label_sv)
values
(-1, 'Järjestelmä', 'System'),
(0, 'Laatija', 'Utarbetad av'),
(1, 'Pätevyyden toteaja', 'Behörighet konstaterats av'),
(2, 'Pääkäyttäjä', 'Huvudanvändare'),
(3, 'Laskuttaja', 'Fakturare'),
(4, 'Aineistoasiakas', 'Material kund')
on conflict (id) do update set
  label_fi = excluded.label_fi,
  label_sv = excluded.label_sv;
