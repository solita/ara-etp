
insert into stat_kayttotarkoitusluokka (id, label_fi, label_sv, ordinal, valid)
values
  (1, '1A-C. Pienet asuinrakennukset',
      '1A-C. Små bostadsbyggnader', 1, true),
  (2, '1D. Rivitalot ja 2-kerroksiset asuinkerrostalot',
      '1D. Radhus och flervåningsbostadshus med bostäder i högst två våningar', 2, true),
  (3, '2. Asuinkerrostalot', '2. Flervåningsbostadshus', 3, true),
  (4, '3. Toimistorakennukset', '3. Kontorsbyggnader', 4, true),
  (5, '4. Liikerakennukset', '4. Affärsbyggnader', 5, true),
  (6, '5. Majoitusliikerakennukset', '5. Byggnader för inkvarteringsanläggningar', 6, true),
  (7, '6. Opetusrakennukset ja päiväkodit', '6. Undervisningsbyggnader och daghem', 7, true),
  (8, '7. Liikuntahallit', '7. Idrottshallar', 8, true),
  (9, '8. Sairaalat', '8. Sjukhus', 9, true),
  (10, '9. Muut rakennukset', '9. Övriga byggnader', 10, true)

on conflict (id) do update
  set label_fi = excluded.label_fi,
      label_sv = excluded.label_sv,
      valid = excluded.valid;
