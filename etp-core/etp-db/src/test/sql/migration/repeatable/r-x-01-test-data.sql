set session_replication_role = 'replica';
--
-- PostgreSQL database dump
--

-- Dumped from database version 15.2
-- Dumped by pg_dump version 15.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: kayttaja; Type: TABLE DATA; Schema: audit; Owner: etp
--

INSERT INTO audit.kayttaja VALUES
	(1, 877, '2024-02-22 11:30:00.626866+00', 0, 'database.etp', 0, 'database', 'etp', 'database@energiatodistusrekisteri.fi', '0501234567', false, -1, false, NULL, NULL, NULL, NULL, false, NULL, NULL, NULL, NULL),
	(2, 966, '2024-02-22 11:30:02.571107+00', 0, 'database.etp', -1, 'conversion', 'etp', 'conversion@energiatodistusrekisteri.fi', '0501234567', false, -1, false, NULL, NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(3, 966, '2024-02-22 11:30:02.571107+00', 0, 'database.etp', -2, 'laskutus', 'etp', 'laskutus@energiatodistusrekisteri.fi', '0501234567', false, -1, false, NULL, NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(4, 966, '2024-02-22 11:30:02.571107+00', 0, 'database.etp', -3, 'communication', 'etp', 'communication@energiatodistusrekisteri.fi', '0501234567', false, -1, false, NULL, NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(5, 966, '2024-02-22 11:30:02.571107+00', 0, 'database.etp', -4, 'presigned', 'etp', 'presigned@energiatodistusrekisteri.fi', '0501234567', false, -1, false, NULL, NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(6, 966, '2024-02-22 11:30:02.571107+00', 0, 'database.etp', -5, 'aineisto', 'etp', 'aineisto@energiatodistusrekisteri.fi', '0501234567', false, -1, false, NULL, NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(7, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 1, 'Pauli', 'Pätevyyden toteaja', 'patevyydentoteaja@solita.fi', '0451234567', false, 1, false, '010280-952L', NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(8, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 2, 'Liisa', 'Specimen-Potex', 'laatija@solita.fi', '0451234567', false, 0, false, '010469-999W', NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(9, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 3, 'Kalevi', 'Specimen-Potex', 'kumppani@solita.fi', '0451234567', false, 0, false, '141199-999N', NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(10, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 4, 'Harri', 'Specimen-Potex', 'harri.lindberg@solita.fi', '0451234567', false, 0, false, '010675-9981', NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(11, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 5, 'Aleksi', 'Kallan', 'aleksi.kallan@solita.fi', '0451234567', false, 0, false, '261298-998X', NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(12, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 6, 'Ulla', 'Specimen-Pirix', 'ulla.laapotti@ara.fi', '0451234567', false, 0, false, '040265-9985', NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(13, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 7, 'Kirsi', 'Juutilainen', 'kirsi.unhonen@ara.fi', '0451234567', false, 0, false, '260991-999R', NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(14, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 8, 'Ari', 'Manninen', 'ari.manninen@ara.fi', '0451234567', false, 0, false, '010101-123N', NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(15, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 9, 'Risto', 'Jesoi', 'risto.jesoi@ara.fi', '0451234567', false, 0, false, '180883-998N', NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(16, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 10, 'Lamitor', 'Laatija', 'laatija@lamit.fi', '0451234567', false, 0, false, '110106A998M', NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(17, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 11, 'Riuska', 'Laatija', 'laatija@granlund.fi', '0451234567', false, 0, false, '290574-9981', NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(18, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 12, 'Cadmatic', 'Laatija', 'laatija@cadmatic.com', '0451234567', false, 0, false, '050391-999B', NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(19, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 13, 'Timbal', 'Laatija', 'laatija@timbal.fi', '0451234567', false, 0, false, '120997-9998', NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(20, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 14, 'Etlas', 'Laatija', 'laatija@etlas.fi', '0451234567', false, 0, false, '271258-9988', NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(21, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 15, 'Caverion', 'Laatija', 'laatija@caverion.fi', '0451234567', false, 0, false, '010170-960F', NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(22, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 16, 'Laskentapalvelut', 'Laatija', 'laatija@dof.fi', '0451234567', false, 0, false, '010170-999R', NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(23, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 17, 'Laskentaohjelmat', 'Laatija', 'laatija@example.com', '0451234567', false, 0, false, '081181-9984', NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(24, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 18, 'Päivi', 'Pääkäyttäjä', 'paakayttaja@solita.fi', '0501234567', false, 2, false, NULL, NULL, 'vvirkamies', 'testivirasto.fi', false, NULL, '', NULL, NULL),
	(25, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 19, 'Paavo', 'Pääkäyttäjä', 'paakayttaja2@solita.fi', '0501234567', false, 2, false, NULL, NULL, 'vvirkamies3', 'testivirasto.fi', false, NULL, '', NULL, NULL),
	(26, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 20, 'Lasse', 'Laskuttaja', 'laskuttaja@solita.fi', '0501234567', false, 3, false, NULL, NULL, 'vvirkamies2', 'testausvirasto.fi', false, NULL, '', NULL, NULL) ON CONFLICT DO NOTHING;


--
-- Data for Name: laatija; Type: TABLE DATA; Schema: audit; Owner: etp
--

INSERT INTO audit.laatija VALUES
	(1, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 2, 2, '2020-01-01', 'KIINKO', false, NULL, '{}', NULL, false, false, false, false, 0, 'Peltokatu 26', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000001', false, false),
	(2, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 3, 2, '2020-01-01', 'KIINKO', false, NULL, '{}', NULL, false, false, false, false, 0, 'Peltokatu 26', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000002', true, false),
	(3, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 4, 2, '2020-01-01', 'KIINKO', false, NULL, '{}', NULL, false, false, false, false, 0, 'Peltokatu 26', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000003', false, false),
	(4, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 5, 2, '2020-01-01', 'KIINKO', false, NULL, '{}', NULL, false, false, false, false, 0, 'Peltokatu 26', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000004', false, false),
	(5, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 6, 2, '2020-01-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Kirkkokatu 12', NULL, '15140', 'Lahti', 'FI', NULL, 'L000000005', false, false),
	(6, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 7, 2, '2020-01-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Kirkkokatu 12', NULL, '15140', 'Lahti', 'FI', NULL, 'L000000006', false, false),
	(7, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 8, 2, '2020-01-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Kirkkokatu 12', NULL, '15140', 'Lahti', 'FI', NULL, 'L000000007', false, false),
	(8, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 9, 2, '2020-01-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Kirkkokatu 12', NULL, '15140', 'Lahti', 'FI', NULL, 'L000000008', false, false),
	(9, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 10, 1, '2020-10-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Hämeenkatu 1', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000009', false, false),
	(10, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 11, 1, '2020-10-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Hämeenkatu 1', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000010', false, false),
	(11, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 12, 1, '2020-10-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Hämeenkatu 1', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000011', false, false),
	(12, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 13, 1, '2020-10-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Hämeenkatu 1', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000012', false, false),
	(13, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 14, 1, '2020-10-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Hämeenkatu 1', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000013', false, false),
	(14, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 15, 1, '2020-10-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Hämeenkatu 1', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000014', false, false),
	(15, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 16, 1, '2020-10-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Hämeenkatu 1', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000015', false, false),
	(16, 1002, '2024-02-22 11:30:03.046259+00', 0, 'database.etp', 17, 1, '2020-10-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Hämeenkatu 1', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000016', false, false) ON CONFLICT DO NOTHING;


--
-- Data for Name: vk_henkilo; Type: TABLE DATA; Schema: audit; Owner: etp
--



--
-- Data for Name: vk_yritys; Type: TABLE DATA; Schema: audit; Owner: etp
--



--
-- Name: kayttaja_event_id_seq; Type: SEQUENCE SET; Schema: audit; Owner: etp
--

SELECT pg_catalog.setval('audit.kayttaja_event_id_seq', 26, true);


--
-- Name: laatija_event_id_seq; Type: SEQUENCE SET; Schema: audit; Owner: etp
--

SELECT pg_catalog.setval('audit.laatija_event_id_seq', 16, true);


--
-- Name: vk_henkilo_event_id_seq; Type: SEQUENCE SET; Schema: audit; Owner: etp
--

SELECT pg_catalog.setval('audit.vk_henkilo_event_id_seq', 1, false);


--
-- Name: vk_yritys_event_id_seq; Type: SEQUENCE SET; Schema: audit; Owner: etp
--

SELECT pg_catalog.setval('audit.vk_yritys_event_id_seq', 1, false);


--
-- PostgreSQL database dump complete
--

--
-- PostgreSQL database dump
--

-- Dumped from database version 15.2
-- Dumped by pg_dump version 15.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: kayttaja; Type: TABLE DATA; Schema: etp; Owner: etp
--

INSERT INTO etp.kayttaja VALUES
	(1, 'Pauli', 'Pätevyyden toteaja', 'patevyydentoteaja@solita.fi', '0451234567', false, 1, NULL, false, '010280-952L', NULL, NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(2, 'Liisa', 'Specimen-Potex', 'laatija@solita.fi', '0451234567', false, 0, NULL, false, '010469-999W', NULL, NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(3, 'Kalevi', 'Specimen-Potex', 'kumppani@solita.fi', '0451234567', false, 0, NULL, false, '141199-999N', NULL, NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(4, 'Harri', 'Specimen-Potex', 'harri.lindberg@solita.fi', '0451234567', false, 0, NULL, false, '010675-9981', NULL, NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(5, 'Aleksi', 'Kallan', 'aleksi.kallan@solita.fi', '0451234567', false, 0, NULL, false, '261298-998X', NULL, NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(6, 'Ulla', 'Specimen-Pirix', 'ulla.laapotti@ara.fi', '0451234567', false, 0, NULL, false, '040265-9985', NULL, NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(7, 'Kirsi', 'Juutilainen', 'kirsi.unhonen@ara.fi', '0451234567', false, 0, NULL, false, '260991-999R', NULL, NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(8, 'Ari', 'Manninen', 'ari.manninen@ara.fi', '0451234567', false, 0, NULL, false, '010101-123N', NULL, NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(9, 'Risto', 'Jesoi', 'risto.jesoi@ara.fi', '0451234567', false, 0, NULL, false, '180883-998N', NULL, NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(10, 'Lamitor', 'Laatija', 'laatija@lamit.fi', '0451234567', false, 0, NULL, false, '110106A998M', NULL, NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(11, 'Riuska', 'Laatija', 'laatija@granlund.fi', '0451234567', false, 0, NULL, false, '290574-9981', NULL, NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(12, 'Cadmatic', 'Laatija', 'laatija@cadmatic.com', '0451234567', false, 0, NULL, false, '050391-999B', NULL, NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(13, 'Timbal', 'Laatija', 'laatija@timbal.fi', '0451234567', false, 0, NULL, false, '120997-9998', NULL, NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(14, 'Etlas', 'Laatija', 'laatija@etlas.fi', '0451234567', false, 0, NULL, false, '271258-9988', NULL, NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(15, 'Caverion', 'Laatija', 'laatija@caverion.fi', '0451234567', false, 0, NULL, false, '010170-960F', NULL, NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(16, 'Laskentapalvelut', 'Laatija', 'laatija@dof.fi', '0451234567', false, 0, NULL, false, '010170-999R', NULL, NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(17, 'Laskentaohjelmat', 'Laatija', 'laatija@example.com', '0451234567', false, 0, NULL, false, '081181-9984', NULL, NULL, NULL, NULL, false, NULL, '', NULL, NULL),
	(18, 'Päivi', 'Pääkäyttäjä', 'paakayttaja@solita.fi', '0501234567', false, 2, NULL, false, NULL, NULL, 'vvirkamies', 'testivirasto.fi', NULL, false, NULL, '', NULL, NULL),
	(19, 'Paavo', 'Pääkäyttäjä', 'paakayttaja2@solita.fi', '0501234567', false, 2, NULL, false, NULL, NULL, 'vvirkamies3', 'testivirasto.fi', NULL, false, NULL, '', NULL, NULL),
	(20, 'Lasse', 'Laskuttaja', 'laskuttaja@solita.fi', '0501234567', false, 3, NULL, false, NULL, NULL, 'vvirkamies2', 'testausvirasto.fi', NULL, false, NULL, '', NULL, NULL) ON CONFLICT DO NOTHING;


--
-- Data for Name: laatija; Type: TABLE DATA; Schema: etp; Owner: etp
--

INSERT INTO etp.laatija VALUES
	(2, 2, '2020-01-01', 'KIINKO', false, NULL, '{}', NULL, false, false, false, false, 0, 'Peltokatu 26', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000001', false, false),
	(3, 2, '2020-01-01', 'KIINKO', false, NULL, '{}', NULL, false, false, false, false, 0, 'Peltokatu 26', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000002', true, false),
	(4, 2, '2020-01-01', 'KIINKO', false, NULL, '{}', NULL, false, false, false, false, 0, 'Peltokatu 26', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000003', false, false),
	(5, 2, '2020-01-01', 'KIINKO', false, NULL, '{}', NULL, false, false, false, false, 0, 'Peltokatu 26', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000004', false, false),
	(6, 2, '2020-01-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Kirkkokatu 12', NULL, '15140', 'Lahti', 'FI', NULL, 'L000000005', false, false),
	(7, 2, '2020-01-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Kirkkokatu 12', NULL, '15140', 'Lahti', 'FI', NULL, 'L000000006', false, false),
	(8, 2, '2020-01-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Kirkkokatu 12', NULL, '15140', 'Lahti', 'FI', NULL, 'L000000007', false, false),
	(9, 2, '2020-01-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Kirkkokatu 12', NULL, '15140', 'Lahti', 'FI', NULL, 'L000000008', false, false),
	(10, 1, '2020-10-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Hämeenkatu 1', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000009', false, false),
	(11, 1, '2020-10-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Hämeenkatu 1', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000010', false, false),
	(12, 1, '2020-10-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Hämeenkatu 1', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000011', false, false),
	(13, 1, '2020-10-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Hämeenkatu 1', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000012', false, false),
	(14, 1, '2020-10-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Hämeenkatu 1', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000013', false, false),
	(15, 1, '2020-10-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Hämeenkatu 1', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000014', false, false),
	(16, 1, '2020-10-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Hämeenkatu 1', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000015', false, false),
	(17, 1, '2020-10-01', 'FISE', false, NULL, '{}', NULL, false, false, false, false, 0, 'Hämeenkatu 1', NULL, '33100', 'Tampere', 'FI', NULL, 'L000000016', false, false) ON CONFLICT DO NOTHING;


--
-- Data for Name: yritys; Type: TABLE DATA; Schema: etp; Owner: etp
--



--
-- Data for Name: energiatodistus; Type: TABLE DATA; Schema: etp; Owner: etp
--

INSERT INTO etp.energiatodistus VALUES
	(1, 2013, 0, NULL, NULL, 2, NULL, 'Laskuriviviite', NULL, NULL, NULL, false, '2020-04-15', 'Katuosoite', 'Katuosoite SV', 'RK', 'Seuraavia toimenpiteitä voisi tehdä:

 * Toimenpide 1
                                                                                                                      * Toimenpide 2', 'Keskeiset suositukset SV', 2, 'Kiinteistötunnus', NULL, false, NULL, false, 33100, 'Rakennusosa', '101089527F', 'Tilaaja Oy', 2016, 'yrityksen katuosoite', 'Yritys Oy', '33100', 'tampere', 1.6, 1.4, 0.75, 1.5, 1.5, 0.75, 1.9, 0, 0.8, 1, 1, 1, 1.2, 1.2, 0.75, 0.8, 1.2, 0.75, 0.9, 0.5, 0.75, 2.2, 1.3, 0.75, 1, 0, 0.75, 0.4, 0, 1, 2, 1.3, 1, 1, 1, 2, 0, 'ilmanvaihdon kuvaus fi', 'ilmanvaihdon kuvaus sv', 0.65, 21, 0, 0, 3, 2, 1, 3.2, 5000, 1, 2, 2, NULL, NULL, 5, NULL, NULL, 7, NULL, NULL, 1.4, 0.9, 3, 1, 10, 0.1, 1, 2, 1.23, 0.5, 2.24, 1, 10, 0.1, 555, 34, 0.3, 1.235, 12.345, 0.55, 25, 2, 2, 1.5, 1.9, 1500, 0.81, 200, 0.45, 0.5, 0.6, 2, 0.6, 3, 0.1, 8, 322, 'E', 550000, 0, 0, 5000, 0, '{"(Turve,10.5,100000)"}', 100, 200, 300, 500, 400, 'Laskentatyökalu v1.2', 2000, 4000, 3000, 1000, 7, 3, 2, 1, 6, 5, 4, 2, 1, 4, 3, NULL, NULL, NULL, NULL, NULL, NULL, '{"(\"Uusiutuva 1\",\"uusiutuva 2 ruotsiksi\",100)","(,\"uusiutuva 2 ruotsiksi\",)"}', '{"(1,2,4,6,3,5,7,8)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(9,10,12,14,11,13,,15)","(,,16,,,17.12345,18,19)"}', 20000, 40000, 50000, 10000, 40000, 30000, 20000, '{"(\"Ostettu energia 1\",\"ostettu energia 1 ruotsiksi\",1234.56)","(,\"ostettu energia 2 ruotsiksi\",)"}', 1000, 2000, 3000, 4000, '{"(Turve,Kiloa,0.8,1000)","(,,,)","(,,,)"}', 30000, 50000, 'Ylä- ja alapohjan huomiot.', 'Ylä- ja alapohjan huomiot SV.', '{"(\"Ylä- ja alapohjan huomiot ehdotus.\",\"Ylä- ja alapohjan huomiot SV ehdotus.\",1,2,3,4)"}', 'Ilmanvaihto- ja ilmastointijärjestelmien huomiot.', 'Ilmanvaihto- ja ilmastointijärjestelmien huomiot SV.', '{"(\"Ilmanvaihto- ja ilmastointijärjestelmien huomiot ehdotus 1.\",\"Ilmanvaihto- ja ilmastointijärjestelmien huomiot SV ehdotus 1.\",1,5,10,20)","(\"Ilmanvaihto- ja ilmastointijärjestelmien huomiot ehdotus 2.\",\"Ilmanvaihto- ja ilmastointijärjestelmien huomiot SV ehdotus 2.\",10,50,100,200)"}', 'Tilojen ja käyttöveden lämmitysjärjestelmien huomiot.', 'ilojen ja käyttöveden lämmitysjärjestelmien huomiot SV.', '{"(\"Tilojen ja käyttöveden lämmitysjärjestelmien huomiot ehdotus.\",\"ilojen ja käyttöveden lämmitysjärjestelmien huomiot SV ehdotus.\",-200,204.52,-123.45,456.679)"}', 'Valaistuksen, jäähdytysjärjestelmien, sähköisten erillislämmitysten ja muiden järjestelmien huomiot.', 'Valaistuksen, jäähdytysjärjestelmien, sähköisten erillislämmitysten ja muiden järjestelmien huomiot SV.', '{"(\"Valaistuksen, jäähdytysjärjestelmien, sähköisten.... -ehdotus.\",\"Valaistuksen, jäähdytysjärjestelmien, sähköisten erillislämmitysten ... ehdotus SV.\",100,200,300,-400)"}', 'Ulkoseinien, ulko-ovien ja ikkunoiden huomiot.', 'Ulkoseinien, ulko-ovien ja ikkunoiden huomiot SV.', '{"(\"Ulkoseinien, ulko-ovien ja ikkunoiden huomiot ehdotus.\",\"Ulkoseinien, ulko-ovien ja ikkunoiden huomiot ehdotus SV.\",9,8,7,6)"}', 'Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja. Lisätietoja.', 'Lisätietoja SV', 'Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus. Suositus.', 'Suositellut toimenpide-ehdotukset SV.', 'Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä. Lisämerkintöjä.', 'Lisämerkintöjä SV.', false, false, NULL, false, NULL, false, 'Rakennuksen nimi', 'Rakennuksen nimi SV'),
	(2, 2018, 0, NULL, NULL, 2, NULL, 'Laskuriviviite', NULL, NULL, NULL, false, '2020-04-15', 'Katuosoite', 'katuosoite-sv', 'PK', 'Seuraavia toimenpiteitä voisi tehdä:

 * Toimenpide 1
                                                                                                                         * Toimenpide 2', NULL, 0, 'Kiinteistötunnus', 0, false, NULL, true, 33100, 'Rakennusosa', '101089527F', 'Tilaaja Oy', 2019, 'yrityksen katuosoite', 'Yritys Oy', '33100', 'tampere', 1.6, 1.4, 0.54, 1.5, 1.5, 0.56, 1.9, 0, 0.8, 1, 1, 1, 1.2, 1.2, 0.65, 0.8, 1.2, 0.85, 0.9, 0.5, 0.53, 2.2, 1.3, 0.34, 1, 0, 0.75, 0.4, 0, 1, 2, 1.5, 1, 1, 1, 2, 1, 'ilmanvaihdon kuvaus fi', NULL, 0.66, 21, 0, 0, 3, 2, 1, 3.1, 500, 1, 2, 0, NULL, NULL, 9, 'Toissijaisen lämmitysjärjestelmän kuvaus', NULL, 1, NULL, NULL, 1.4, 0.9, 3, 1, 10, 0.1, 1, 2, 1.23, 0.5, 2.24, 1, 10, 0.1, 567, 34.2, 2, 1.235, 12.345, 0.45, 25, 2, 2, 1.5, 1.9, 1500, 0.5, 200, 0.34, 0.5, 0.6, 14, 0.6, 8, 0.6, 14, 50, 'A', 0, 0, 0, 0, 50000, NULL, 100, 200, 300, 500, 400, 'Laskentatyökalu v1.2', 2000, 4000, 3000, 1000, 7, 3, 2, 1, 6, 5, 4, 2, 1, 4, 3, 200, 100, 400, 600, 500, 300, NULL, '{"(1,2,4,6,3,5,7,8)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(9,10,12,14,11,13,,15)","(,,16,,,17.12345,18,19)"}', 20000, 40000, 50000, 10000, 40000, 30000, 20000, NULL, 1000, 2000, 3000, 4000, '{"(\"Vapaa ostettu polttoaine\",Kiloa,0.8,1000)","(,,,)","(,,,)"}', 30000, 50000, 'Ylä- ja alapohjan huomiot.', NULL, '{"(\"Ylä- ja alapohjan huomiot ehdotus.\",,1,2,3,4)"}', 'Ilmanvaihto- ja ilmastointijärjestelmien huomiot.', NULL, '{"(\"Ilmanvaihto- ja ilmastointijärjestelmien huomiot ehdotus 1.\",,1,5,10,20)","(\"Ilmanvaihto- ja ilmastointijärjestelmien huomiot ehdotus 2.\",\"iv muutos 2\",10,50,100,200)"}', 'Tilojen ja käyttöveden lämmitysjärjestelmien huomiot.', 'Lämmityshuomio ruotsiksi!', '{"(\"Tilojen ja käyttöveden lämmitysjärjestelmien huomiot ehdotus.\",\"muutosehdotus ruotsiksi 1\",-200,204.52,-123.45,456.679)"}', 'Valaistuksen, jäähdytysjärjestelmien, sähköisten erillislämmitysten ja muiden järjestelmien huomiot.', NULL, '{"(\"Valaistuksen, jäähdytysjärjestelmien, sähköisten erillislämmitysten ja muiden järjestelmien huomiot.\",,100,200,300,400)"}', 'Ulkoseinien, ulko-ovien ja ikkunoiden huomiot.', NULL, '{"(\"Ulkoseinien, ulko-ovien ja ikkunoiden huomiot ehdotus.\",,9,8,7,6)"}', 'Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja.', NULL, 'Suositellut toimenpide-ehdotukset', NULL, 'Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä.', NULL, false, false, NULL, false, NULL, false, 'Rakennuksen nimi', 'Rakennuksen nimi SV'),
	(3, 2018, 0, NULL, NULL, 2, NULL, 'Laskuriviviite', NULL, NULL, NULL, false, '2020-04-15', 'Katuosoite', 'katuosoite-sv', 'TT', 'Seuraavia toimenpiteitä voisi tehdä:

 * Toimenpide 1
                                                                                                                         * Toimenpide 2', NULL, 0, 'Kiinteistötunnus', 0, false, NULL, true, 33100, 'Rakennusosa', '101089527F', 'Tilaaja Oy', 2018, 'yrityksen katuosoite', 'Yritys Oy', '33100', 'tampere', 1.6, 1.4, 0.54, 1.5, 1.5, 0.56, 1.9, 0, 0.8, 1, 1, 1, 1.2, 1.2, 0.65, 0.8, 1.2, 0.85, 0.9, 0.5, 0.53, 2.2, 1.3, 0.34, 1, 0, 0.75, 0.4, 0, 1, 2, 1.5, 1, 1, 1, 2, 1, 'ilmanvaihdon kuvaus fi', NULL, 0.66, 21, 0, 0, 3, 2, 1, 3.1, 150000, 1, 2, 0, NULL, NULL, 9, 'Toissijaisen lämmitysjärjestelmän kuvaus', NULL, 1, NULL, NULL, 1.4, 0.9, 3, 1, 10, 0.1, 1, 2, 1.23, 0.5, 2.24, 1, 10, 0.1, 567, 34.2, 2, 1.235, 12.345, 0.45, 25, 2, 2, 1.5, 1.9, 1500, 0.5, 200, 0.34, 0.5, 1, 2, 1, 1, 1, 19, 104, 'B', 0, 0, 7000000, 10000000, 7000, NULL, 100, 200, 300, 500, 400, 'Laskentatyökalu v1.2', 2000, 4000, 3000, 1000, 7, 3, 2, 1, 6, 5, 4, 2, 1, 4, 3, 200, 100, 400, 600, 500, 300, NULL, '{"(1,2,4,6,3,5,7,8)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(9,10,12,14,11,13,,15)","(,,16,,,17.12345,18,19)"}', 20000, 40000, 50000, 10000, 40000, 30000, 20000, NULL, 1000, 2000, 3000, 4000, '{"(\"Vapaa ostettu polttoaine\",Kiloa,0.8,1000)","(,,,)","(,,,)"}', 30000, 50000, 'Ylä- ja alapohjan huomiot.', NULL, '{"(\"Ylä- ja alapohjan huomiot ehdotus.\",,1,2,3,4)"}', 'Ilmanvaihto- ja ilmastointijärjestelmien huomiot.', NULL, '{"(\"Ilmanvaihto- ja ilmastointijärjestelmien huomiot ehdotus 1.\",,1,5,10,20)","(\"Ilmanvaihto- ja ilmastointijärjestelmien huomiot ehdotus 2.\",\"iv muutos 2\",10,50,100,200)"}', 'Tilojen ja käyttöveden lämmitysjärjestelmien huomiot.', 'Lämmityshuomio ruotsiksi!', '{"(\"Tilojen ja käyttöveden lämmitysjärjestelmien huomiot ehdotus.\",\"muutosehdotus ruotsiksi 1\",-200,204.52,-123.45,456.679)"}', 'Valaistuksen, jäähdytysjärjestelmien, sähköisten erillislämmitysten ja muiden järjestelmien huomiot.', NULL, '{"(\"Valaistuksen, jäähdytysjärjestelmien, sähköisten erillislämmitysten ja muiden järjestelmien huomiot.\",,100,200,300,400)"}', 'Ulkoseinien, ulko-ovien ja ikkunoiden huomiot.', NULL, '{"(\"Ulkoseinien, ulko-ovien ja ikkunoiden huomiot ehdotus.\",,9,8,7,6)"}', 'Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja.', NULL, 'Suositellut toimenpide-ehdotukset', NULL, 'Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä.', NULL, false, false, NULL, false, NULL, false, 'Rakennuksen nimi', 'Rakennuksen nimi SV'),
	(4, 2018, 0, NULL, NULL, 2, NULL, 'Laskuriviviite', NULL, NULL, NULL, false, '2020-04-15', 'Katuosoite', 'katuosoite-sv', 'AK3', 'Seuraavia toimenpiteitä voisi tehdä:

 * Toimenpide 1
                                                                                                                       * Toimenpide 2', NULL, 0, 'Kiinteistötunnus', 0, false, NULL, true, 33100, 'Rakennusosa', '101089527F', 'Tilaaja Oy', 2017, 'yrityksen katuosoite', 'Yritys Oy', '33100', 'tampere', 1.6, 1.4, 0.54, 1.5, 1.5, 0.56, 1.9, 0, 0.8, 1, 1, 1, 1.2, 1.2, 0.65, 0.8, 1.2, 0.85, 0.9, 0.5, 0.53, 2.2, 1.3, 0.34, 1, 0, 0.75, 0.4, 0, 1, 2, 1.5, 1, 1, 1, 2, 1, 'ilmanvaihdon kuvaus fi', NULL, 0.66, 21, 0, 0, 3, 2, 1, 3.1, 15000, 1, 2, 0, NULL, NULL, 9, 'Toissijaisen lämmitysjärjestelmän kuvaus', NULL, 1, NULL, NULL, 1.4, 0.9, 3, 1, 10, 0.1, 1, 2, 1.23, 0.5, 2.24, 1, 10, 0.1, 567, 34.2, 2, 1.235, 12.345, 0.45, 25, 2, 2, 1.5, 1.9, 1500, 0.5, 200, 0.34, 0.5, 0.6, 3, 0.6, 4, 0.1, 9, 101, 'C', 0, 0, 3000000, 5000, 7000, NULL, 100, 200, 300, 500, 400, 'Laskentatyökalu v1.2', 2000, 4000, 3000, 1000, 7, 3, 2, 1, 6, 5, 4, 2, 1, 4, 3, 200, 100, 400, 600, 500, 300, NULL, '{"(1,2,4,6,3,5,7,8)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(9,10,12,14,11,13,,15)","(,,16,,,17.12345,18,19)"}', 20000, 40000, 50000, 10000, 40000, 30000, 20000, NULL, 1000, 2000, 3000, 4000, '{"(\"Vapaa ostettu polttoaine\",Kiloa,0.8,1000)","(,,,)","(,,,)"}', 30000, 50000, 'Ylä- ja alapohjan huomiot.', NULL, '{"(\"Ylä- ja alapohjan huomiot ehdotus.\",,1,2,3,4)"}', 'Ilmanvaihto- ja ilmastointijärjestelmien huomiot.', NULL, '{"(\"Ilmanvaihto- ja ilmastointijärjestelmien huomiot ehdotus 1.\",,1,5,10,20)","(\"Ilmanvaihto- ja ilmastointijärjestelmien huomiot ehdotus 2.\",\"iv muutos 2\",10,50,100,200)"}', 'Tilojen ja käyttöveden lämmitysjärjestelmien huomiot.', 'Lämmityshuomio ruotsiksi!', '{"(\"Tilojen ja käyttöveden lämmitysjärjestelmien huomiot ehdotus.\",\"muutosehdotus ruotsiksi 1\",-200,204.52,-123.45,456.679)"}', 'Valaistuksen, jäähdytysjärjestelmien, sähköisten erillislämmitysten ja muiden järjestelmien huomiot.', NULL, '{"(\"Valaistuksen, jäähdytysjärjestelmien, sähköisten erillislämmitysten ja muiden järjestelmien huomiot.\",,100,200,300,400)"}', 'Ulkoseinien, ulko-ovien ja ikkunoiden huomiot.', NULL, '{"(\"Ulkoseinien, ulko-ovien ja ikkunoiden huomiot ehdotus.\",,9,8,7,6)"}', 'Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja.', NULL, 'Suositellut toimenpide-ehdotukset', NULL, 'Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä.', NULL, false, false, NULL, false, NULL, false, 'Rakennuksen nimi', 'Rakennuksen nimi SV'),
	(5, 2018, 0, NULL, NULL, 2, NULL, 'Laskuriviviite', NULL, NULL, NULL, false, '2020-04-15', 'Katuosoite', 'katuosoite-sv', 'YAT', 'Seuraavia toimenpiteitä voisi tehdä:

 * Toimenpide 1
                                                                                                                       * Toimenpide 2', NULL, 0, 'Kiinteistötunnus', 0, false, NULL, true, 33100, 'Rakennusosa', '101089527F', 'Tilaaja Oy', 2020, 'yrityksen katuosoite', 'Yritys Oy', '33100', 'tampere', 1.6, 1.4, 0.54, 1.5, 1.5, 0.56, 1.9, 0, 0.8, 1, 1, 1, 1.2, 1.2, 0.65, 0.8, 1.2, 0.85, 0.9, 0.5, 0.53, 2.2, 1.3, 0.34, 1, 0, 0.75, 0.4, 0, 1, 2, 1.5, 1, 1, 1, 2, 1, 'ilmanvaihdon kuvaus fi', NULL, 0.66, 21, 0, 0, 3, 2, 1, 3.1, 140, 1, 2, 0, NULL, NULL, 9, 'Toissijaisen lämmitysjärjestelmän kuvaus', NULL, 1, NULL, NULL, 1.4, 0.9, 3, 1, 10, 0.1, 1, 2, 1.23, 0.5, 2.24, 1, 10, 0.1, 567, 34.2, 2, 1.235, 12.345, 0.45, 25, 2, 2, 1.5, 1.9, 1500, 0.5, 200, 0.34, 0.5, 0.6, 2, 0.6, 3, 0.1, 6, 190, 'D', 0, 0, 0, 20000, 5000, NULL, 100, 200, 300, 500, 400, 'Laskentatyökalu v1.2', 2000, 4000, 3000, 1000, 7, 3, 2, 1, 6, 5, 4, 2, 1, 4, 3, 200, 100, 400, 600, 500, 300, NULL, '{"(1,2,4,6,3,5,7,8)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(,,,,,,,)","(9,10,12,14,11,13,,15)","(,,16,,,17.12345,18,19)"}', 20000, 40000, 50000, 10000, 40000, 30000, 20000, NULL, 1000, 2000, 3000, 4000, '{"(\"Vapaa ostettu polttoaine\",Kiloa,0.8,1000)","(,,,)","(,,,)"}', 30000, 50000, 'Ylä- ja alapohjan huomiot.', NULL, '{"(\"Ylä- ja alapohjan huomiot ehdotus.\",,1,2,3,4)"}', 'Ilmanvaihto- ja ilmastointijärjestelmien huomiot.', NULL, '{"(\"Ilmanvaihto- ja ilmastointijärjestelmien huomiot ehdotus 1.\",,1,5,10,20)","(\"Ilmanvaihto- ja ilmastointijärjestelmien huomiot ehdotus 2.\",\"iv muutos 2\",10,50,100,200)"}', 'Tilojen ja käyttöveden lämmitysjärjestelmien huomiot.', 'Lämmityshuomio ruotsiksi!', '{"(\"Tilojen ja käyttöveden lämmitysjärjestelmien huomiot ehdotus.\",\"muutosehdotus ruotsiksi 1\",-200,204.52,-123.45,456.679)"}', 'Valaistuksen, jäähdytysjärjestelmien, sähköisten erillislämmitysten ja muiden järjestelmien huomiot.', NULL, '{"(\"Valaistuksen, jäähdytysjärjestelmien, sähköisten erillislämmitysten ja muiden järjestelmien huomiot.\",,100,200,300,400)"}', 'Ulkoseinien, ulko-ovien ja ikkunoiden huomiot.', NULL, '{"(\"Ulkoseinien, ulko-ovien ja ikkunoiden huomiot ehdotus.\",,9,8,7,6)"}', 'Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja Lisatietoja.', NULL, 'Suositellut toimenpide-ehdotukset', NULL, 'Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä Lisämerkintöjä.', NULL, false, false, NULL, false, NULL, false, 'Rakennuksen nimi', 'Rakennuksen nimi SV') ON CONFLICT DO NOTHING;


--
-- Data for Name: kayttaja_aineisto; Type: TABLE DATA; Schema: etp; Owner: etp
--



--
-- Data for Name: laatija_yritys; Type: TABLE DATA; Schema: etp; Owner: etp
--



--
-- Data for Name: vo_toimenpide; Type: TABLE DATA; Schema: etp; Owner: etp
--



--
-- Data for Name: liite; Type: TABLE DATA; Schema: etp; Owner: etp
--



--
-- Data for Name: sivu; Type: TABLE DATA; Schema: etp; Owner: etp
--



--
-- Data for Name: viestiketju; Type: TABLE DATA; Schema: etp; Owner: etp
--



--
-- Data for Name: vastaanottaja; Type: TABLE DATA; Schema: etp; Owner: etp
--



--
-- Data for Name: viesti; Type: TABLE DATA; Schema: etp; Owner: etp
--



--
-- Data for Name: viesti_liite; Type: TABLE DATA; Schema: etp; Owner: etp
--



--
-- Data for Name: viesti_reader; Type: TABLE DATA; Schema: etp; Owner: etp
--



--
-- Data for Name: vk_valvonta; Type: TABLE DATA; Schema: etp; Owner: etp
--



--
-- Data for Name: vk_henkilo; Type: TABLE DATA; Schema: etp; Owner: etp
--



--
-- Data for Name: vk_note; Type: TABLE DATA; Schema: etp; Owner: etp
--



--
-- Data for Name: vk_toimenpide; Type: TABLE DATA; Schema: etp; Owner: etp
--



--
-- Data for Name: vk_toimenpide_henkilo; Type: TABLE DATA; Schema: etp; Owner: etp
--



--
-- Data for Name: vk_yritys; Type: TABLE DATA; Schema: etp; Owner: etp
--



--
-- Data for Name: vk_toimenpide_yritys; Type: TABLE DATA; Schema: etp; Owner: etp
--



--
-- Data for Name: vk_valvonta_liite; Type: TABLE DATA; Schema: etp; Owner: etp
--



--
-- Data for Name: vo_note; Type: TABLE DATA; Schema: etp; Owner: etp
--



--
-- Data for Name: vo_tiedoksi; Type: TABLE DATA; Schema: etp; Owner: etp
--



--
-- Data for Name: vo_virhetype; Type: TABLE DATA; Schema: etp; Owner: etp
--

INSERT INTO etp.vo_virhetype VALUES
	(0, 'Ei laskennalliset / Todistus tehty laajennuksesta', 'Ei laskennalliset / Todistus tehty laajennuksesta SV', true, 1, 'Energiatodistus on laadittu virheellisesti rakennuksen laajennuksesta, eikä koko rakennuksesta. Todistus tehdään rakennuksen osasta vain silloin, kuin rakennuksen eri osien käyttötarkoitukset poikkeavat olennaisesti toisistaan. ARA suosittelee, että rakennuksesta laaditaan uusi koko rakennusta koskeva todistus käyttöönoton jälkeen. Tällöin todistus laaditaan olemassa olevalle rakennukselle sisältäen havainnointikäynnin paikan päällä. (Laki rakennuksen energiatodistuksesta 50/2013, 4 §)', 'TODO SV'),
	(1, 'Ei laskennalliset / Kerrosala alle 50 m²', 'Ei laskennalliset / Kerrosala alle 50 m² SV', true, 2, 'Energiatodistus on laadittu nettoalaltaan  alle 50 m²:n suuruiselle rakennukselle. Onko rakennuksen kerrosala alle 50 m²? Kerrosalaltaan alle 50 m²:n suuruisille rakennuksille ei tehdä energiatodistusta. (Laki rakennuksen energiatodistuksesta 50/2013, 3 §)', 'TODO SV'),
	(2, 'Ei laskennalliset / Paritalon puolikkaasta tehty todistus', 'Ei laskennalliset / Paritalon puolikkaasta tehty todistus SV', true, 3, 'Energiatodistus on laadittu ainoastaan paritalon toisesta asunnosta. ARAn linjauksen mukaan todistus tulee kuitenkin laatia koko rakennukselle silloin kun rakennuksella on yksi pysyvä rakennustunnus.', 'TODO SV'),
	(3, 'Ei laskennalliset / Autotallin pinta-ala laskettu mukaan', 'Ei laskennalliset / Autotallin pinta-ala laskettu mukaan SV', true, 4, 'Netto-alaan on virheellisesti huomioitu mukaan autotallin pinta-ala.  Energiatodistukseen ei huomioida rakennuksen sisällä sijaitsevia tai rakennukseen rakenteellisesti liittyviä moottoriajoneuvosuojia. (Ympäristöministeriön asetus rakennuksen energiatodistuksesta 1048/2017, Liite 1)', 'TODO SV'),
	(4, 'Ei laskennalliset / Rakennustunnus puuttuu/on virheellinen', 'Ei laskennalliset / Rakennustunnus puuttuu/on virheellinen SV', true, 5, 'Pysyvä rakennustunnus [puuttuu/on virheellinen]. Rakennustunnuksena esitetään väestötietojärjestelmän mukainen pysyvä rakennustunnus. Pysyvät rakennustunnukset löytyvät esimerkiksi SYKEen ylläpitämästä Liiteri-karttapalvelusta. (Ympäristöministeriön asetuksen rakennuksen energiatodistuksesta 1048/2017, Liite 4)', 'TODO SV'),
	(5, 'Ei laskennalliset / Todistus tehty väärän lain mukaan I', 'Ei laskennalliset / Todistus tehty väärän lain mukaan I SV', true, 6, 'Käyttöönottovaiheen energiatodistus tulee päivittää saman lain mukaiseksi kuin lupavaiheen energiatodistus on ollut.  (Energiatodistuslaki 50/2013 30 §)', 'TODO SV'),
	(6, 'Ei laskennalliset / Todistuksessa viitattu liitteisiin', 'Ei laskennalliset / Todistuksessa viitattu liitteisiin SV', true, 7, 'Energiatodistusasetuksen mukaan liitteet eivät ole osa energiatodistusta, eikä todistuksessa tule viitata liitteisiin. Energiatodistus laaditaan asetuksen liitteen 3 mukaiselle lomakkeelle.  Toimenpide-ehdotukset merkitään energiatodistukseen seuraavasti: 
* Toimenpide-ehdotuksina esitetään lyhyesti energiansäästötoimenpiteet kullekin lomakkeessa yksilöidylle järjestelmälle erikseen.
* Toimenpiteelle arvioidaan sen aikaansaama muutos rakennuksen lämmitysenergian, sähköenergian ja tilojen jäähdytysenergian laskennallisessa ostoenergiankulutuksessa. Pienentynyt ostoenergian määrä esitetään negatiivisena lukuna, lisääntynyt ostoenergian määrä positiivisena lukuna.
* Toimenpiteille esitetään myös toimenpiteellä aikaansaatava muutos E-luvussa. E-luvun muutos pienemmäksi ilmoitetaan samaten negatiivisena lukuna.
(Ympäristöminiteriön asetus rakennuksen energiatodistuksesta 1048/2017, 6§)', 'TODO SV'),
	(7, 'Ei laskennalliset / Rakennuksen valmistumisvuosi väärin', 'Ei laskennalliset / Rakennuksen valmistumisvuosi väärin SV', true, 8, 'Rakennuksen valmistumisvuosi on ilmoitettu virheellisesti. Rakennuksen valmistumisvuotena käytetään vuotta, jolloin rakennus on ensimmäistä kertaa loppukatselmuksessa hyväksytty käyttöön. Kun energiatodistus laaditaan uudelle rakennukselle rakennusluvan hakemisen yhteydessä, valmistumisvuotena käytetään todistuksessa luvan hakemisvuotta. (Ympäristöministeriön asetus rakennuksen energiatodistuksesta 1048/2017, liite 4)', 'TODO SV'),
	(8, 'Ei laskennalliset / Ylilämpenemislaskelma puuttuu (uusi rakennus)', 'Ei laskennalliset / Ylilämpenemislaskelma puuttuu (uusi rakennus) SV', true, 9, 'Käytetyn laskentaohjelman oikeellisuutta ei voitu tarkastaa, sillä lähetetystä materiaalista puuttui laskennallinen kesäajan huonelämpötilatarkastelu.  Uutta rakennusta suunniteltaessa tulee tehdä energiaselvitys, joka sisältää laskennallisen kesäaikaisen huonelämpötilatarkastelun. (Ympäristöministeriön asetus uuden rakennuksen energiatehokkuudesta 1010/2017, 34 §, 29 §)', 'TODO SV'),
	(9, 'Ei laskennalliset / Laatimisvaihe väärin', 'Ei laskennalliset / Laatimisvaihe väärin SV', true, 10, 'Energiatodistuksessa ilmoitetaan, onko energiatodistus laadittu uudelle rakennukselle rakennuslupaa haettaessa, uudelle rakennukselle käyttöönottovaiheessa  vai  olemassa  olevalle  rakennukselle.  Rakennus katsotaan otetuksi käyttöön, kun se on maankäyttö- ja rakennuslain 153 §:n 1 momentin mukaisessa loppukatselmuksessa hyväksytty käyttöönotettavaksi. Käyttöönoton jälkeen todistus on tehtävä olemassa olevan rakennuksen lainsäädännön mukaisesti. (Energitodistuslaki 50/2013, 5 § ja Ympäristöministeriön asetus rakennuksen energiatodistuksesta 1048/2017, Liite 4)', 'TODO SV'),
	(10, 'Rakennusvaippa / q<sub>50</sub>-luku väärin (uudet rakennukset)', 'Rakennusvaippa / q<sub>50</sub>-luku väärin (uudet rakennukset) SV', true, 11, 'Rakennusvaipan ilmanvutoluku q<sub>50</sub> on alhainen. Uusien rakennuksien ilmanpitävyyden lähtötietona käytetään rakennusvaipan ilmanvuotoluvun suunnitteluarvoa, jos ilmanpitävyys osoitetaan teollisen talonrakennuksen laadunvarmistusmenettelyllä tai tullaan osoittamaan mittaamalla. Mikäli rakennusvaipan ilmanvuotolukua ei voida edellä mainituilla tavoilla selvittää, se määritetään taulukon 4 arvojen perusteella. (Ympäristöministeriön asetus rakennuksen energiatodistuksesta 1048/2017, liite1)', 'TODO SV'),
	(11, 'Rakennusvaippa / q<sub>50</sub>-luku väärin (olemassa olevat)', 'Rakennusvaippa / q<sub>50</sub>-luku väärin (olemassa olevat) SV', true, 12, 'Rakennusvaipan ilmanvuotoluku q<sub>50</sub> on alhainen. Olemassa olevan rakennuksen rakennusvaipan ilmanvuotoluku selvitetään mittaamalla, suunnitelmista tai ajantasaisista rakennuksen asiakirjoista. Mikäli rakennusvaipan ilmanvuotolukua ei voida edellä mainituilla tavoilla selvittää, se määritetään asetuksen taulukon 4 arvojen perusteella. On hyvä muistaa, että n50 ja q<sub>50</sub> ovat kaksi eri arvoa. (Ympäristöministeriön asetuksen rakennuksen energiatodistuksesta 1048/2017, liite1)', 'TODO SV'),
	(12, 'Rakennusvaippa / Nettoala laskettu virheellisesti', 'Rakennusvaippa / Nettoala laskettu virheellisesti SV', true, 13, 'Rakennuksen lämmitetty nettoala poikkeaa piirustusten mukaisesta ja sen osalta laskenta tulee tarkistaa. Lämmitetty nettoala (Anetto) on lämmitettyjen kerrostasoalojen summa kerrostasoja ympäröivien ulkoseinien sisäpintojen mukaan laskettuna. (Ympäristöministeriön asetus rakennuksen energiatodistuksesta 1048/2017, Liite 1)', 'TODO SV'),
	(13, 'Rakennusvaippa / Alapohjan pinta-ala laskettu virheellisesti', 'Rakennusvaippa / Alapohjan pinta-ala laskettu virheellisesti SV', true, 14, 'Alapohjan pinta-ala poikkeaa piirustusten mukaisesta ja sen osalta laskenta tulee tarkistaa.  Alapohjan pinta-ala lasketaan sisämittojen mukaan aukkojen ja rakenteiden aloja vähentämättä. (Ympäristöministeriön asetus rakennuksen energiatodistuksesta 1048/2017, Liite 1)', 'TODO SV'),
	(14, 'Rakennusvaippa / Yläpohjan pinta-ala laskettu virheellisesti', 'Rakennusvaippa / Yläpohjan pinta-ala laskettu virheellisesti SV', true, 15, 'Yläpohjan pinta-ala poikkeaa piirustusten mukaisesta ja sen osalta laskenta tulee tarkistaa. Yläpohjan pinta-ala lasketaan ulkoseinien sisämittojen mukaisesti kattoikkunoiden aukkojen pinta-alat vähentäen.  (Ympäristöministeriön asetus rakennuksen energiatodistuksesta 1048/2017, Liite 1)', 'TODO SV'),
	(15, 'Rakennusvaippa / Ikkunoiden pinta-ala laskettu virheellisesti', 'Rakennusvaippa / Ikkunoiden pinta-ala laskettu virheellisesti SV', true, 16, 'Ikkunoiden pinta-alat poikkeavat piirustusten mukaisesta ja niiden osalta laskenta tulee tarkistaa. Ympäristöministeriön asetuksen rakennuksen energiatodistuksesta (1048/2017, Liite 1) mukaan ikkunoiden ja ovien pinta-alat lasketaan kehän eli karmirakenteen ulkomittojen mukaan.', 'TODO SV'),
	(16, 'Rakennusvaippa / Ulkoseinien pinta-ala laskettu virheellisesti', 'Rakennusvaippa / Ulkoseinien pinta-ala laskettu virheellisesti SV', true, 17, 'Ulkoseinien pinta-alat poikkeavat piirustusten mukaisesta ja niiden osalta laskenta tulee tarkistaa. Ulkoseinien pinta-ala lasketaan sisämittojen mukaisesti alapohjan lattiapinnasta yläpohjan alapintaan ikkunoiden ja ovien aukkojen pinta-alat vähentäen. (Ympäristöministeriön asetus rakennuksen energiatodistuksesta 1048/2017, Liite 1)', 'TODO SV'),
	(17, 'Rakennusvaippa / U-arvoissa virhe', 'Rakennusvaippa / U-arvoissa virhe SV', true, 18, 'Laskennassa käytetyt rakenteiden lämmönläpäisykertoimet (U-arvot) eivät vastaa [suunnitelmien mukaisia/ rakentamisajankohdan määräysten mukaisia] arvoja.  (Ympäristöministeriön asetus rakennuksen energiatodistuksesta 1048/2017, liite 1)', 'TODO SV'),
	(18, 'Rakennusvaippa / Ikkunoiden g<sub>kohtisuora</sub> I', 'Rakennusvaippa / Ikkunoiden g<sub>kohtisuora</sub> I SV', true, 19, 'Energiatodistuksen mukaan rakennuksen Ikkunoiden g<sub>kohtisuora</sub> on 0,85. Suomen rakentamismääräyskokoelman energiatehokkuusopas rakennuksen energiakulutuksen ja lämmitystehontarpeen laskentaan taulukon 5.1 mukaan g<sub>kohtisuora</sub>=0,85 on tyypillinen arvo yksinkertaiselle lasitukselle. Pyydämme tarkistamaan ikkunoiden g<sub>kohtisuora</sub> -arvot.', 'TODO SV'),
	(19, 'Rakennusvaippa / Ikkunoiden g<sub>kohtisuora</sub> II', 'Rakennusvaippa / Ikkunoiden g<sub>kohtisuora</sub> II SV', true, 20, 'Ikkunoiden g<sub>kohtisuora</sub> arvo on korkea. Uusien ikkunoiden g<sub>kohtisuora</sub> arvot ovat yleensä alhaisempia. Mikäli ikkunoiden tuotetiedoissa määritettyjä arvoja ei ole käytettävissä, käytetään g<sub>kohtisuora</sub> arvoa 0,6. (Ympäristöministeriön asetus rakennuksen energiatodistuksesta (1048/2017, Liite 1)', 'TODO SV'),
	(20, 'Rakennusvaippa / Kylmäsillat virheelliset (uudet rakennukset)', 'Rakennusvaippa / Kylmäsillat virheelliset (uudet rakennukset) SV', true, 21, 'Rakenteiden välisten liitosten kylmäsiltojen lämpöhäviö on laskettava. Rakenteiden välisten liitosten kylmäsiltojen ominaislämpöhäviöt ja pituudet määritetään rakennuksen asiakirjoista. Ellei tarkempaa tietoa ole käytettävissä, kylmäsiltojen laskenta voidaan tehdä energiatehokkuuden laskentaohjeen luvussa 3 esitetyn mukaisesti. (Ympäristöministeriön asetus rakennuksen energiatodistuksesta 1048/2017, Liite 1; Energiatehokkuusopas rakennuksen energiakulutuksen ja lämmitystehontarpeen laskentaan, luku 3)', 'TODO SV'),
	(21, 'Rakennusvaippa / Kylmäsillat virheelliset  (olemassa olevat)', 'Rakennusvaippa / Kylmäsillat virheelliset  (olemassa olevat) SV', true, 22, 'Kylmäsiltojen lämpöhäviö puuttuu. Rakenteiden välisten liitosten kylmäsiltojen lämpöhäviö on laskettava. Olemassa oleville rakennuksille kylmäsiltojen vaikutus voidaan arvioida yksinkertaistetusti lisäämällä 10 % ulkovaipan johtumislämpöhäviöön. (Ympäristöministeriön asetuksen rakennuksen energiatodistuksesta 1048/2017, Liite 1)', 'TODO SV'),
	(22, 'Ilmanvaihto / Ilmavirrat virheelliset', 'Ilmanvaihto / Ilmavirrat virheelliset SV', true, 23, 'Ilmanvaihdon energiankulutuksen laskennassa tulee käyttää vakioidun käytön mukaisia ilmamääriä sekä käyttöaikoja. (Ympäristöministeriön asetus rakennuksen energiatehokkuudesta 1010/2017, 10 § ja 11 §)', 'TODO SV'),
	(23, 'Ilmanvaihto / SFP-luku virheellinen', 'Ilmanvaihto / SFP-luku virheellinen SV', true, 24, 'Ilmanvaihtojärjestelmän ominaissähkötehosta (SFP-luku) [ei ole tehty selvitystä/on virheellisesti laskettu]. SFP-luku voidaan laskea laitetietoja apuna käyttäen. Uusissa rakennuksissa käytetään suunnitelmien arvoja. Mikäli ilmanvaihtojärjestelmän ominaissähkötehoa ei voida selvittää, käytetään asetuksen liitteen taulukon 3 arvoja. (Ympäristöministeriön asetus rakennuksen energiatodistuksesta 1048/2017, liite 1)', 'TODO SV'),
	(24, 'Ilmanvaihto / SFP-luku yli vaatimusten (uudet rak.)', 'Ilmanvaihto / SFP-luku yli vaatimusten (uudet rak.) SV', true, 25, 'Ilmanvaihdon ominaissähköteho (SFP-luku) ei vastaa uudisrakentamiselle asetettuja vaatimuksia. (Ympäristöministeriön asetus rakennuksen energiatehokkuudesta 1010/2017, 30 §)', 'TODO SV'),
	(25, 'Ilmanvaihto / Lämmöntalteenoton vuosihyötysuhde virheellinen', 'Ilmanvaihto / Lämmöntalteenoton vuosihyötysuhde virheellinen SV', true, 26, 'Ilmanvaihtojärjestelmän lämmöntalteenoton vuosihyötysuhde on korkea ja [siitä ei ole tehty selvitystä/se on virheellisesti laskettu]. Ilmanvaihdon LTO:n vuosihyötysuhde voidaan laskea laitetietojen avulla Ympäristöministeriön exceliä apuna käyttäen. Uusissa rakennuksissa käytetään suunnitelmien arvoja. Mikäli ilmanvaihtojärjestelmän LTO:n vuosihyötysuhdetta ei voida selvittää, käytetään asetuksen liitteen taulukon 2 arvoja. (Ympäristöministeriön asetus rakennuksen energiatodistuksesta 1048/2017, liite 1)', 'TODO SV'),
	(26, 'Ilmanvaihto / SFP-luku ja LTO vuosihyötysuhde VTT-arvot', 'Ilmanvaihto / SFP-luku ja LTO vuosihyötysuhde VTT-arvot SV', true, 27, 'Ilmanvaihtokoneen kokonaisvuosihyötysuhteesta eikä SFP-luvusta ole esitetty laskelmaa. Kokonaisvuosihyötysuhde ja SFP-luku ovat rakennuskohtaisia, joten ne eivät voi olla VTT:n antamia arvoja.', 'TODO SV'),
	(27, 'Ilmanvaihto / Ilmanvaihdon lämmitysenergiankulutus virheellinen', 'Ilmanvaihto / Ilmanvaihdon lämmitysenergiankulutus virheellinen SV', true, 28, 'Ilmanvaihdon lämmitysenergiankulutus on [alhainen/korkea]. Ilmanvaihdon energiankulutuksen laskennassa tulee käyttää vakioidun käytön mukaisia ilmamääriä sekä käyttöaikoja ja ilmoitettua vuosihyötysuhdetta. (Ympäristöministeriön asetus rakennuksen energiatehokkuudesta 1010/2017, 10 § ja 11 §)', 'TODO SV'),
	(28, 'Ilmanvaihto / Ilmavaihdon sähkönkulutus virheellinen', 'Ilmanvaihto / Ilmavaihdon sähkönkulutus virheellinen SV', true, 29, 'Ilmanvaihdon sähköenergiankulutus on [alhainen/korkea]. Ilmanvaihdon energiankulutuksen laskennassa tulee käyttää vakioidun käytön mukaisia ilmamääriä sekä käyttöaikoja ja ilmoitettua ilmanvaihtojärjestelmän SFP-lukua. (Ympäristöministeriön asetus rakennuksen energiatehokkuudesta 1010/2017, 10 § ja 11 §)', 'TODO SV'),
	(29, 'Ilmanvaihto / Painovoimainen iv huomioimatta', 'Ilmanvaihto / Painovoimainen iv huomioimatta SV', true, 30, 'Ilmanvaihdon lämmitysenergiankulutusta ei ole huomioitu laskennassa. Ilmanvaihdon energiankulutuksen laskennassa käytetään ilmamäärinä vakioidun käytön mukaisia arvoja. Painovoimaisen ilmavaihdon tapauksessa tuloilma on ulkoa otettavaa korvausilmaa. Tiloissa tapahtuva tuloilman ja korvausilman lämpeneminen on osa tilojen lämmitysenergiantarvetta ja huomioidaan laskennassa siinä yhteydessä. (Ympäristöminiteriön asetus rakennuksen energiatehokkuudesta 1010/2017, 10 §)', 'TODO SV'),
	(30, 'Ilmanvaihto / Ilmvirrat, pientalot', 'Ilmanvaihto / Ilmvirrat, pientalot SV', true, 31, 'Ympäristöministeriön asetus uuden rakennuksen energiatehokkuudesta (1010/2017) 10 § mukaan E-luku on laskettava käyttäen seuraavia käyttöajan ulkoilmavirtoja, Käyttötarkoitusluokka 1) 0,4 m³/(s m²). (huom! kohta ilmavirta tulo/poisto)', 'TODO SV'),
	(31, 'Ilmanvaihto / Sisäänpuhalluslämpötila "virheellinen"', 'Ilmanvaihto / Sisäänpuhalluslämpötila "virheellinen" SV', true, 32, 'Annetun vuosihyötysuhteen ja vakioidun käytön mukaan laskettu ilmanvaihdon lämmitysenergiantarve on alhainen. Energiatehokkuus-laskentaoppaan mukaan tuloilman sisäänpuhalluslämpötilana tilaan voidaan käyttää 18 °C, ellei tarkempaa tietoa ole saatavilla. Jos halutaan käyttää tätä alhaisempia sisäänpuhalluslämpötiloja lämmityskaudella, tulee siitä tehdä selvitys, mm. miten tilakohtaisten lämmityslaitteiden mitoituksessa on huomioitu tuloilman lämmitystarve.  (Energiatehokkuusopas rakennuksen energiakulutuksen ja lämmitystehontarpeen laskentaan, luku 3)', 'TODO SV'),
	(32, 'Ilmanvaihto / Lämpötilahyötysuhde = vuosihyötysuhde', 'Ilmanvaihto / Lämpötilahyötysuhde = vuosihyötysuhde SV', true, 33, 'Tarkasta ilmanvaihtokoneen LTO:n lämpötilahyötysuhteen arvo se ei voi olla [sama/pienempi] mitä LTO:n vuosihyötysuhde.', 'TODO SV'),
	(33, 'Ilmanvaihto / LTO:n vuosihyötysuhteen laskennassa virheitä', 'Ilmanvaihto / LTO:n vuosihyötysuhteen laskennassa virheitä SV', true, 34, 'Lämmöntalteenoton vuosihyötysuhde on yläkanttiin laskettu. Lähetetyn materiaalin mukaisessa ilmanvaihtokoneessa on vastavirtasiirrin, jonka jäätymisen esto on lähempänä +0 °C, kuin -10 °C. Jäätymisen eston ulospuhallusilman minimilämpötilana voidaan käyttää Energiatehokkuus-oppaan mukaisia ohjearvoja, jos laitteesta ei ole olemassa varmennettuja suoritusarvoja. LTO-vuosihyötysuhteen laskennassa on oletettu myös, että kaikki ilmanvaihto on käytön ulkopuolella kiinni, tämä on vastoin Ympäristöministeriön asetusta uuden rakennuksen sisäilmastosta ja ilmanvaihdosta (1009/2017).', 'TODO SV'),
	(34, 'Käyttövesi / LKV:n jaon ja luovutuksen hyötysuhde', 'Käyttövesi / LKV:n jaon ja luovutuksen hyötysuhde SV', true, 35, 'Lämpimän käyttöveden jakelun hyötysuhde poikkeaa taulukkoarvosta ja siitä ei ole tehty selvitystä. LKV:n jakelun hyötysuhde voidaan selvittää erillisselvityksellä, jolloin arvoa käytetään laskennassa. Jos jakelun hyötysuhdetta ei ole selvitetty, käytetään hyötysuhteena asetuksen liitteen taulukon 5 arvoja. (Ympäristöministeriön asetus rakennuksen energiatodistuksesta 1048/2017, Liite 1)', 'TODO SV'),
	(35, 'Käyttövesi / Lämmityksen tuoton hyötysuhde, käyttövesi', 'Käyttövesi / Lämmityksen tuoton hyötysuhde, käyttövesi SV', true, 36, 'Lämmöntuottojärjestelmän hyötysuhde poikkeaa taulukkoarvosta käyttöveden lämmityksen osalta ja siitä ei ole tehty selvitystä. Tuoton hyötysuhde voidaan selvittää rakennuksen tarkastuksen yhteydessä tai tuotetiedoista, jolloin arvoa käytetään laskennassa. Jos hyötysuhdetta ei ole selvitetty, käytetään lämmöntuottojärjestelmän hyötysuhteina taulukoissa 10 ja 11 esitettyjä arvoja. (Ympäristöministeriön asetus rakennuksen energiatodistuksesta 1048/2018, Liite 1)', 'TODO SV'),
	(36, 'Käyttövesi / Kiertovesipumpun sähkönkulutus puuttuu', 'Käyttövesi / Kiertovesipumpun sähkönkulutus puuttuu SV', true, 37, 'Lämpimän käyttöveden kiertovesipumpun sähkönkulutus puuttuu. Lämpimän käyttöveden kiertovesipumpun sähköenergian kulutus lasketaan energiatehokkuuden laskentaohjeen kohdan 6.3.4 mukaan tai muulla vastaavalla tavalla.  (Energiatehokkuusopas rakennuksen energiakulutuksen ja lämmitystehontarpeen laskentaan, luku 6)', 'TODO SV'),
	(37, 'Käyttövesi / LKV:n kiertojohto laskettu virheellisesti', 'Käyttövesi / LKV:n kiertojohto laskettu virheellisesti SV', true, 38, 'Lämpimän käyttöveden kiertojohdon lämpöhäviöt [ovat alhaiset/puuttuvat]. LKV:n kiertojohdon häviöt voidaan selvittää erillisselvityksellä, jolloin saatua arvoa tulee käyttää laskennassa. Muussa tapauksessa se määritetään asetuksen liitteen taulukoiden 6 ja 7 avulla. (Ympäristöministeriön asetus rakennuksen energiatodistuksesta 1048/2017, Liite 1)', 'TODO SV'),
	(38, 'Käyttövesi / LKV:n kiertojohto II asuinkerrostalot', 'Käyttövesi / LKV:n kiertojohto II asuinkerrostalot SV', true, 39, 'Lämpimän käyttöveden kiertojohdon lämpöhäviöt [ovat alhaiset/puuttuvat]. Jos häviöt laskee putkipituuden mukaan, tulee putkipituuteen huomioida kiertojohdon lisäksi myös lämpimän käyttöden jakojohto, siltä osin, kun siinä on kiertoa (asuntojen vesimittareille asti). Laskenta tulee tarkistaa tältä osin.', 'TODO SV'),
	(39, 'Käyttövesi / Käyttöveden energiankulutus virheellinen I', 'Käyttövesi / Käyttöveden energiankulutus virheellinen I SV', true, 40, 'Lämpimän käyttöveden energiankulutus ei vastaa vakioidun käytön mukaan laskettua. Lämpimän käyttöveden nettoenergiantarpeena käytetään asetuksen mukaisia vakioidun käytön arvoja. (Ympäristöminiteriön asetus uuden rakennuksen energiatehokkuudesta 1010/2017, 12 §)', 'TODO SV'),
	(40, 'Käyttövesi / Käyttöveden energiankulutus virheellinen II', 'Käyttövesi / Käyttöveden energiankulutus virheellinen II SV', true, 41, 'Käyttöveden lämmitysenergian nettotarve on korkea. Käyttötarkoitusluokassa 1 on lämpimän käyttöveden lämmitysenergian nettotarve enintään 4 200 kWh vuodessa asuntoa kohden. (Ympäristöministeriön asetus uuden rakennuksen energiatehokkuudesta 1010/2017, 12 §)', 'TODO SV'),
	(41, 'Käyttövesi / Lämpöpumpun SPF-luku käyttövesi', 'Käyttövesi / Lämpöpumpun SPF-luku käyttövesi SV', true, 42, 'Lämpöpumpun lämpökerroin on käyttöveden osalta korkea, eikä siitä ole annettu lisäselvitystä. Lämpöpumpun kausisuorituskykykertoimet (SPF-luvut) voidaan selvittää  suunnitelmista tai tuotetiedoista. Ellei tämä ole mahdollista, käytetään asetuksen liitteen 1 taulukoiden 12-14 arvoja. (Ympäristöministeriön asetus rakennuksen energiatodistuksesta 1048/2017, Liite 1)', 'TODO SV'),
	(42, 'Tilojen lämmitys / Tilojen lämmityksen jaon ja luovutuksen hyötysuhde', 'Tilojen lämmitys / Tilojen lämmityksen jaon ja luovutuksen hyötysuhde SV', true, 43, 'Lämmitysjärjestelmien lämmönjaon ja -luovutuksen vuosihyötysuhde poikkeaa taulukkoarvosta ja siitä ei ole tehty selvitystä. Vuosihyötysuhde voidaan selvittää rakennuksen tarkastuksen yhteydessä, jolloin arvoa käytetään laskennassa. Muussa tapauksessa laskennassa käytetään liitteen 1 taulukossa 9 esitettyjä  lämmönjaon ja -luovutuksen vuosihyötysuhteiden arvoja. (Ympäristöministeriön asetus rakennuksen energiatodistuksesta 1048/2017, liite1)', 'TODO SV'),
	(43, 'Tilojen lämmitys / Lämmityksen tuoton hyötysuhde, tilojen lämmitys', 'Tilojen lämmitys / Lämmityksen tuoton hyötysuhde, tilojen lämmitys SV', true, 44, 'Lämmöntuottojärjestelmän hyötysuhde poikkeaa taulukkoarvosta tilojen lämmityksen osalta ja siitä ei ole tehty selvitystä. Tuoton hyötysuhde voidaan selvittää rakennuksen tarkastuksen yhteydessä tai tuotetiedoista, jolloin arvoa käytetään laskennassa. Jos hyötysuhdetta ei ole selvitetty, käytetään lämmöntuottojärjestelmän hyötysuhteina taulukoissa 10 ja 11 esitettyjä arvoja. (Ympäristöministeriön asetus rakennuksen energiatodistuksesta 1048/2018, Liite 1)', 'TODO SV'),
	(44, 'Tilojen lämmitys / Tilojen lämmityksen apulaitteet', 'Tilojen lämmitys / Tilojen lämmityksen apulaitteet SV', true, 45, 'Lämmitysjärjestelmän apulaitteiden sähkökulutus [puuttuu/on alhainen]. Lämmitysjärjestelmän apulaitteiden sähkönkulutus muodostuu lämmönjaon ja -luovutuksen- sekä tuoton apulaitteiden sähkönkulutuksesta. Apulaitteiden sähkönkäyttö voidaan selvittää rakennuksen tarkastuksen yhteydessä, jolloin arvoa käytetään laskennassa. Muuten apulaitteiden sähkönkulutus lasketaan taulukoissa 9, 10 ja 11 esitettyjen lukujen avulla. (Ympäristöministeriön asetus rakennuksen energiatodistuksesta 1048/2017, Liite 1)', 'TODO SV'),
	(45, 'Tilojen lämmitys / Lämpöpumpun SPF-luku tilalämmitys', 'Tilojen lämmitys / Lämpöpumpun SPF-luku tilalämmitys SV', true, 46, 'Lämpöpumpun lämpökerroin on tilojen lämmityksen osalta korkea, eikä siitä ole annettu lisäselvitystä. Lämpöpumpun kausisuorituskykykertoimet (SPF-luvut) voidaan selvittää  suunnitelmista tai tuotetiedoista. Ellei tämä ole mahdollista, käytetään asetuksen liitteen 1 taulukoiden 12-14 arvoja. (Ympäristöministeriön asetus rakennuksen energiatodistuksesta 1048/2017, Liite 1)', 'TODO SV'),
	(46, 'Tilojen lämmitys / Rakennuksen ulkopuolisten lämpökanaalien häviöt', 'Tilojen lämmitys / Rakennuksen ulkopuolisten lämpökanaalien häviöt SV', true, 47, 'Rakennuksen ulkopuolisia lämpökanaaleja ei ole huomioitu laskennassa. Jos rakennus on kytketty lämmitysjärjestelmään (tilojen, ilmanvaihdon ja lämpimän käyttöveden lämmitys), jossa lämpö johdetaan rakennuksen
ulkopuolisilla lämpöputkilla yhteisestä lämmönsiirtimestä tai lämmöntuottolaitteesta useampaan rakennukseen, on kyseisten lämpöputkien lämpöhäviö jaettava rakennusten kesken
pinta-alojen suhteessa. (Ympäristöministeriön asetus rakennuksen energiatehokkuusasetus 1010/2017, 18 §)', 'TODO SV'),
	(47, 'Tilojen lämmitys / Maatilan omaan käyttöön tuottama aluelämpö ei ole kaukolämpöä', 'Tilojen lämmitys / Maatilan omaan käyttöön tuottama aluelämpö ei ole kaukolämpöä SV', true, 48, 'Kaukolämpö on jakeluverkon kautta asiakkaan kiinteistölle toimitettua lämpöä. Maatilalla tuotettua aluelämpöä ei voida rinnastaa energiatodistuslaskennassa kaukolämpöön. (Ympäristöminiteriön asetus uuden rakennuksen energiatehokkuudesta 1010/2017, 2 §)', 'TODO SV'),
	(48, 'Tilojen lämmitys / Ilmalämpöpumppu', 'Tilojen lämmitys / Ilmalämpöpumppu SV', true, 49, 'Ilmalämpöpumpulla tuotettu energiamäärä on liian suuri. Pienille asuinrakennuksille (käyttötarkoitusluokka 1) käytetään ilma-ilmalämpöpumpun tuottamalle energialle enintään asetuksen liitteen 1 taulukossa 15 esitettyjä arvoja.  (Ympäristöministeriön asetus rakennuksen energiatodistuksesta 1048/2017, Liite 1)', 'TODO SV'),
	(49, 'Tilojen lämmitys / COP vs. SPF', 'Tilojen lämmitys / COP vs. SPF SV', true, 50, 'Lämpöpumpun erillisselvityksessä on ilmoitettu lämpökertoimet (COP) lämmitykselle ja lämpimän käyttöveden valmistukselle. Lämpökerroin ei ole sama asia, mitä lämpöpumpun kausisuorituskykykerroin (SPF-luku). COP kertoo hetkellisen hyötysuhteen tietyssä lämpötilassa, kun taas SPF-luku kertoo keskimääräisen hyötysuhteen vuoden aikana huomioiden sääolosuhteet ja lämmitysverkoston lämpötilan. Vuosihyötysuhteen laskennasta on kerrottu enemmän Ympäristöministeriön sivulta löytyvästä Lämpöpumppujen energialaskentaoppaassa. Lämpöpumppujen SPF-luvut voidaan selvittää  suunnitelmista tai tuotetiedoista, ellei tämä ole mahdollista, käytetään asetuksen liitteen 1 taulukoiden 12-14 arvoja. (Ympäristöministeriön asetus rakennuksen energiatodistuksesta (1048/2017, liite 1)', 'TODO SV'),
	(50, 'Vakioitu käyttö ja lämpökuormat / Valaistus I', 'Vakioitu käyttö ja lämpökuormat / Valaistus I SV', true, 51, 'Valaistuksen lämpökuorma poikkeaa asetuksen taulukkoarvosta ja siitä ei ole tehty selvitystä. Valaistuksen lämpökuorman arvona voidaan käyttää valaistussuunnitelman mukaista arvoa, jos valaistussuunnitelmasta voidaan lämpökuorma määrittää tilatyyppikohtaisesti valaistuksen tehotiheyden ja valaistuksen ohjauksen perusteella. (Ympäristöministeriön asetus rakennuksen energiatehokkuudesta 1010/2017, 11 §)', 'TODO SV'),
	(51, 'Vakioitu käyttö ja lämpökuormat / Valaistus II, asuinkerrostalot', 'Vakioitu käyttö ja lämpökuormat / Valaistus II, asuinkerrostalot SV', true, 52, 'Laskennassa käytetty valaistuksen tehotaso poikkeaa asetuksen taulukkoarvosta. Asuinkerrostalossa, jossa asukkaalla on mahdollista tuoda omat valaisimet, on asuinhuoneissa käytettävä vakioidun käytön mukaisia arvoja. Yleisissä tiloissa sekä asuntojen pesuhuoneissa voidaan käyttää suunniteltuja valaistustasoja. (Ympäristöministeriön asetus rakennuksen energiatehokkuudesta 1010/2017, 11 §)', 'TODO SV'),
	(52, 'Vakioitu käyttö ja lämpökuormat / Lämpökuormat laskettu väärin', 'Vakioitu käyttö ja lämpökuormat / Lämpökuormat laskettu väärin SV', true, 53, 'Laskennassa tulee käyttää Ympäristöministeriön asetus rakennuksen energiatehokkuudesta (1010/2017) 11 § mukaisia valaistuksen, kuluttajalaitteiden ja ihmisten aiheuttamia  lämpökuormia.', 'TODO SV'),
	(53, 'Vakioitu käyttö ja lämpökuormat / Poikkeavat lähtötiedot', 'Vakioitu käyttö ja lämpökuormat / Poikkeavat lähtötiedot SV', true, 54, 'Mikäli energiatodistuksen laadinnassa on käytetty laitteiden tuotetietoihin pohjautuvia laskennan lähtöarvoja, jotka poikkeavat energiatodistusasetuksen laskentaliitteen tai energiatehokkuuden laskentaohjeen arvoista, lisämerkinnöissä on esitettävä laitteiden valmistajat ja muut laitetiedot sekä käytetyt laskennan lähtöarvot. (Ympäristöministeriön asetus rakennuksen energiatodistuksesta 1048/2017, Liite 4)', 'TODO SV'),
	(54, 'Vakioitu käyttö ja lämpökuormat / Varastorakennuksen kuormat', 'Vakioitu käyttö ja lämpökuormat / Varastorakennuksen kuormat SV', true, 55, 'Varastorakennusten E-luku lasketaan uusille rakennuksille suunnitteluarvoilla. Kuormien käyttöasteet ja aikataulut tulee tarkastaa, nyt laskennassa on käytetty omakotitalolle tyypillisiä vakioidun käytön arvoja. (Ympäristöministeriön asetus rakennuksen energiatodistuksesta (1048/2017)', 'TODO SV'),
	(55, 'Jäähdytys / Väärä laskentamenetelmä, jäähdytys', 'Jäähdytys / Väärä laskentamenetelmä, jäähdytys SV', true, 56, 'Todistus on tehty virheellisesti uudesta jäähdytetystä rakennuksesta kuukausitason laskenta-menetelmällä.  Kuukausitason laskentamenetelmää voidaan käyttää rakennukselle, jonka sisäilman lämpötilan hallinta ei edellytä jäähdytystä tai jäähdytystä edellytetään vain tiloissa, joiden lämmitetty nettoala on alle 10 prosenttia rakennuksen lämmitetystä nettoalasta tai joiden lämmitetty nettoala on alle 50 neliömetriä. Huonelämpötilatarkastelun puuttuessa, ei voida todeta etteikö rakennuksen sisäilman lämpötilan hallinta edellyttäisi jäähdytystä. Jäähdytys tulee huomioida ja laskennassa on käytettävä dynaamista laskentamenetelmää. (Ympäristöministeriön asetus energiatehokkuudesta 1010/2017, 8 §).', 'TODO SV'),
	(56, 'Jäähdytys / Jäähdytysjärjestelmän kylmäkerroin', 'Jäähdytys / Jäähdytysjärjestelmän kylmäkerroin SV', true, 57, 'Jäähdytysjärjestelmän painotettu kylmäkerroin ei ole sama asia mitä jäähdytyskoneen COP. COP kertoo hetkellisen hyötysuhteen tietyssä lämpötilassa, kun taas painotettu kylmäkerroin kertoo jäähdytyksen keskimääräisen hyötysuhteen jäähdytyskauden aikana. Jos laitevalmistajan varmennettuja suoritusarvoja ei ole käytettävissä, voi laskennassa käyttää energiatehokkuus-laskentaoppaan taulukossa 9.2 annettuja arvoja. (Energiatehokkuusopas rakennuksen energiakulutuksen ja lämmitystehontarpeen laskentaan, luku 9)', 'TODO SV'),
	(57, 'Toimenpide-ehdotukset / Paikan päällä ei ole käyty', 'Toimenpide-ehdotukset / Paikan päällä ei ole käyty SV', true, 58, 'ARAn näkemyksen mukaan laatija ei ole itse havainnoinut paikan päällä. Rakennuksen tyypilliseen käyttöön tarvittavan energiamäärän selvittämiseksi sekä suositusten laatimiseksi on energiatodistuksen laatijan selvitettävä olemassa olevan rakennuksen energiankulutukseen vaikuttavat ominaisuudet asiakirjoista, havainnoimalla paikan päällä todistuksen kohdetta sekä tarvittaessa haastattelemalla rakennuksen käyttäjiä tai ylläpitohenkilökuntaa.  (Laki rakennuksen energiatodistuksesta 50/2013, 11§)', 'TODO SV'),
	(58, 'Toimenpide-ehdotukset / Toimenpide-ehdotukset puuttuu', 'Toimenpide-ehdotukset / Toimenpide-ehdotukset puuttuu SV', true, 59, 'Energiatodistuksessa ei ole annettu toimenpide-ehdotuksia. Todistuksessa annetaan suosituksia toimista, joilla voidaan parantaa kustannustehokkaasti rakennuksen energiatehokkuutta, ellei kyseessä ole uudisrakennus tai rakennus, jolle ei tällaisia toimia voida osoittaa. (Laki rakennuksen energiatodistuksesta 50/2013, 9 §)', 'TODO SV'),
	(59, 'Muut / Aurinkopaneelit ja -keräimet', 'Muut / Aurinkopaneelit ja -keräimet SV', true, 60, 'Aurinkopaneeleista ja -keräimistä otetaan laskennassa huomioon vain se osa, joka voidaan rakennuksessa käyttää hyödyksi eli se osuus, joka pienentää ostoenergiantarvetta. Ulkopuolisiin energiaverkkoihin syötettyä energiaa ei oteta laskennassa huomioon, joten se ei vaikuta E-luvun arvoon. Hyödynnettävästä energiasta pitää esittää erillinen laskelma. (Ympäristöministeriön asetus rakennuksen energiatodistuksesta 1048/2017, liite 1)', 'TODO SV'),
	(60, 'Muut / E-lukulaskenta tehty virheellisesti', 'Muut / E-lukulaskenta tehty virheellisesti SV', true, 61, 'E-luvun laskenta on suurilta osin virheellinen, joten laskennan virheitä ei lähdetä tässä tarkastusmuistiossa erittelemään. Ympäristöministeriö on julkaissut uudet energiatodistusten laskentaesimerkit Motivan sivuilla. Teidän tulee tarkastaa, että käyttämänne laskentaohjelma antaa esimerkkejä vastaavat tulokset laskennassa käytetyillä lähtöarvoilla.', 'TODO SV'),
	(61, 'Muut / Lähtötiedot ovat virheelliset', 'Muut / Lähtötiedot ovat virheelliset SV', true, 62, 'E-luvun laskennan lähtötiedot ovat suurilta osin virheellisiä, joten lähtötietojen virheitä ei lähdetä tässä tarkastusmuistiossa erittelemään.', 'TODO SV'),
	(62, 'Muut / Lähtötietoja ei ole toimitettu', 'Muut / Lähtötietoja ei ole toimitettu SV', true, 63, 'Laatija ei ole toimittanut pyynnöstä huolimatta taustamateriaa. Valvontamuistio on tehty pelkän energiatodistuksen pohjalta', 'TODO SV'),
	(63, 'Muut / Jokeri (luokittelematon virhe)', 'Muut / Jokeri (luokittelematon virhe) SV', true, 64, '', '') ON CONFLICT DO NOTHING;


--
-- Data for Name: vo_virhe; Type: TABLE DATA; Schema: etp; Owner: etp
--



--
-- Name: energiatodistus_id_seq; Type: SEQUENCE SET; Schema: etp; Owner: etp
--

SELECT pg_catalog.setval('etp.energiatodistus_id_seq', 5, true);


--
-- Name: kayttaja_id_seq; Type: SEQUENCE SET; Schema: etp; Owner: etp
--

SELECT pg_catalog.setval('etp.kayttaja_id_seq', 20, true);


--
-- Name: laatija_laskutus_asiakastunnus_seq; Type: SEQUENCE SET; Schema: etp; Owner: etp
--

SELECT pg_catalog.setval('etp.laatija_laskutus_asiakastunnus_seq', 16, true);


--
-- Name: liite_id_seq; Type: SEQUENCE SET; Schema: etp; Owner: etp
--

SELECT pg_catalog.setval('etp.liite_id_seq', 1, false);


--
-- Name: sivu_id_seq; Type: SEQUENCE SET; Schema: etp; Owner: etp
--

SELECT pg_catalog.setval('etp.sivu_id_seq', 1, false);


--
-- Name: viesti_id_seq; Type: SEQUENCE SET; Schema: etp; Owner: etp
--

SELECT pg_catalog.setval('etp.viesti_id_seq', 5000, true);


--
-- Name: viesti_liite_id_seq; Type: SEQUENCE SET; Schema: etp; Owner: etp
--

SELECT pg_catalog.setval('etp.viesti_liite_id_seq', 1, false);


--
-- Name: viestiketju_id_seq; Type: SEQUENCE SET; Schema: etp; Owner: etp
--

SELECT pg_catalog.setval('etp.viestiketju_id_seq', 5000, true);


--
-- Name: vk_henkilo_id_seq; Type: SEQUENCE SET; Schema: etp; Owner: etp
--

SELECT pg_catalog.setval('etp.vk_henkilo_id_seq', 1, false);


--
-- Name: vk_note_id_seq; Type: SEQUENCE SET; Schema: etp; Owner: etp
--

SELECT pg_catalog.setval('etp.vk_note_id_seq', 1, false);


--
-- Name: vk_toimenpide_id_seq; Type: SEQUENCE SET; Schema: etp; Owner: etp
--

SELECT pg_catalog.setval('etp.vk_toimenpide_id_seq', 1, false);


--
-- Name: vk_valvonta_id_seq; Type: SEQUENCE SET; Schema: etp; Owner: etp
--

SELECT pg_catalog.setval('etp.vk_valvonta_id_seq', 1, false);


--
-- Name: vk_valvonta_liite_id_seq; Type: SEQUENCE SET; Schema: etp; Owner: etp
--

SELECT pg_catalog.setval('etp.vk_valvonta_liite_id_seq', 1, false);


--
-- Name: vk_yritys_id_seq; Type: SEQUENCE SET; Schema: etp; Owner: etp
--

SELECT pg_catalog.setval('etp.vk_yritys_id_seq', 1, false);


--
-- Name: vo_note_id_seq; Type: SEQUENCE SET; Schema: etp; Owner: etp
--

SELECT pg_catalog.setval('etp.vo_note_id_seq', 1, false);


--
-- Name: vo_tiedoksi_id_seq; Type: SEQUENCE SET; Schema: etp; Owner: etp
--

SELECT pg_catalog.setval('etp.vo_tiedoksi_id_seq', 1, false);


--
-- Name: vo_toimenpide_id_seq; Type: SEQUENCE SET; Schema: etp; Owner: etp
--

SELECT pg_catalog.setval('etp.vo_toimenpide_id_seq', 1, false);


--
-- Name: yritys_id_seq; Type: SEQUENCE SET; Schema: etp; Owner: etp
--

SELECT pg_catalog.setval('etp.yritys_id_seq', 1, false);


--
-- Name: yritys_laskutus_asiakastunnus_seq; Type: SEQUENCE SET; Schema: etp; Owner: etp
--

SELECT pg_catalog.setval('etp.yritys_laskutus_asiakastunnus_seq', 1, false);


--
-- PostgreSQL database dump complete
--

