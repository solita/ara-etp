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
  Valtion tukeman asuntorakentamisen keskuksen (Varke) tehtävänä on valvoa energiatodistusten käyttämistä myynti- ja
  vuokraustilanteissa. Valvontamme perusteella myynnissä tai vuokrattavana olevaa rakennustanne/asuntoanne markkinoidaan
  julkisesti ilman energiatodistusta. <b>Mikäli kohteen julkista markkinointia jatketaan tai kohde myydään, tulee
  energiatodistus hankkia {{määräpäivä}} mennessä.</b>
</p>

<p>
  Rakennuksen omistajan on huolehdittava, että rakennuksen energiatodistus hankitaan ja sitä käytetään laissa
  säädetyissä tilanteissa. Rakennusta, huoneistoa tai niiden hallintaoikeutta myytäessä tai vuokrattaessa täytyy olla
  energiatodistus.  Energiatodistus on hankittava ennen kohteen julkista markkinointia, koska myynti- ja
  vuokrailmoituksessa on ilmoitettava energiatehokkuutta koskevat tiedot.
</p>

<p>
  Jos rakennuksen omistaja ei täytä laissa säädettyjä velvollisuuksiaan, Varke kehottaa korjaamaan asian ja antaa
  määräajan korjaukselle. Jos asiaa ei korjata määräajassa, Varke antaa rakennuksen omistajalle varoituksen ja uuden
  määräajan. Jos asiaa ei edelleenkään korjata, Varke antaa omistajaa velvoittavan käskypäätöksen, jota voidaan tehostaa
  uhkasakolla.
</p>

<div class="sivunvaihto"></div>

<h1>Uppmaning till tillsyn över energicertifikat</h1>

<p>
  Uppmaning till tillsyn över energicertifikat Centralen för statligt stött bostadsbyggande (Varke) har till uppgift att
  övervaka användningen av energicertifikat vid försäljning och uthyrning. På basis av vår tillsyn marknadsförs den
  byggnad/bostad som ni ska sälja eller hyra ut offentligt utan energicertifikat. <b>Om den offentliga marknadsföringen
  av objektet fortsätter eller om objektet säljs, ska ett energicertifikat skaffas senast {{määräpäivä}}.</b>
</p>

<p>
  Byggnadens ägare ansvarar för att byggnadens energicertifikat skaffas och används i de situationer som regleras i
  lagen. Vid försäljning eller uthyrning av en byggnad eller lägenhet eller besittningsrätten till dem måste det finnas
  ett energicertifikat.  Ett energicertifikat ska skaffas innan objektet börjar marknadsföras offentligt, eftersom
  uppgifter om objektets energieffektivitet ska anges i försäljnings- och hyresannonser.
</p>

<p>
  Om byggnadens ägare inte uppfyller sina lagstadgade skyldigheter uppmanar Varke ägaren att korrigera saken och ställer
  upp en tidsfrist för korrigeringen. Om saken inte korrigeras inom tidsfristen ger Varke byggnadens ägare en varning
  och en ny tidsfrist. Om saken alltjämt inte korrigeras ger Varke ägaren ett förpliktande order, som kan förenas med
  ett vite.
</p>

<p>
  Lisätietoja/Mer information: energiatodistus.varke@gov.fi
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
