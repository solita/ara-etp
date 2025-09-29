insert into ppp_energiatehokkuus_mahdollisuus (id, label_fi, ordinal)
values
(0,  'Kyllä, kaukolämpöön', 1),
(1,  'Kyllä, Kaukojäähdytykseen', 2),
(2,  'Kyllä, molempiin', 3),
(3,  'Ei kumpaankaan',4)

on conflict (id) do update set
label_fi = excluded.label_fi,
ordinal = excluded.ordinal;

insert into ppp_paalammitysjarjestelma (id, label_fi, ordinal)
values
(0,  'Kaukolämpö', 1),
(1,  'Sähkö', 2),
(2,  'Puu', 3),
(3,  'Pelletti',4),
(4,  'Öljy',5),
(5,  'Kaasu',6),
(6,  'Maalämpöpumppu',7),
(7,  'Vesi-ilmalämpöpumppu',8),
(8,  'Poistoilmalämpöpumppu',9),
(9,  'Muu läämmitysjärjestelmä, mikä',10)

on conflict (id) do update set
label_fi = excluded.label_fi,
ordinal = excluded.ordinal;

insert into ppp_ilmanvaihto (id, label_fi, ordinal)
values
(0,  'Painovoimainen ilmanvaihtojärjestelmä', 1),
(1,  'Koneellinen poistoilmanvaihtojärjestelmä', 2),
(2,  'Koneellinen tulo- ja poistoilmanvaihtojärjestelmä', 3),
(3,  'Poistoilmalämpöpumppu (vain käyttötarkoitusluokka 1)',4),
(4,  'Painovoimainen/koneellinen poisto',5),
(5,  'Painovoimainen/koneellinen tulo- ja poisto',6),
(6,  'Koneellinen poistoilmanvaihtojärjestelmä lämmöntalteenotolla',7),
(7,  'Koneellinen tulo- ja poistoilmanvaihtojärjestelmä lämmöntalteenotolla',8),
(8,  'Muu ilmanvaihtojärjestelmä, mikä',9)

on conflict (id) do update set
label_fi = excluded.label_fi,
ordinal = excluded.ordinal;

insert into ppp_uusiutuva_energia (id, label_fi, ordinal)
values
(0,  'Ei ole', 1),
(1,  'Aurinkosähkö', 2),
(2,  'Aurinkolämpö', 3),
(3,  'Aurinkosähkö ja -lämpö',4),
(4,  'Tuulivoima',5),
(5,  'Muu',6)

on conflict (id) do update set
label_fi = excluded.label_fi,
ordinal = excluded.ordinal;

insert into ppp_jaahdytys (id, label_fi, ordinal)
values
(0,  'Ei ole', 1),
(1,  'Aurinkosuojaukset', 2),
(2,  'Muu', 3)

on conflict (id) do update set
label_fi = excluded.label_fi,
ordinal = excluded.ordinal;


insert into ppp_toimenpide_ehdotus (id, label_fi, ordinal)
values
(0,  'Siirtyminen kaukolämpöön', 1),
(1,  'Maalämpöpumppu', 2),
(2,  'Ilma-vesilämpöpumppu', 3),
(3,  'Poistoilmalämpöpumppu',4),
(4,  'Muu lämpöpumppu',5),
(5,  'Hybridilämmitys',6),
(6,  'Bioenergia',7),
(7,  'Muu lämmitystoimenpide',8),
(8,  'Käyttövesivaraajan lisäys/muutos',9),
(9,  'Vakiopaineventtiili',10),
(10,  'Muu lämpimän käyttöveden toimenpide',11),
(11,  'Koneellisen ilmanvaihdon muutos',12),
(12,  'Lämmöntalteenoton lisäys/parannus',13),
(13,  'Ilmanvaihtokoneen uusiminen',14),
(14,  'Tarpeenmukainen ilmanvaihto',15),
(15,  'Muu ilmanvaihdon toimenpide',16),
(16,  'Valaistuksen tehotiheyden muutos',17),
(17,  'Tarpeenmukainen valaistus',18),
(18,  'Muu valaistuksen toimenpide',19),
(19,  'Siirtyminen kaukojäähdytykseen',20),
(20,  'Jäähdytyskoneen muutos/uusiminen',21),
(21,  'Passiivisen jäähdytyksen lisääminen',22),
(22,  'Muu jäähdytystoimenpide',23),
(23,  'Aurinkosähkö',24),
(24,  'Aurinkolämpö',25),
(25,  'Tuulivoima',26),
(26,  'Muu uusiutuvan energian toimenpide',27),
(27,  'Muu ikkunatoimenpide',28),
(28,  'Muu ulko-ovitoimenpide',29),
(29,  'Muu yläpohjan toimenpide',30),
(30,  'Muu alapohjan toimenpide',31),
(31,  'Tiiveyden parantaminen',32),
(32,  'Muu julkisivun toimenpide',33),
(33,  'Energian varastointi',34),
(34,  'Kulutusjousto',35),
(35,  'Varaavat tulisijat',36),
(36,  'Lämmönjaon hyötysuhteen muutos',37),
(37,  'Lämmitysverkoston eristyksen muutos',38),
(38,  'Energiavaraajan muutos/lisäys',39),
(39,  'Lämmitysverkoston eristyksen muutos',40),
(40,  'Märkätilojen lämmityksen muutos',41),
(41,  'Lämmin käyttövesi',42),
(42,  'Lämpimän käyttövesiverkoston eristyksen muutos',43),
(43,  'Lämpimän käyttöveden kiertojohdon muutos',44),
(44,  'Painovoimaisen IV:n muutos koneelliseksi',45),
(45,  'Jäteveden lämmöntalteenotto',46)

on conflict (id) do update set
label_fi = excluded.label_fi,
ordinal = excluded.ordinal;
