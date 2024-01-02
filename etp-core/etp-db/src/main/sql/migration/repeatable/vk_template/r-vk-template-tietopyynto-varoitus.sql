insert into vk_template (
    id,
    label_fi,
    label_sv,
    ordinal,
    toimenpidetype_id,
    language,
    valid,
    tiedoksi,
    content
)
values (4, 'Varoitus', 'Varning', 1, 3, 'fi', true, false,
$$
<div class="otsikko">
    <b>VAROITUS/VARNING</b> <br/>
    <b>{{päivä}}</b> <br/>
    {{diaarinumero}}
</div>

<div class="kaytto-omistaja">
    {{#omistaja-henkilo}}
        {{etunimi}} {{sukunimi}}
    {{/omistaja-henkilo}}
    {{#omistaja-yritys}}
        {{nimi}}
    {{/omistaja-yritys}}
</div>
<div class="kaytto-kohde">
    {{#kohde}}
        <table class="sarake">
            <tr>
                <td>Kohde/Objekt: </td> 
                <td>
                    <div class="nowrap">{{katuosoite}}</div>  
                    <div class="nowrap">{{postinumero}} {{postitoimipaikka}}</div>  
                </td>
            </tr>
        </table>
        Ilmoituspaikka/Meddelandeplats: {{ilmoituspaikka}} <br/>
        Ilmoitustunnus/Meddelandekod: {{ilmoitustunnus}} <br/>
        Havaintopäivä/Observationsdatum: {{havaintopäivä}} <br/>
    {{/kohde}}
    Kehotuksen päivämäärä/Uppmaningsdatum: {{#tietopyynto}} {{tietopyynto-kehotus-pvm}} {{/tietopyynto}}
</div>

<h1>Energiatodistusvalvonnan varoitus</h1>

<p>
  Asumisen rahoitus- ja kehittämiskeskuksen (ARA) tehtävänä on valvoa energiatodistusten käyttämistä myynti-
  ja vuokraustilanteissa. ARA on lähettänyt teille kehotuksen liittyen rakennuksen/asunnon markkinointiin
  ilman energiatodistusta. <b>ARA antaa Teille varoituksen.</b> Mikäli kohteen julkista markkinointia
  jatketaan tai se myydään, <b>ARA pyytää esittämään energiatodistuksen {{määräpäivä}} mennessä sähköpostitse
  energiatodistus@ara.fi tai postitse.</b>
</p>

<p>
  Rakennusta, huoneistoa tai niiden hallintaoikeutta myytäessä tai vuokrattaessa täytyy olla
  energiatodistus. Energiatodistus on annettava joko alkuperäisenä tai jäljennöksenä ostajalle tai
  vuokralaiselle. Myynti- tai vuokrausilmoituksessa on oltava myytävän kohteen
  energiatehokkuusluokka. Rakennuksen omistaja tai haltija vastaa siitä, että rakennuksen energiatodistus
  hankitaan ja sitä käytetään laissa säädetyissä tilanteissa.
</p>

<p>
  Jos energiatodistusta ei hankita ja esitetä ARAlle tässä varoituksessa asetetussa määräajassa, ARA antaa
  tilanteen mukaisen käskypäätöksen. Käskypäätöstä tehostetaan uhkasakolla.
</p>

<div class="sivunvaihto"></div>

<h1>Varning från tillsyn av energicertifikat</h1>

<p>
  Finansierings- och utvecklingscentralen för boendet (ARA) har till uppgift att övervaka användningen av
  energicertifikat vid försäljning och uthyrning. ARA har skickat er uppmaning angående marknadsföring av en
  byggnad/bostad utan energicertifikat. <b>ARA tilldelar er en varning.</b> Om den offentliga marknadsföringen
  av objektet fortsätter eller om det säljs <b>ber ARA er att skicka in ett energicertifikat senast
  {{määräpäivä}} per e-post till energiatodistus@ara.fi eller per post.</b>
</p>

<p>
  Vid försäljning eller uthyrning av en byggnad eller lägenhet eller besittningsrätten till dem måste det
  finnas ett energicertifikat. Energicertifikatet ska överlämnas till köparen eller hyrestagaren antingen i
  original eller som kopia. Meddelandet om försäljning eller uthyrning ska innehålla objektets
  energieffektivitetsklass. Byggnadens ägare eller innehavare ansvarar för att byggnadens energicertifikat
  skaffas och används i de situationer som regleras i lagen.
</p>

<p>
  Om energicertifikat inte skaffas och framvisas till ARA inom tidsfristen i denna varning tilldelar ARA ett
  lämpligt beslut om föreläggande. Beslutet om föreläggande kan förenas med ett vite.
</p>

<p>
    {{#valvoja}}
    {{etunimi}} {{sukunimi}}
    {{/valvoja}}<br/>
    energia-asiantuntija/energiexpert, ARA
</p>

<table class="sarake max-width">
    <tr>
        <td><b>Sovelletut säännökset:</b></td>
        <td>Laki rakennuksen energiatodistuksesta (50/2013)</td>
    </tr>
    <tr>
        <td><b>Tillämpade förordningar: </b></td>
        <td>Lagen om energicertifikat för byggnader (50/2013)</td>
    </tr>
    <tr>
        <td><b>Tiedoksi/För kännedom:</b></td>
        <td>
            <div>{{#tiedoksi}}{{.}}<br/>{{/tiedoksi}}</div>
        </td>
    </tr>
</table>
$$)
on conflict (id) do update set
  label_fi = excluded.label_fi,
  label_sv = excluded.label_sv,
  ordinal = excluded.ordinal,
  toimenpidetype_id = excluded.toimenpidetype_id,
  language = excluded.language,
  valid = excluded.valid,
  tiedoksi = excluded.tiedoksi,
  content = excluded.content;
