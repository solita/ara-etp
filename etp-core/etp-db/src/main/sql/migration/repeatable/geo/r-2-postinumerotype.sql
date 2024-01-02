insert into postinumerotype (id, label_fi, label_sv, ordinal)
values
(1, 'Normaalipostinumero', 'Normaalipostinumero', 2),
(2, 'Postilokeropostinumero', 'Postilokeropostinumero', 3),
(3, 'Yrityspostinumero', 'Yrityspostinumero', 4),
(4, 'Koontipalvelupostinumero', 'Koontipalvelupostinumero', 5),
(5, 'Vastauslähetyspostinumero', 'Vastauslähetyspostinumero', 6),
(6, 'SmartPOST (pakettiautomaatti)', 'SmartPOST (pakettiautomaatti)', 7),
(7, 'Noutopiste', 'Noutoposte', 8),
(8, 'Tekninen postinumero', 'Tekninen postinumero', 9)
on conflict (id) do update set
  label_fi = excluded.label_fi,
  label_sv = excluded.label_sv,
  ordinal = excluded.ordinal;
