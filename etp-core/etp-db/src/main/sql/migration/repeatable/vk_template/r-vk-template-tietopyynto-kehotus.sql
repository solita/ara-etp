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
values (3, 'Kehotus', 'Uppmaning', 1, 2, 'fi', true, true,
$$
<div class="otsikko">
    <b>KEHOTUS/UPPMANING</b> <br/>
    <b>{{päivä}}</b> <br/>
    {{diaarinumero}}
</div>

<div class="kaytto-omistaja">
    {{#omistaja-henkilo}}
        {{etunimi}} {{sukunimi}}
    {{/omistaja-henkilo}}
    {{#omistaja-yritys}}
        {{nimi}}<br />
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
</div>

<h1>Energiatodistusvalvonnan kehotus</h1>

<p>
  Valvontamme perusteella myynnissä tai vuokrattavana olevaa rakennustanne/asuntoanne markkinoidaan julkisesti
  ilman energiatodistusta. Mikäli kohteen julkista markkinointia jatketaan tai se myydään, <b>Varke kehottaa
  esittämään energiatodistuksen {{määräpäivä}} mennessä sähköpostitse osoitteeseen
  <span class="email-address">energiatodistus.varke@gov.fi</span> tai postitse.</b>
</p>

<p>
  Energiatodistus on hankittava ennen kuin kohdetta aletaan markkinoida julkisesti, sillä myynti- tai
  vuokrausilmoituksessa on mainittava kohteen energiatehokkuusluokka. Rakennusta, huoneistoa tai niiden
  hallintaoikeutta myytäessä tai vuokrattaessa täytyy olla energiatodistus. Energiatodistus on annettava joko
  alkuperäisenä tai jäljennöksenä ostajalle tai vuokralaiselle. Rakennuksen omistaja tai haltija vastaa siitä,
  että rakennuksen energiatodistus hankitaan ja sitä käytetään laissa säädetyissä tilanteissa.
</p>

<p>
  Valtion tukeman asuntorakentamisen keskuksen (Varke) tehtävänä on valvoa energiatodistusten käyttämistä myynti-
  ja vuokraustilanteissa. Jos rakennuksen omistaja ei täytä laissa säädettyjä velvollisuuksiaan, Varke kehottaa
  korjaamaan asian ja antaa määräajan korjaukselle. Jos asiaa ei korjata määräajassa, Varke antaa rakennuksen
  omistajalle varoituksen ja uuden määräajan. Jos asiaa ei edelleenkään korjata, Varke antaa omistajaa
  velvoittavan käskypäätöksen, jota voidaan tehostaa uhkasakolla.
</p>

<div class="sivunvaihto"></div>

<h1>Uppmaning till tillsyn över energicertifikat</h1>

<p>
  På basis av vår tillsyn marknadsförs den byggnad/bostad som ni ska sälja eller hyra ut offentligt utan
  energicertifikat. Om den offentliga marknadsföringen av objektet fortsätter eller om det säljs <b>uppmanar
  Varke er att skicka in ett energicertifikat senast den {{määräpäivä}} per e-post till
  <span class="email-address">energiatodistus.varke@gov.fi</span> eller per post.</b>
</p>

<p>
  Ett energicertifikat ska skaffas innan objektet börjar marknadsföras offentligt, eftersom objektets
  energieffektivitetsklass ska nämnas i försäljnings- eller hyresannonsen. Vid försäljning eller uthyrning av
  en byggnad eller lägenhet eller besittningsrätten till dem måste det finnas ett
  energicertifikat. Energicertifikatet ska överlämnas till köparen eller hyrestagaren antingen i original
  eller som kopia. Byggnadens ägare eller innehavare ansvarar för att byggnadens energicertifikat skaffas och
  används i de situationer som regleras i lagen.
</p>

<p>
  Centralen för statligt stött bostadsbyggande (Varke) har till uppgift att övervaka användningen av
  energicertifikat vid försäljning och uthyrning. Om byggnadens ägare inte uppfyller sin lagstadgade
  skyldigheter uppmanar Varke ägaren att korrigera saken och ställer upp en tidsfrist för korrigeringen. Om
  saken inte korrigeras inom tidsfristen ger Varke byggnadens ägare en varning och en ny tidsfrist. Om saken
  alltjämt inte korrigeras ger Varke ägaren ett förpliktande beslut om order, som kan förenas med ett vite.
</p>

<p>
    {{#valvoja}}
    {{etunimi}} {{sukunimi}}
    <br/>
    {{titteli-fi}}/{{titteli-sv}}{{/valvoja}}, Varke
</p>

<table class="sarake max-width">
    <tbody>
        <tr>
            <td><b>Sovelletut säännökset:</b></td>
            <td>Laki rakennuksen energiatodistuksesta (50/2013)</td>
        </tr>
        <tr>
            <td><b>Tillämpade förordningar: </b></td>
            <td>Lagen om energicertifikat för byggnader (50/2013)</td>
        </tr>
    </tbody>
    <tbody class="page-break-avoid">
        <tr>
            <td colspan="2"><b>Tiedoksi/För kännedom:</b></td>
        </tr>
        <tr>
          <td colspan="2">
            <ul class="mt-0">
              {{#tiedoksi}}
                <li class="mt-0 slim">
                {{#rooli}}<span class="list-item">{{.}}</span>{{/rooli}}
                <span class="list-item nowrap">{{nimi}}</span>
                {{#email}}<span class="list-item nowrap">{{.}}</span>{{/email}}
                </li>
              {{/tiedoksi}}
            </ul>
          </td>
        </tr>
    </tbody>
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
