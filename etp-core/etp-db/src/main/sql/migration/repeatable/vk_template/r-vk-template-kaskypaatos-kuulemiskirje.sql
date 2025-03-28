insert into vk_template (id,
                         label_fi,
                         label_sv,
                         ordinal,
                         toimenpidetype_id,
                         language,
                         valid,
                         tiedoksi,
                         content)
values (5, 'Käskypäätös / kuulemiskirje', 'Käskypäätös / kuulemiskirje (sv)', 1, 7, 'fi', true, true,
        $$
<div class="otsikko-ja-vastaanottaja-container" style="margin-bottom: -1cm">
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

<h1>Kuuleminen uhkasakon asettamisesta</h1>

<div>
    Rakennuksen omistaja: {{#tyyppikohtaiset-tiedot}}{{omistajat}}{{/tyyppikohtaiset-tiedot}}<br/>
    Rakennus: {{#kohde}}{{katuosoite}} {{postinumero}} {{postitoimipaikka}}{{/kohde}}
</div>

<h2>Asian tausta</h2>

<p>Rakennuksen energiatodistuksesta annetun lain (50/2013, energiatodistuslaki) 6 §:n mukaan rakennusta, huoneistoa tai
    niiden hallintaoikeutta myytäessä tai vuokrattaessa täytyy olla energiatodistus. Energiatodistus on annettava joko
    alkuperäisenä tai jäljennöksenä ostajalle tai vuokralaiselle. Kohteen julkisessa myynti- tai vuokrausilmoituksessa
    on mainittava myytävän tai vuokrattavan kohteen energiatehokkuusluokka. Rakennuksen omistaja tai haltija vastaa
    siitä, että rakennuksen energiatodistus hankitaan ja sitä käytetään laissa säädetyissä tilanteissa
    (energiatodistuslain 2 §).</p>

<p>Seuraamuksista säädetään energiatodistuslain 24 §:ssä. Jos rakennuksen omistaja ei täytä energiatodistuslaissa
    säädettyjä velvollisuuksiaan tai toimii muutoin energiatodistuslain tai sen nojalla annettujen säännösten
    vastaisesti, Varken on kehotettava korjaamaan asiantila ja asetettava määräaika asiantilan korjaamiselle. Jos asiaa ei
    korjata määräajassa, Varken on annettava asianomaiselle taholle varoitus ja uusi määräaika. Jos asiantilaa ei
    määräajassa korjata, Varken tulee antaa tilanteen mukainen käsky- tai kieltopäätös. Varke voi tehostaa käskyä tai
    kieltoa vakavissa tai olennaisissa rikkomuksissa uhkasakolla tai teettämis- tai keskeyttämisuhalla, joista
    säädetään uhkasakkolaissa (1113/1990).</p>

{{#kohde}}
<p>Omistamaanne rakennusta osoitteessa {{katuosoite}} {{postinumero}} {{postitoimipaikka}} on markkinoitu julkisesti
    {{ilmoituspaikka}} -sivustolla ilman energiatodistusta.
    {{/kohde}}
    {{#aiemmat-toimenpiteet}}Tästä syystä Varke ryhtyi energiatodistuslain 24 §:n
    mukaisiin toimenpiteisiin ja on lähettänyt Teille {{kehotus-pvm}} kehotuksen.</p>

<p>Kehotuksessa Teitä on pyydetty {{kehotus-maarapaiva}} mennessä antamaan Varkelle tietoa mahdollisesta
    energiatodistuksesta tai
    jos todistusta ei ole, sen laadinta-aikataulusta. Kun kehotusta ei noudatettu, Varke antoi Teille varoituksen, jonka
    määräaika oli {{varoitus-maarapaiva}}. Asiantilaa ei kehotuksesta ja varoituksesta huolimatta korjattu, ja Varken
    energiatodistusrekisteristä ilmenee, ettei kohteelle ole edelleenkään laadittu energiatodistusta<sup>1</sup>.</p>
{{/aiemmat-toimenpiteet}}

<p>Varke voi energiatodistuslain 24 §:n mukaisena viimesijaisena keinona antaa käskypäätöksen, jolla Teidät velvoitetaan
    hankkimaan energiatodistus. Käskyä voidaan tehostaa uhkasakolla, jonka määrä on arviolta {{#tyyppikohtaiset-tiedot}}{{fine}}{{/tyyppikohtaiset-tiedot}} euroa.</p>

<h2>Asianosaisen kuuleminen</h2>

<p>Ennen asian ratkaisemista Varke varaa Teille hallintolain (434/2003) 34 §:n mukaisen tilaisuuden lausua mielipiteenne
    asiasta sekä antaa selitys sellaisista vaatimuksista ja selvityksistä, jotka voivat vaikuttaa asian ratkaisuun.
    Vastaus on annettava viimeistään {{määräpäivä}}. Vastauksen antamatta jättäminen ei estä
    asian ratkaisemista.</p>

<div class="page-break-avoid">
<p>Vastaus pyydetään toimittamaan Varken kirjaamoon joko sähköpostitse <br/>varke.ym@gov.fi tai
postitse Valtion tukeman asuntorakentamisen keskus, PL 35, 00023 Valtioneuvosto.</p>

{{#valvoja}}
{{etunimi}} {{sukunimi}}
<br/>
{{titteli-fi}}
{{/valvoja}}
</div>

<p class="footnote mb-0"><sup>1</sup> Varken energiatodistusrekisterissä ovat 1.5.2015 jälkeen laaditut energiatodistukset.</p>

<div class="sivunvaihto"></div>

<div class="otsikko-ja-vastaanottaja-container" style="margin-bottom: -1cm">
    <div class="otsikko">
        <b>Brev om hörande</b>
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

<h1>Hörande om föreläggande av vite</h1>

<div>
    Byggnadens ägare: {{#tyyppikohtaiset-tiedot}}{{omistajat}}{{/tyyppikohtaiset-tiedot}}<br/>
    Byggnad: {{#kohde}}{{katuosoite}} {{postinumero}} {{postitoimipaikka}}{{/kohde}}
</div>

<h2>Ärendets bakgrund</h2>

<p>Enligt 6 § i lagen om energicertifikat för byggnader (50/2013, lagen om energicertifikat för byggnader) ska det
    finnas ett energicertifikat vid försäljning eller uthyrning av en byggnad eller lägenhet eller besittningsrätten
    till dem. Energicertifikatet ska överlämnas till köparen eller hyrestagaren antingen i original eller som kopia. I
    en offentlig anmälan om försäljning eller uthyrning av ett objekt ska energieffektivitetsklassen för det objekt som
    säljs eller hyrs ut nämnas. Byggnadens ägare eller innehavare ansvarar för att byggnadens energicertifikat skaffas
    och används i de situationer som regleras i lagen (2 § i lagen om energicertifikat).</p>

<p>Bestämmelser om påföljderna finns i 24 § i lagen om energicertifikat. Om byggnadens ägare inte fullgör sina
    skyldigheter enligt lagen om energicertifikat eller i övrigt handlar i strid med lagen om energicertifikat eller
    bestämmelserna som utfärdats med stöd av den, ska Varke uppmana ägaren att rätta till situationen och ange en
    tidsfrist inom vilken situationen ska rättas till. Om saken inte rättas till inom tidsfristen ska Varke ge den som
    saken gäller en varning och ange en ny tidsfrist. Om situationen inte rättas till inom tidsfristen ska Varke enligt
    vad situationen kräver meddela beslut om föreläggande eller förbud. Vid allvarliga eller väsentliga överträdelser
    kan Varke förena föreläggandet eller förbudet med vite eller med hot om tvångsutförande eller hot om avbrytande, om
    vilka det bestäms i viteslagen (1113/1990).</p>

{{#kohde}}
<p>Er byggnad på {{katuosoite}} {{postinumero}} {{postitoimipaikka}} har marknadsförts offentligt på webbplatsen
    {{ilmoituspaikka}} utan energicertifikat.
    {{/kohde}}
    {{#aiemmat-toimenpiteet}}Därför vidtog Varke åtgärder enligt 24 § i lagen om energicertifikat och har skickat en
    uppmaning till Er den {{kehotus-pvm}}.</p>

<p>I uppmaningen har Ni ombetts att senast {{kehotus-maarapaiva}} ge Varke information om ett eventuellt energicertifikat
    eller, om ett sådant inte finns, tidtabellen för upprättandet av certifikatet. När uppmaningen inte följdes gav Varke
    Er en varning vars tidsfrist var {{varoitus-maarapaiva}}. Situationen korrigerades inte trots uppmaning och varning,
    och av Varkes energicertifikatregister framgår att det fortfarande inte har upprättats något energicertifikat för
    adressen<sup>2</sup>.</p>
{{/aiemmat-toimenpiteet}}

<p>Varke kan som sista utväg i enlighet med 24 § i lagen om energicertifikat meddela ett beslut genom vilket Ni åläggs
    att skaffa ett energicertifikat. Ordern kan förenas med vite som uppgår till uppskattningsvis {{#tyyppikohtaiset-tiedot}}{{fine}}{{/tyyppikohtaiset-tiedot}} euro.</p>

<h2>Hörande av parter</h2>

<p>Innan ärendet avgörs ger Varke Er tillfälle enligt 34 § i förvaltningslagen (434/2003) att uttala Er åsikt i ärendet
    samt att ge en förklaring till sådana yrkanden och utredningar som kan inverka på avgörandet av ärendet. Svaret ska
    ges senast {{määräpäivä}}. Avsaknaden av svar hindrar inte att ärendet avgörs. </p>

<div class="page-break-avoid">
<p>Vi ber Er skicka svaret till Varkes registratorskontor antingen per e-post till <br/>varke.ym@gov.fi eller per post till Centralen för statligt stött
bostadsbyggande, PB 35, 00023 Statsrådet.</p>

{{#valvoja}}
{{etunimi}} {{sukunimi}}
<br/>
{{titteli-sv}}
{{/valvoja}}
</div>

<p class="footnote mb-0"><sup>2</sup> I Varkes energicertifikatregister finns energicertifikat som upprättats efter 1 maj 2015.</p>
$$)
on conflict (id) do update set label_fi          = excluded.label_fi,
                               label_sv          = excluded.label_sv,
                               ordinal           = excluded.ordinal,
                               toimenpidetype_id = excluded.toimenpidetype_id,
                               language          = excluded.language,
                               valid             = excluded.valid,
                               tiedoksi          = excluded.tiedoksi,
                               content           = excluded.content;
