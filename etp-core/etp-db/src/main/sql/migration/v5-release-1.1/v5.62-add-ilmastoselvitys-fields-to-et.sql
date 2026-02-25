call create_classification('ilmastoselvitys_laadintaperuste'::name);

alter table energiatodistus
    add column is$laatimisajankohta date,
    add column is$laatija text,
    add column is$yritys text,
    add column is$yritys_osoite text,
    add column is$yritys_postinumero integer references postinumero (id),
    add column is$yritys_postitoimipaikka text,
    add column is$laadintaperuste integer references ilmastoselvitys_laadintaperuste (id),

    add column is$hiilijalanjalki$rakennus$rakennustuotteiden_valmistus numeric,
    add column is$hiilijalanjalki$rakennus$kuljetukset_tyomaavaihe numeric,
    add column is$hiilijalanjalki$rakennus$rakennustuotteiden_vaihdot numeric,
    add column is$hiilijalanjalki$rakennus$energiankaytto numeric,
    add column is$hiilijalanjalki$rakennus$purkuvaihe numeric,

    add column is$hiilijalanjalki$rakennuspaikka$rakennustuotteiden_valmistus numeric,
    add column is$hiilijalanjalki$rakennuspaikka$kuljetukset_tyomaavaihe numeric,
    add column is$hiilijalanjalki$rakennuspaikka$rakennustuotteiden_vaihdot numeric,
    add column is$hiilijalanjalki$rakennuspaikka$energiankaytto numeric,
    add column is$hiilijalanjalki$rakennuspaikka$purkuvaihe numeric,

    add column is$hiilikadenjalki$rakennus$uudelleenkaytto numeric,
    add column is$hiilikadenjalki$rakennus$kierratys numeric,
    add column is$hiilikadenjalki$rakennus$ylimaarainen_uusiutuvaenergia numeric,
    add column is$hiilikadenjalki$rakennus$hiilivarastovaikutus numeric,
    add column is$hiilikadenjalki$rakennus$karbonatisoituminen numeric,

    add column is$hiilikadenjalki$rakennuspaikka$uudelleenkaytto numeric,
    add column is$hiilikadenjalki$rakennuspaikka$kierratys numeric,
    add column is$hiilikadenjalki$rakennuspaikka$ylimaarainen_uusiutuvaenergia numeric,
    add column is$hiilikadenjalki$rakennuspaikka$hiilivarastovaikutus numeric,
    add column is$hiilikadenjalki$rakennuspaikka$karbonatisoituminen numeric;
