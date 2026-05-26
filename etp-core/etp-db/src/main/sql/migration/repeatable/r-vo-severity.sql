insert into vo_severity (id, label_fi, label_sv, ordinal)
values
(0, 'Ei huomautettavaa', 'Inget att anmärka', 1),
(1, 'Puutteet ei edellytä toimenpiteitä', 'Bristerna kräver inte åtgärder', 2),
(2, 'Oleellisesti virheellinen', 'Väsentligt felaktig', 3)
on conflict (id) do update set
  label_fi = excluded.label_fi,
  label_sv = excluded.label_sv,
  ordinal = excluded.ordinal;
