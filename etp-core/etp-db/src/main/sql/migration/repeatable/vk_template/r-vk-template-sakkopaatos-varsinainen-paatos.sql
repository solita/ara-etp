insert into vk_template (id,
                         label_fi,
                         label_sv,
                         ordinal,
                         toimenpidetype_id,
                         language,
                         valid,
                         tiedoksi,
                         content)
values (9, 'Sakkopäätös / varsinainen päätös', 'Sakkopäätös / varsinainen päätös (sv)', 1, 15, 'fi', true, true,
        $$
<div class="otsikko-ja-vastaanottaja-container">
    <div class="otsikko">
        <b>Päätös</b>
        <b>{{päivä}}</b> <br/>
        Dnro: {{diaarinumero}}
    </div>

    <div class="vastaanottaja">
        {{#omistaja-henkilo}}
        {{etunimi}} {{sukunimi}}<br/>
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

<h1>Uhkasakon tuomitseminen maksettavaksi</h1>

<div>
    Vastaanottaja/asianosainen: {{#omistaja-henkilo}}{{etunimi}} {{sukunimi}}{{/omistaja-henkilo}}{{#omistaja-yritys}}{{nimi}}{{/omistaja-yritys}}<br/>
</div>

<h2>Päätös</h2>

<p>Asumisen rahoitus- ja kehittämiskeskus (ARA) tuomitsee
    {{#omistaja-henkilo}}
    {{etunimi}} {{sukunimi}}
    {{/omistaja-henkilo}}
    {{#omistaja-yritys}}
    {{nimi}}
    {{/omistaja-yritys}}
    (jäljempänä Asianosainen) maksettavaksi
    hänelle ARAn päätöksessä {{#aiemmat-toimenpiteet}}{{varsinainen-paatos-pvm}} (dnro {{varsinainen-paatos-diaarinumero}}, jäljempänä ARAn päätös){{/aiemmat-toimenpiteet}} asetetun {{#tyyppikohtaiset-tiedot}}{{fine}}{{/tyyppikohtaiset-tiedot}} euron
    kiinteän uhkasakon, koska päävelvoitetta ei ole noudatettu, eikä noudattamatta jättämiseen ole pätevää syytä.
    Uhkasakko tuomitaan maksettavaksi kerralla.
    Tilisiirtolomake uhkasakon maksamiseksi on tämän päätöksen liitteenä. Uhkasakko maksetaan valtiolle ja sen
    perinnästä vastaa Oikeusrekisterikeskus.
</p>

<h2>Kuuleminen</h2>

{{#aiemmat-toimenpiteet}}
<p>ARA varasi {{sakkopaatos-kuulemiskirje-pvm}} päivätyllä kirjeellään (dnro {{sakkopaatos-kuulemiskirje-diaarinumero}}) Asianosaiselle uhkasakkolain 22 §:n ja
    siinä viitatun hallintolain (434/2003) 34 §:n mukaisesti ennen asian ratkaisemista tilaisuuden lausua
    mielipiteensä asiasta sekä antaa selityksensä sellaisista vaatimuksista ja selvityksistä, jotka saattavat
    vaikuttaa asian ratkaisuun.</p>
{{/aiemmat-toimenpiteet}}

{{#tyyppikohtaiset-tiedot}}
<p class="respect-new-lines">{{vastaus-fi}}</p>

{{^recipient-answered}}
<div class="sivunvaihto"></div>
{{/recipient-answered}}
{{/tyyppikohtaiset-tiedot}}

<h3>Päätöksen perustelut</h3>

<p>Rakennuksen energiatodistuksesta annetun lain (50/2013, energiatodistuslaki) 6 §:n mukaan rakennusta, huoneistoa tai
    niiden hallintaoikeutta myytäessä tai vuokrattaessa täytyy olla energiatodistus. Energiatodistus on annettava joko
    alkuperäisenä tai jäljennöksenä ostajalle tai vuokralaiselle. Kohteen julkisessa myynti- tai vuokrausilmoituksessa
    on mainittava myytävän tai vuokrattavan kohteen energiatehokkuusluokka. Rakennuksen omistaja tai haltija vastaa
    siitä, että rakennuksen energiatodistus hankitaan ja sitä käytetään laissa säädetyissä tilanteissa
    (energiatodistuslain 2 §).</p>

<p>Seuraamuksista säädetään energiatodistuslain 24 §:ssä. Jos rakennuksen omistaja ei täytä energiatodistuslaissa
    säädettyjä velvollisuuksia tai toimii muutoin energiatodistuslain tai sen nojalla annettujen säännösten
    vastaisesti, ARAn on kehotettava korjaamaan asiantila ja asetettava määräaika asiantilan korjaamiselle. Jos asiaa
    ei korjata määräajassa, ARAn on annettava asianomaiselle taholle varoitus ja uusi määräaika. Jos asiantilaa ei
    määräajassa korjata, ARAn tulee antaa tilanteen mukainen käsky- tai kieltopäätös. ARA voi tehostaa käskyä tai
    kieltoa vakavissa tai olennaisissa rikkomuksissa uhkasakolla tai teettämis- tai keskeyttämisuhalla, joista
    säädetään uhkasakkolaissa (1113/1990).</p>

<p>Uhkasakkolain (1113/1990) 10 §:n mukaan uhkasakon asettanut viranomainen voi tuomita uhkasakon maksettavaksi, jos
    päävelvoitetta ei ole noudatettu, eikä noudattamatta jättämiseen ole pätevää syytä. Edellytyksenä uhkasakon
    tuomitsemiselle maksettavaksi on myös, että uhkasakon asettamista koskeva päätös on lainvoimainen, jollei päätöstä
    ole säädetty tai määrätty noudatettavaksi muutoksenhausta huolimatta. Lain 7 §:n mukaan kullekin asianosaiselle on
    asetettava eri uhkasakko, jos uhkasakko kohdistetaan useisiin asianosaisiin.</p>

<p>Asianosainen on laiminlyönyt noudattaa energiatodistuslain 6 §:ssä säädettyä velvollisuutta käyttää energiatodistusta
    rakennuksen myyntitilanteessa. ARA antoi laiminlyönnin johdosta Asianosaiselle energiatodistuslain 24 §:n mukaisen
    kehotuksen ja varoituksen energiatodistuksen hankkimiseen, mutta niitä ei noudatettu. Näin ollen ARA teki
    {{#aiemmat-toimenpiteet}}{{varsinainen-paatos-pvm}}{{/aiemmat-toimenpiteet}} päätöksen, jolla se käski Asianosaisen hankkimaan osoitteessa {{#kohde}}{{katuosoite}} {{postinumero}} {{postitoimipaikka}}{{/kohde}}
    sijaitsevalle rakennukselle energiatodistuksen viimeistään {{#aiemmat-toimenpiteet}}{{varsinainen-paatos-maarapaiva}}{{/aiemmat-toimenpiteet}} ja esittämään energiatodistuksen ARAlle.
    Päätöksen tehosteeksi ARA asetti {{#aiemmat-toimenpiteet}}{{varsinainen-paatos-fine}}{{/aiemmat-toimenpiteet}} euron suuruisen kiinteän uhkasakon. Päätöksen mukaan uhkasakko tuomitaan
    maksettavaksi, mikäli ARAn käskyä ei noudateta annetussa määräajassa.</p>

{{#tyyppikohtaiset-tiedot}}
<p class="respect-new-lines">{{statement-fi}}</p>
{{/tyyppikohtaiset-tiedot}}

<h2>Maksuohjeet</h2>

<p>Tilisiirtolomake uhkasakon maksamiseksi on tämän päätöksen liitteenä.</p>

<h2>Sovelletut oikeusohjeet</h2>

<ul>
    <li class="no-indicator">Laki rakennuksen energiatodistuksesta (50/2013) 2 §, 6 § ja 24 §</li>
    <li class="no-indicator">Uhkasakkolaki (1113/1990) 7 § ja 10 §</li>
</ul>

<h2>Muutoksenhaku</h2>

<p>Tähän päätökseen tyytymätön saa hakea siihen muutosta {{#tyyppikohtaiset-tiedot}}{{oikeus-fi}}{{/tyyppikohtaiset-tiedot}}. Valitus on tehtävä ja
    toimitettava edellä mainittuun hallinto-oikeuteen 30 päivän kuluessa tämän päätöksen tiedoksisaannista.</p>

<p>Tarkemmat ohjeet valituksen tekemisestä ovat tämän päätöksen liitteenä.</p>

<div class="page-break-avoid">
{{#valvoja}}
<p>Lisätietoja päätöksestä antaa energia-asiantuntija {{etunimi}} {{sukunimi}}, <br/>puh. {{puhelin}} / s-posti
    {{email}}.</p>
{{/valvoja}}

<table class="max-width">
    <tr>
        <td>
            {{#tyyppikohtaiset-tiedot}}
            <div>{{department-head-name}}</div>
            <div>{{department-head-title-fi}}</div>
            {{/tyyppikohtaiset-tiedot}}
        </td>

        <td>
            {{#valvoja}}
            <div>{{etunimi}} {{sukunimi}}</div>
            {{/valvoja}}
            <div>energia-asiantuntija</div>
        </td>
    </tr>
</table>

<p class="small-text">Tämä asiakirja on allekirjoitettu sähköisesti asianhallintajärjestelmässä.</p>
</div>

<div class="sivunvaihto"></div>

<div class="otsikko-ja-vastaanottaja-container">
    <div class="otsikko">
        <b>Beslut</b>
        <b>{{päivä}}</b> <br />
        Dnr {{diaarinumero}}
    </div>

    <div class="vastaanottaja">
        {{#omistaja-henkilo}}
        {{etunimi}} {{sukunimi}}<br />
        {{jakeluosoite}}<br />
        {{postinumero}} {{postitoimipaikka}}
        {{/omistaja-henkilo}}
        {{#omistaja-yritys}}
        {{nimi}}<br />
        {{vastaanottajan-tarkenne}}<br />
        {{jakeluosoite}}<br />
        {{postinumero}} {{postitoimipaikka}}
        {{/omistaja-yritys}}
    </div>
</div>

<h1>Utdömande av vite</h1>

<div>
    Mottagare: {{#omistaja-henkilo}}{{etunimi}}
    {{sukunimi}}{{/omistaja-henkilo}}{{#omistaja-yritys}}{{nimi}}{{/omistaja-yritys}}<br />
</div>

<h2>Beslut</h2>

<p>Finansierings- och utvecklingscentralen för boendet (ARA) dömer
    {{#omistaja-henkilo}}
    {{etunimi}} {{sukunimi}}
    {{/omistaja-henkilo}}
    {{#omistaja-yritys}}
    {{nimi}}
    {{/omistaja-yritys}}
    att betala det fasta vite på {{#tyyppikohtaiset-tiedot}}{{fine}}{{/tyyppikohtaiset-tiedot}} euro som fastställts i
    ARAs beslut {{#aiemmat-toimenpiteet}}{{varsinainen-paatos-pvm}} (dnr {{varsinainen-paatos-diaarinumero}}, nedan ARAs
    beslut){{/aiemmat-toimenpiteet}}, eftersom huvudförpliktelsen inte har iakttagits och det inte finns någon giltig
    orsak för varför den inte har iakttagits. Vitet döms ut på en gång.
</p>

<p>Gireringsblanketten för betalning av vite finns som bilaga till detta beslut. </p>

<h2>Hörande</h2>

{{#aiemmat-toimenpiteet}}
<p>AARA gav i sitt brev (dnr {{sakkopaatos-kuulemiskirje-diaarinumero}}) daterat {{sakkopaatos-kuulemiskirje-pvm}}
    {{#omistaja-henkilo}}
    {{sukunimi}}
    {{/omistaja-henkilo}}
    {{#omistaja-yritys}}
    {{nimi}}
    {{/omistaja-yritys}} i enlighet med 22 § i viteslagen och den hänvisade 34 § i förvaltningslagen (434/2003) möjlighet att
    yttra sig i ärendet samt ge sin förklaring till sådana yrkanden och utredningar som kan påverka avgörandet av
    ärendet.</p>
{{/aiemmat-toimenpiteet}}

<p class="respect-new-lines">{{#omistaja-henkilo}}{{sukunimi}}{{/omistaja-henkilo}}{{#omistaja-yritys}}{{nimi}}{{/omistaja-yritys}} {{#tyyppikohtaiset-tiedot}}{{vastaus-sv}}{{/tyyppikohtaiset-tiedot}}</p>

{{#tyyppikohtaiset-tiedot}}
{{^recipient-answered}}
<div class="sivunvaihto"></div>
{{/recipient-answered}}
{{/tyyppikohtaiset-tiedot}}

<h3>Motivering av beslutet</h3>

<p>Enligt 6 § i lagen om energicertifikat för byggnader (50/2013, lagen om energicertifikat för byggnader) ska det
    finnas ett energicertifikat vid försäljning eller uthyrning av en byggnad eller lägenhet eller besittningsrätten
    till dem. Energicertifikatet ska överlämnas till köparen eller hyrestagaren antingen i original eller som kopia. I
    en offentlig anmälan om försäljning eller uthyrning av ett objekt ska energieffektivitetsklassen för det objekt som
    säljs eller hyrs ut nämnas. Byggnadens ägare eller innehavare ansvarar för att byggnadens energicertifikat skaffas
    och används i de situationer som regleras i lagen (2 § i lagen om energicertifikat).</p>

<p>Bestämmelser om påföljderna finns i 24 § i lagen om energicertifikat. Om byggnadens ägare inte fullgör sina
    skyldigheter enligt lagen om energicertifikat eller i övrigt handlar i strid med lagen om energicertifikat eller
    bestämmelserna som utfärdats med stöd av den, ska ARA uppmana ägaren att rätta till situationen och ange en
    tidsfrist inom vilken situationen ska rättas till. Om saken inte rättas till inom tidsfristen ska ARA ge den som
    saken gäller en varning och ange en ny tidsfrist. Om situationen inte rättas till inom tidsfristen ska ARA enligt
    vad situationen kräver meddela beslut om föreläggande eller förbud. Vid allvarliga eller väsentliga överträdelser
    kan ARA förena föreläggandet eller förbudet med vite eller med hot om tvångsutförande eller hot om avbrytande, om
    vilka det bestäms i viteslagen (1113/1990).</p>

<p>Enligt 10 § i viteslagen (1113/1990) kan den myndighet som har förelagt vitet döma ut vitet, om huvudförpliktelsen
    inte har iakttagits och det inte finns någon giltig orsak till försummelsen. En förutsättning för utdömande av vite
    är också att beslutet om föreläggande av vite har vunnit laga kraft, om det inte har föreskrivits eller bestämts att
    beslutet skall iakttas trots att ändring har sökts.
    Enligt 7 § lagen ska olika vitesföreläggande bestämmas för varje inblandad part, om vitet åläggs flera parter.
</p>

<p> {{#omistaja-henkilo}}
    {{etunimi}} {{sukunimi}}
    {{/omistaja-henkilo}}
    {{#omistaja-yritys}}
    {{nimi}}
    {{/omistaja-yritys}} har försummat skyldigheten enligt 6 § i lagen om energicertifikat att använda energicertifikat
    vid försäljning av en byggnad. På grund av försummelsen gav ARA
    {{#omistaja-henkilo}}
    {{sukunimi}}
    {{/omistaja-henkilo}}
    {{#omistaja-yritys}}
    {{nimi}}
    {{/omistaja-yritys}} en uppmaning och varning enligt 24 § i
    lagen om energicertifikat om att skaffa energicertifikat, men dessa efterlevdes inte. Således fattade ARA ett beslut
    {{#aiemmat-toimenpiteet}}{{varsinainen-paatos-pvm}}{{/aiemmat-toimenpiteet}} päätöksen, genom vilket ARA beordrade
    {{#omistaja-henkilo}}
    {{etunimi}} {{sukunimi}}
    {{/omistaja-henkilo}}
    {{#omistaja-yritys}}
    {{nimi}}
    {{/omistaja-yritys}} att skaffa ett energicertifikat för byggnaden på adressen {{#kohde}}{{katuosoite}}
    {{postinumero}} {{postitoimipaikka}}{{/kohde}}
    senast {{#aiemmat-toimenpiteet}}{{varsinainen-paatos-maarapaiva}}{{/aiemmat-toimenpiteet}} och visa upp
    energicertifikatet för ARA. ARA förenade beslutet med ett fast vite på
    {{#aiemmat-toimenpiteet}}{{varsinainen-paatos-fine}}{{/aiemmat-toimenpiteet}} euro. Enligt beslutet döms vitet ut om
    ARAs order inte iakttas inom utsatt tid.</p>

{{#tyyppikohtaiset-tiedot}}
<p class="respect-new-lines">{{statement-sv}}</p>
{{/tyyppikohtaiset-tiedot}}

<h2>Betalningsanvisningar</h2>

<p>Gireringsblanketten för betalning av vite finns som bilaga till detta beslut.</p>

<h2>Tillämpliga rättsnormer</h2>

<ul>
    <li class="no-indicator">2 §, 6 § och 24 § i lagen om energicertifikat för byggnader (50/2013)</li>
    <li class="no-indicator">10 § i viteslagen (1113/1990)</li>
</ul>

<h2>Ändringssökande</h2>

<p> Den som är missnöjd med detta beslut kan söka ändring i det
    hos {{#tyyppikohtaiset-tiedot}}{{oikeus-sv}}{{/tyyppikohtaiset-tiedot}} förvaltningsdomstolen. Besvär måste göras och
    levereras till ovan nämnda förvaltningsdomstol inom 30 dagar från delgivningen av det här beslutet.</p>

<p>Närmare anvisningar om anförande av besvär finns som bilaga till detta beslut.</p>

<div class="page-break-avoid">
{{#valvoja}}
<p>Mer information om beslutet ges av energiexpert {{etunimi}} {{sukunimi}}, <br />tfn {{puhelin}} / e-post
    {{email}}.</p>
{{/valvoja}}

<table class="max-width">
    <tr>
        <td>
            {{#tyyppikohtaiset-tiedot}}
            <div>{{department-head-name}}</div>
            <div>{{department-head-title-sv}}</div>
            {{/tyyppikohtaiset-tiedot}}
        </td>

        <td>
            {{#valvoja}}
            <div>{{etunimi}} {{sukunimi}}</div>
            {{/valvoja}}
            <div>energiexpert</div>
        </td>
    </tr>
</table>

<p class="small-text">Denna handling har undertecknats elektroniskt i ärendehanterings- systemet</p>
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
