insert into vk_template (id,
                         label_fi,
                         label_sv,
                         ordinal,
                         toimenpidetype_id,
                         language,
                         valid,
                         tiedoksi,
                         content)
values (6, 'Käskypäätös / varsinainen päätös', 'Käskypäätös / varsinainen päätös (sv)', 1, 8, 'fi', true, true,
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

<h1>Käsky hankkia energiatodistus ja uhkasakon asettaminen</h1>

<div>
    Vastaanottaja/asianosainen: {{#omistaja-henkilo}}{{sukunimi}} {{etunimi}}{{/omistaja-henkilo}}{{#omistaja-yritys}}{{nimi}}{{/omistaja-yritys}}<br/>
</div>

<h2>Päätös</h2>

<p>Asumisen rahoitus- ja kehittämiskeskus (ARA) katsoo, että
    {{#omistaja-henkilo}}
    {{etunimi}} {{sukunimi}}
    {{/omistaja-henkilo}}
    {{#omistaja-yritys}}
    {{nimi}}
    {{/omistaja-yritys}}
    (jäljempänä Asianosainen) on
    laiminlyönyt noudattaa rakennuksen energiatodistuksesta annetun lain (50/2013, energiatodistuslaki) 6 §:ssä
    säädettyä velvollisuutta käyttää energiatodistusta rakennuksen myyntitilanteessa. Energiatodistuslain 2 §:n mukaan
    rakennuksen omistaja vastaa siitä, että rakennuksen energiatodistus hankitaan ja että sitä tai siinä olevia tietoja
    käytetään laissa säädetyissä tilanteissa.</p>

<p>ARA käskee energiatodistuslain 24 §:n nojalla Asianosaisen hankkimaan osoitteessa {{#kohde}}{{katuosoite}}
    {{postinumero}} {{postitoimipaikka}}{{/kohde}} sijaitsevalle rakennukselle energiatodistuksen viimeistään
    {{määräpäivä}} ja
    esittämään energiatodistuksen ARAlle. ARA asettaa käskyn tehosteeksi uhkasakkolain (1113/1990) 6 §:ssä tarkoitetun
    kiinteän uhkasakon. Uhkasakon suuruus on {{#tyyppikohtaiset-tiedot}}{{fine}}{{/tyyppikohtaiset-tiedot}} euroa. Uhkasakko tuomitaan maksettavaksi, mikäli ARAn käskyä ei
    ole annetussa määräajassa noudatettu.</p>

<h2>Kuuleminen</h2>

<p>ARA varasi {{#aiemmat-toimenpiteet}}{{kuulemiskirje-pvm}}{{/aiemmat-toimenpiteet}} päivätyllä kirjeellään (dnro
    {{#aiemmat-toimenpiteet}}{{kuulemiskirje-diaarinumero}}{{/aiemmat-toimenpiteet}}) Asianosaiselle uhkasakkolain 22 §:n ja
    siinä viitatun hallintolain (434/2003) 34 §:n mukaisesti ennen asian ratkaisemista tilaisuuden lausua
    mielipiteensä asiasta sekä antaa selityksensä sellaisista vaatimuksista ja selvityksistä, jotka saattavat
    vaikuttaa asian ratkaisuun. Kuulemiskirjeessä ARA kertoi, että uhkasakon määrä on arviolta {{#aiemmat-toimenpiteet}}{{kuulemiskirje-fine}}{{/aiemmat-toimenpiteet}} euroa.</p>

<p class="respect-new-lines">{{#tyyppikohtaiset-tiedot}}{{vastaus-fi}}{{/tyyppikohtaiset-tiedot}}</p>

<h2>Päätöksen perustelut</h2>

<p>Energiatodistuslain 6 §:n mukaan rakennusta, huoneistoa tai niiden hallintaoikeutta myytäessä tai vuokrattaessa
    täytyy olla energiatodistus. Energiatodistus on annettava joko alkuperäisenä tai jäljennöksenä ostajalle tai
    vuokralaiselle. Kohteen julkisessa myynti- tai vuokrausilmoituksessa on mainittava myytävän tai vuokrattavan kohteen
    energiatehokkuusluokka. Rakennuksen omistaja tai haltija vastaa siitä, että rakennuksen energiatodistus hankitaan ja
    sitä käytetään laissa säädetyissä tilanteissa (energiatodistuslain 2 §).</p>

<p>Seuraamuksista säädetään energiatodistuslain 24 §:ssä. Jos rakennuksen omistaja ei täytä energiatodistuslaissa
    säädettyjä velvollisuuksia tai toimii muutoin energiatodistuslain tai sen nojalla annettujen säännösten
    vastaisesti, ARAn on kehotettava korjaamaan asiantila ja asetettava määräaika asiantilan korjaamiselle. Jos asiaa
    ei korjata määräajassa, ARAn on annettava asianomaiselle taholle varoitus ja uusi määräaika. Jos asiantilaa ei
    määräajassa korjata, ARAn tulee antaa tilanteen mukainen käsky- tai kieltopäätös. ARA voi tehostaa käskyä tai
    kieltoa vakavissa tai olennaisissa rikkomuksissa uhkasakolla tai teettämis- tai keskeyttämisuhalla, joista
    säädetään uhkasakkolaissa (1113/1990).</p>

<p>ARA on lähettänyt Asianosaiselle kehotuksen, sillä Asianosaisen omistamaa rakennusta osoitteessa
    {{#kohde}}{{katuosoite}} {{postinumero}} {{postitoimipaikka}} on
    markkinoitu {{ilmoituspaikka}} -sivustolla ilman energiatodistusta.{{/kohde}}
    Kehotuksessa Asianosaista on pyydetty {{#aiemmat-toimenpiteet}}{{kehotus-maarapaiva}}{{/aiemmat-toimenpiteet}}
    mennessä antamaan ARAlle tietoa mahdollisesta energiatodistuksesta tai jos todistusta ei ole, sen
    laadinta-aikataulusta.</p>

{{#aiemmat-toimenpiteet}}
<p>Kohteen markkinointia kuitenkin jatkettiin kehotuksesta huolimatta, eikä Asianosainen esittänyt energiatodistusta
    ARAlle. Tästä syystä ARA ryhtyi energiatodistuslain 24 §:n mukaisiin toimenpiteisiin ja antoi Asianosaiselle
    varoituksen esittää energiatodistus {{varoitus-maarapaiva}} mennessä. Asiantilaa ei kehotuksesta ja varoituksesta
    huolimatta
    korjattu, ja ARAn energiatodistusrekisteristä ilmenee, ettei kohteelle ole edelleenkään laadittu energiatodistusta
    <sup>1</sup>.</p>
{{/aiemmat-toimenpiteet}}

<p>ARA on siis antanut Asianosaiselle monta mahdollisuutta korjata toimintansa. ARA voi energiatodistuslain 24 §:n
    mukaisena viimesijaisena keinona antaa käskypäätöksen, jolla rakennuksen omistaja velvoitetaan hankkimaan
    energiatodistus. Käskyä voidaan tehostaa uhkasakolla, josta säädetään uhkasakkolaissa.</p>

{{#tyyppikohtaiset-tiedot}}
{{#recipient-answered}}
<h3>Kannanotto vastineeseen</h3>

<p class="respect-new-lines">{{statement-fi}}</p>
{{/recipient-answered}}
{{/tyyppikohtaiset-tiedot}}

<h2>Sovelletut oikeusohjeet</h2>

<ul>
    <li class="no-indicator">Laki rakennuksen energiatodistuksesta (50/2013) 2 §, 6 § ja 24 §</li>
    <li class="no-indicator">Uhkasakkolaki (1113/1990) 4 §, 6 §, 8 § ja 22 §</li>
    <li class="no-indicator">Hallintolaki (434/2003) 34 §</li>
</ul>

<h2>Muutoksenhaku</h2>

<p>Tähän päätökseen tyytymätön saa hakea siihen muutosta {{#tyyppikohtaiset-tiedot}}{{oikeus-fi}}{{/tyyppikohtaiset-tiedot}}. Valitus on tehtävä ja
    toimitettava edellä mainittuun hallinto-oikeuteen 30 päivän kuluessa tämän päätöksen tiedoksisaannista.</p>

<div class="footnote">
    <p class="mb-0"><sup>1</sup> ARAn energiatodistusrekisterissä ovat 1.5.2015 jälkeen laaditut
        energiatodistukset.</p>
</div>

<p>Tarkemmat ohjeet valituksen tekemisestä ovat tämän päätöksen liitteenä.</p>

<div class="page-break-avoid">
{{#valvoja}}
<p>Lisätietoja päätöksestä antaa energia-asiantuntija {{etunimi}} {{sukunimi}}, <br/>puh. {{puhelin}} / s-posti
    {{email}}. </p>
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

<p class="small-text"><i>Asiakirja on allekirjoitettu sähköisesti asianhallintajärjestelmässä.</i></p>
</div>

<div class="sivunvaihto"></div>


<div class="otsikko-ja-vastaanottaja-container">
    <div class="otsikko">
        <b>Beslut</b>
        <b>{{päivä}}</b> <br/>
        Dnr: {{diaarinumero}}
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

<h1>Order att skaffa energicertifikat och föreläg-gande av vite</h1>

<div>
    Mottagare: {{#omistaja-henkilo}}{{sukunimi}}
    {{etunimi}}{{/omistaja-henkilo}}{{#omistaja-yritys}}{{nimi}}{{/omistaja-yritys}}<br/>
</div>

<h2>Beslut</h2>

<p>Finansierings- och utvecklingscentralen för boendet (ARA) anser att
    {{#omistaja-henkilo}}
    {{etunimi}} {{sukunimi}}
    {{/omistaja-henkilo}}
    {{#omistaja-yritys}}
    {{nimi}}
    {{/omistaja-yritys}}
    har försummat skyldigheten enligt 6 § i lagen om energicertifikat för byggnader (50/2013, lagen om energicertifikat)
    att använda energicertifikat vid försäljning av byggnaden. Enligt 2 § i lagen om energicertifikat ansvarar
    byggnadens ägare för att byggnadens energicertifikat skaffas och att certifikatet eller uppgifterna i det används i
    lagstadgade situationer.</p>

<p>Med stöd av 24 § i lagen om energicertifikat beordrar ARA {{#omistaja-henkilo}}
    {{sukunimi}}
    {{/omistaja-henkilo}}
    {{#omistaja-yritys}}
    {{nimi}}
    {{/omistaja-yritys}} att skaffa ett energicertifikat för byggnaden på adressen {{#kohde}}{{katuosoite}}
    {{postinumero}} {{postitoimipaikka}}{{/kohde}} senast
    {{määräpäivä}} och visa upp energicertifikatet för ARA. ARA förenar förordnandet med ett sådant fast vite som avses
    i 6 § i viteslagen (1113/1990). Vitesbeloppet är {{#tyyppikohtaiset-tiedot}}{{fine}}{{/tyyppikohtaiset-tiedot}}
    euro.
    Vitet döms ut om ARAs order inte har iakttagits inom utsatt tid.</p>

<h2>Hörande</h2>

<p>ARA gav i sitt brev (dnr
    {{#aiemmat-toimenpiteet}}{{kuulemiskirje-diaarinumero}}) daterat {{kuulemiskirje-pvm}}{{/aiemmat-toimenpiteet}}

    {{#omistaja-henkilo}}
    {{sukunimi}}
    {{/omistaja-henkilo}}
    {{#omistaja-yritys}}
    {{nimi}}
    {{/omistaja-yritys}}
    i enlighet med 22 § i viteslagen och den hänvisade 34 § i förvaltningslagen (434/2003) möjlighet
    att yttra sig i ärendet samt ge sin förklaring till sådana yrkanden och utredningar som kan påverka avgörandet av
    ärendet. I brevet om hörande berättade ARA att vitesbeloppet är uppskattningsvis
    {{#aiemmat-toimenpiteet}}{{kuulemiskirje-fine}}{{/aiemmat-toimenpiteet}} euro.</p>

<p>{{#omistaja-henkilo}}
    {{sukunimi}}
    {{/omistaja-henkilo}}
    {{#omistaja-yritys}}
    {{nimi}}
    {{/omistaja-yritys}}
    <span class="respect-new-lines">{{#tyyppikohtaiset-tiedot}}{{vastaus-sv}}{{/tyyppikohtaiset-tiedot}}</span></p>

<h2>Motivering av beslutet</h2>

<p>Vid försäljning eller uthyrning av en byggnad eller lägenhet eller besittningsrätten till dem måste det finnas ett
    energicertifikat enligt 6§ i lagen om energicertifikat. Energicertifikatet ska överlämnas till köparen eller
    hyrestagaren antingen i original eller som kopia. I en offentlig anmälan om försäljning eller uthyrning av ett
    objekt ska energieffektivitetsklassen för det objekt som säljs eller hyrs ut nämnas. Byggnadens ägare eller
    innehavare ansvarar för att byggnadens energicertifikat skaffas och används i de situationer som regleras i lagen
    (2 § i lagen om energicertifikat).</p>

<p>Bestämmelser om påföljderna finns i 24 § i lagen om energicertifikat. Om byggnadens ägare inte fullgör sina
    skyldigheter enligt lagen om energicertifikat eller i övrigt handlar i strid med lagen om energicertifikat eller
    bestämmelserna som utfärdats med stöd av den, ska ARA uppmana ägaren att rätta till situationen och ange en
    tidsfrist inom vilken situationen ska rättas till. Om saken inte rättas till inom tidsfristen ska ARA ge den som
    saken gäller en varning och ange en ny tidsfrist. Om situationen inte rättas till inom tidsfristen ska ARA enligt
    vad situationen kräver meddela beslut om föreläggande eller förbud. Vid allvarliga eller väsentliga överträdelser
    kan ARA förena föreläggandet eller förbudet med vite eller med hot om tvångsutförande eller hot om avbrytande, om
    vilka det bestäms i viteslagen (1113/1990).</p>

<p>ARA har skickat en uppmaning till {{#omistaja-henkilo}}{{sukunimi}}{{/omistaja-henkilo}}{{#omistaja-yritys}}{{nimi}}{{/omistaja-yritys}}, eftersom den byggnaden som ägs av {{#omistaja-henkilo}}
    {{sukunimi}}
    {{/omistaja-henkilo}}
    {{#omistaja-yritys}}
    {{nimi}}
    {{/omistaja-yritys}} har marknadsförts på {{#kohde}}{{ilmoituspaikka}}{{/kohde}} utan energicertifikat. I uppmaningen har
    {{#omistaja-henkilo}}
    {{sukunimi}}
    {{/omistaja-henkilo}}
    {{#omistaja-yritys}}
    {{nimi}}
    {{/omistaja-yritys} ombetts att senast {{#aiemmat-toimenpiteet}}{{kehotus-maarapaiva}}{{/aiemmat-toimenpiteet}} ge
    ARA information om ett eventuellt energicertifikat eller, om ett sådant inte finns, tidtabellen för upprättandet av
    certifikatet.</p>

{{#aiemmat-toimenpiteet}}
<p>Marknadsföringen av objektet fortsatte dock trots uppmaning och {{#omistaja-henkilo}}
    {{sukunimi}}
    {{/omistaja-henkilo}}
    {{#omistaja-yritys}}
    {{nimi}}
    {{/omistaja-yritys}} uppvisade inget energicertifikat för ARA. Därför vidtog ARA åtgärder enligt 24 § i lagen om
    energicertifikat och varnade {{#omistaja-henkilo}}
    {{sukunimi}}
    {{/omistaja-henkilo}}
    {{#omistaja-yritys}}
    {{nimi}}
    {{/omistaja-yritys}} att visa upp energicertifikatet senast {{varoitus-maarapaiva}}.
    Situationen korrigerades inte trots uppmaning och varning, och av ARAs energicertifikatregister framgår att det
    fortfarande inte har upprättats något energicertifikat för adressen
    <sup>2</sup>.</p>
{{/aiemmat-toimenpiteet}}

<p>ARA har alltså gett {{#omistaja-henkilo}}
    {{sukunimi}}
    {{/omistaja-henkilo}}
    {{#omistaja-yritys}}
    {{nimi}}
    {{/omistaja-yritys}} många möjligheter att korrigera sin verksamhet. ARA kan som sista utväg i enlighet
    med 24 § i lagen om energicertifikat meddela ett beslut genom vilket byggnadens ägare åläggs att skaffa ett
    energicertifikat. Ordern kan förenas med vite enligt viteslagen. </p>

{{#tyyppikohtaiset-tiedot}}
{{#recipient-answered}}
<h3>Ställningstagande till bemötandet</h3>

<p class="respect-new-lines">{{statement-sv}}</p>
{{/recipient-answered}}
{{/tyyppikohtaiset-tiedot}}

<h2>Tillämpliga rättsnormer</h2>

<ul>
    <li class="no-indicator">2 §, 6 § och 24 § i lagen om energicertifikat för byggnader (50/2013)</li>
    <li class="no-indicator">4 §, 6 §, 8 § och 22 § i viteslagen (1113/1990)</li>
    <li class="no-indicator">34 § i förvaltningslagen (434/2003)</li>
</ul>

<h2>Ändringssökande</h2>

<p> Den som är missnöjd med detta beslut kan söka ändring i det hos förvaltningsdomstolen i
    {{#tyyppikohtaiset-tiedot}}{{oikeus-sv}}{{/tyyppikohtaiset-tiedot}}. Besvär måste göras och levereras till ovan nämnda
    förvaltningsdomstol inom 30 dagar från delgivningen av det här beslutet.</p>

<div class="footnote">
    <p class="mb-0"><sup>2</sup> I ARAs energicertifikatregister finns i regel alla energicertifikat som upprättats
        efter 1 maj 2015.</p>
</div>

<p>Noggrannare anvisningar om inlämning av besvär finns med som bilaga till det här beslutet.</p>

<div class="page-break-avoid">
{{#valvoja}}
<p>Mer information om besslutet ges av energiexpert {{etunimi}} {{sukunimi}}, <br/>tfn {{puhelin}} / e-post
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

<p class="small-text"><i>Handlingen har undertecknats elektroniskt i ärendehanteringssystemet.</i></p>
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
