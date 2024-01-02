INSERT INTO lammonjako (id, label_fi, label_sv, ordinal)
VALUES
(0, 'Vesikiertoinen patterilämmitys', 'Vattenburen elementvärme', 1),
(1, 'Vesikiertoinen lattialämmitys', 'Vattenburen golvvärme', 2),
(2, 'Vesikiertoinen kattosäteilylämmitys', 'Vattenburen takvärme', 3),
(3, 'Sähköpatterilämmitys', 'Uppvärmning med elelement', 4),
(4, 'Sähköinen lattialämmitys', 'Elburen golvvärme', 5),
(5, 'Sähköinen kattolämmitys', 'Elburen takvärme', 6),
(6, 'Ilmalämmitys', 'Luftuppvärmning', 7),
(7, 'Uuni- tai kamiinalämmitys', 'Uppvärmning med ugn eller kamin', 8),
(8, 'Vesikiertoinen lattia-/patterilämmitys', 'Vattenburen golv-/elementvärme', 9),
(9, 'Vesikiertoinen lämmitys/ märkätiloissa sähköinen lattialämmitys', 'Vattenburen värme/elburen golvvärme i våtutrymmen', 10),
(10, 'Ilmalämmitys/vesikiertoinen patterilämmitys', 'Luftuppvärmning/vattenburen elementvärme', 11),
(11, 'Ilmalämmitys/sähköpatterilämmitys', 'Luftuppvärmning/uppvärmning med elelement', 12),
(12, 'Muu lämmönjako', 'Annat värmedistribution', 13)
ON CONFLICT (id) DO UPDATE
SET
label_fi = excluded.label_fi,
label_sv = excluded.label_sv;
