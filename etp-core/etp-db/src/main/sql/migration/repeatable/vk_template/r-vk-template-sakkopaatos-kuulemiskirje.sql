insert into vk_template (id,
                         label_fi,
                         label_sv,
                         ordinal,
                         toimenpidetype_id,
                         language,
                         valid,
                         tiedoksi,
                         content)
values (7, 'Sakkopäätös / kuulemiskirje', 'Sakkopäätös / kuulemiskirje (sv)', 1, 14, 'fi', true, true,
        $$
<div class="otsikko-ja-vastaanottaja-container">
    <div class="otsikko">
        <b>Kuulemiskirje</b>
        <b>{{päivä}}</b> <br/>
        Dnro: {{diaarinumero}}
    </div>

    <div class="vastaanottaja">
        {{#omistaja-henkilo}}
        {{sukunimi}} {{etunimi}}<br/>
        {{jakeluosoite}}<br/>
        {{postinumero}} {{postitoimipaikka}}
        {{/omistaja-henkilo}}
        {{#omistaja-yritys}}
        {{nimi}}<br/>
        {{vastaanottajan-tarkenne}}<br/>
        {{jakeluosoite}}<br/>
        {{postinumero}} {{postitoimipaikka}}
        {{/omistaja-yritys}}
    </div>
</div>

<h1>Kuuleminen uhkasakon tuomitsemisesta</h1>

<div>
    Rakennuksen omistaja: {{#omistaja-henkilo}}{{sukunimi}}
    {{etunimi}}{{/omistaja-henkilo}}{{#omistaja-yritys}}{{nimi}}{{/omistaja-yritys}}<br/>
    Rakennus: {{#kohde}}{{katuosoite}} {{postinumero}} {{postitoimipaikka}}{{/kohde}}
</div>

<h2>Asian tausta</h2>

<p>Varke on {{#aiemmat-toimenpiteet}}{{varsinainen-paatos-pvm}}{{/aiemmat-toimenpiteet}} tekemällään päätöksellä velvoittanut
    {{#omistaja-henkilo}}
    {{etunimi}} {{sukunimi}}
    {{/omistaja-henkilo}}
    {{#omistaja-yritys}}
    {{nimi}}
    {{/omistaja-yritys}}
    hankkimaan osoitteessa {{#kohde}}{{katuosoite}} {{postinumero}} {{postitoimipaikka}}{{/kohde}} sijaitsevalle
    rakennukselle energiatodistuksen viimeistään {{#aiemmat-toimenpiteet}}{{varsinainen-paatos-maarapaiva}}{{/aiemmat-toimenpiteet}} ja esittämään
    energiatodistuksen Varkelle. Päätöksen tehosteeksi Varke on asettanut
    {{#tyyppikohtaiset-tiedot}}{{fine}}{{/tyyppikohtaiset-tiedot}} euron suuruisen kiinteän uhkasakon.
    Päätöksen mukaan uhkasakko tuomitaan maksettavaksi, mikäli Varken käskyä ei ole annetussa määräajassa noudatettu.</p>

<p>Varken päätökseen ei ole haettu muutosta, eli päätös on lainvoimainen. Energiatodistusta ei ole hankittu eikä esitetty
    Varkelle määräajassa. Näin ollen Varke aikoo tehdä päätöksen
    {{#tyyppikohtaiset-tiedot}}{{fine}}{{/tyyppikohtaiset-tiedot}} euron uhkasakon tuomitsemisesta maksuun.</p>

<h2>Asianosaisen kuuleminen</h2>

<p>Ennen asian ratkaisemista Varke varaa Teille hallintolain (434/2003) 34 §:n mukaisen tilaisuuden lausua mielipiteenne
    asiasta sekä antaa selitys sellaisista vaatimuksista ja selvityksistä, jotka voivat vaikuttaa asian ratkaisuun.
    Vastaus on annettava viimeistään {{määräpäivä}}. Vastauksen antamatta jättäminen ei
    estä asian ratkaisemista.</p>

<div class="page-break-avoid">
<p>Vastaus pyydetään toimittamaan Varken kirjaamoon joko sähköpostitse <br/>varke.ym@gov.fi tai postitse Valtion tukeman asuntorakentamisen keskus,
PL 35, 00023 Valtioneuvosto</p>

{{#valvoja}}
{{etunimi}} {{sukunimi}}
<br/>
{{titteli-fi}}
{{/valvoja}}
</div>

<div class="sivunvaihto"></div>

<div class="otsikko-ja-vastaanottaja-container">
    <div class="otsikko">
        <b>Brev om hörande</b>
        <b>{{päivä}}</b> <br/>
        Dnr: {{diaarinumero}}
    </div>

    <div class="vastaanottaja">
        {{#omistaja-henkilo}}
        {{sukunimi}} {{etunimi}}<br/>
        {{jakeluosoite}}<br/>
        {{postinumero}} {{postitoimipaikka}}
        {{/omistaja-henkilo}}
        {{#omistaja-yritys}}
        {{nimi}}<br/>
        {{vastaanottajan-tarkenne}}<br/>
        {{jakeluosoite}}<br/>
        {{postinumero}} {{postitoimipaikka}}
        {{/omistaja-yritys}}
    </div>
</div>

<h1>Hörande om utdömande av vite</h1>

<div>
    Byggnadens ägare: {{#omistaja-henkilo}}{{sukunimi}}
    {{etunimi}}{{/omistaja-henkilo}}{{#omistaja-yritys}}{{nimi}}{{/omistaja-yritys}}<br/>
    Byggnad: {{#kohde}}{{katuosoite}} {{postinumero}} {{postitoimipaikka}}{{/kohde}}
</div>

<h2>Ärendets bakgrund</h2>

<p>Varke har genom sitt beslut {{#aiemmat-toimenpiteet}}{{varsinainen-paatos-pvm}}{{/aiemmat-toimenpiteet}} ålagt
    {{#omistaja-henkilo}}
    {{etunimi}} {{sukunimi}}
    {{/omistaja-henkilo}}
    {{#omistaja-yritys}}
    {{nimi}}
    {{/omistaja-yritys}}
    att senast {{#aiemmat-toimenpiteet}}{{varsinainen-paatos-maarapaiva}}{{/aiemmat-toimenpiteet}} skaffa ett energicertifikat till byggnaden på adressen
    {{#kohde}}{{katuosoite}} {{postinumero}} {{postitoimipaikka}}{{/kohde}} och visa upp energicertifikatet för Varke. Varke
    har förenat beslutet med ett fast vite på {{#tyyppikohtaiset-tiedot}}{{fine}}{{/tyyppikohtaiset-tiedot}} euro.
    Enligt beslutet döms vitet ut om Varkes order inte har iakttagits inom utsatt tid.</p>

<p>Ändring i Varkes beslut har inte sökts, dvs. beslutet har vunnit laga kraft. Energicertifikatet har inte skaffats eller
    visats upp för Varke inom utsatt tid. Således har Varke för avsikt att fatta beslut om att döma ut vitet på
    {{#tyyppikohtaiset-tiedot}}{{fine}}{{/tyyppikohtaiset-tiedot}} euro.</p>

<h2>Hörande av parter</h2>

<p>Innan ärendet avgörs ger Varke Er tillfälle enligt 34 § i förvaltningslagen (434/2003) att uttala Er åsikt i ärendet
    samt att ge en förklaring till sådana yrkanden och utredningar som kan inverka på avgörandet av ärendet. Svaret ska
    ges senast {{määräpäivä}}. Avsaknaden av svar hindrar inte att ärendet avgörs.</p>

<div class="page-break-avoid">
<p>Vi ber Er skicka svaret till Varkes registratorskontor antingen per e-post till <br/>varke.ym@gov.fi eller per post till
    Finansierings- och ut-vecklingscentralen för boendet, PB 30, 15141 Lahtis.</p>

{{#valvoja}}
{{etunimi}} {{sukunimi}}
<br/>
{{titteli-sv}}
{{/valvoja}}
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
