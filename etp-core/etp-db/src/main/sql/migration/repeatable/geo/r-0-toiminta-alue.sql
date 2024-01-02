
-- Conversion
-- select '(''' || koodi || ''', ''' || nimi || ''', ''' || nimisv || '''),'
-- from etp.hallintoalue order by nimi;

insert into toimintaalue (id, nuts_code, label_fi, label_sv)
values
(0,  'FI187', 'Etelä-Karjala', 'Södra Karelen'),
(1,  'FI194', 'Etelä-Pohjanmaa', 'Södra Österbotten'),
(2,  'FI131', 'Etelä-Savo', 'Södra Savolax'),
(3,  'FI134', 'Kainuu', 'Kajanaland'),
(4,  'FI184', 'Kanta-Häme', 'Egentliga Tavastland'),
(5,  'FI1A1', 'Keski-Pohjanmaa', 'Mellersta Österbotten'),
(6,  'FI193', 'Keski-Suomi', 'Mellersta Finland'),
(7,  'FI186', 'Kymenlaakso', 'Kymmenedalen'),
(8,  'FI1A3', 'Lappi', 'Lappland'),
(9,  'FI185', 'Päijät-Häme', 'Päijänne-Tavastland'),
(10, 'FI192', 'Pirkanmaa', 'Birkaland'),
(11, 'FI195', 'Pohjanmaa', 'Österbotten'),
(12, 'FI133', 'Pohjois-Karjala', 'Norra Karelen'),
(13, 'FI1A2', 'Pohjois-Pohjanmaa', 'Norra Österbotten'),
(14, 'FI132', 'Pohjois-Savo', 'Norra Savolax'),
(15, 'FI191', 'Satakunta', 'Satakunda'),
(16, 'FI181', 'Uusimaa', 'Nyland'),
(17, 'FI183', 'Varsinais-Suomi', 'Egentliga Finland')
on conflict (id) do update set
  nuts_code = excluded.nuts_code,
  label_fi = excluded.label_fi,
  label_sv = excluded.label_sv;
