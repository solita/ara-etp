insert into vastaanottajaryhma (id, label_fi, label_sv, ordinal)
values
(0, 'Valvojat', 'Inspektör', 1),
(1, 'Laatijat', 'Upprättare', 2),
(2, 'Laskuttajat', 'Laskuttajat', 3)
on conflict (id) do update set
  label_fi = excluded.label_fi,
  label_sv = excluded.label_sv;