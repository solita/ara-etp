insert into vk_template (id,
                         label_fi,
                         label_sv,
                         ordinal,
                         toimenpidetype_id,
                         language,
                         valid,
                         tiedoksi,
                         content)
values (8, 'Käskypäätös / tiedoksianto (Haastemies)', 'Käskypäätös / tiedoksianto (Haastemies) (sv)', 1, 11, 'fi', true,
        true,
        $$
<div class="otsikko-ja-vastaanottaja-container">
    <div class="otsikko">
        <b>Lähete {{päivä}}</b><br/>
        {{diaarinumero}}<br/>
    </div>

    <div class="vastaanottaja">
    {{#tyyppikohtaiset-tiedot}}
        {{#karajaoikeus}}
        {{label-fi}}<br/>
        {{/karajaoikeus}}
        Haastemiehet<br/>
        {{haastemies-email}}<br/>
    {{/tyyppikohtaiset-tiedot}}
    </div>
</div>

<h1>Asiakirjan toimituspyyntö</h1>
<div style="height: 2.5cm">
<div style="float: left;">Vastaanottaja:</div>
<div style="float: none; overflow: hidden; padding-left: 3.5cm">
{{#omistaja-henkilo}}
    {{etunimi}} {{sukunimi}} ({{henkilotunnus}})<br/>
    {{jakeluosoite}}<br/>
    {{postinumero}} {{postitoimipaikka}}<br/>
{{/omistaja-henkilo}}
{{#omistaja-yritys}}
    {{nimi}}<br/>
    {{vastaanottajan-tarkenne}}<br/>
    {{jakeluosoite}}<br/>
    {{postinumero}} {{postitoimipaikka}}
{{/omistaja-yritys}}
</div>
</div>

<h2>Toimitettavat asiakirjat (Päätös+liite)</h2>
<div>
    Käsky hankkia energiatodistus ja uhkasakon asettaminen,<br/>
    {{diaarinumero}}<br/>
    Liite:	valitusosoitus
</div>

<h2>Laskutustiedot</h2>
<div>
    Asumisen rahoitus- ja kehittämiskeskus<br/>
    Verkkolaskuosoite/OVT-tunnus: 003709483205<br/>
    Välittäjätunnus (OpusCapita Solutions Oy): E204503<br/>
    Y-tunnus: 0948320-5<br/>
    ALV-tunnus (VAT-number): FI09483205<br/>
    Laskussa mainittava (Viite tms):ET/{{diaarinumero}}<br/>
    <p>ARA pyytää lähettämään tiedot (PÄÄTÖS ja Dnro) asiakirjan toimittamisesta sähköpostina kirjaamoon (kirjaamo.ara@ara.fi)</p>
</div>

<h2>Liitteet</h2>
<div>
    PÄÄTÖS<br/>
    Valitusosoitus
</div>
$$)
on conflict (id) do update set label_fi          = excluded.label_fi,
                               label_sv          = excluded.label_sv,
                               ordinal           = excluded.ordinal,
                               toimenpidetype_id = excluded.toimenpidetype_id,
                               language          = excluded.language,
                               valid             = excluded.valid,
                               tiedoksi          = excluded.tiedoksi,
                               content           = excluded.content;
