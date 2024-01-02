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
values (1, 'Kehotus 2021', 'Uppmaning 2021', 1, 2, 'fi', false, false,
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
    Tietopyynnön päivämäärä/Begäran om uppgifter: {{#tietopyynto}} {{tietopyynto-pvm}} {{/tietopyynto}}
</div>

<h1>Energiatodistusvalvonnan kehotus</h1>

<p>Asumisen rahoitus- ja kehittämiskeskuksen (ARA) tehtävänä on valvoa energiatodistusten käyttämistä myynti- ja
    vuokraustilanteissa. ARA on lähettänyt teille tietopyynnön liittyen rakennuksen/asunnon markkinointiin ilman
    energiatodistusta. Mikäli kohteen julkista markkinointia jatketaan tai se myydään, <b>ARA kehottaa esittämään
        energiatodistuksen {{määräpäivä}} mennessä sähköpostitse energiatodistus@ara.fi tai postitse.</b></p>

<p>Rakennusta, huoneistoa tai niiden hallintaoikeutta myytäessä tai vuokrattaessa täytyy olla energiatodistus.
    Energiatodistus on annettava joko alkuperäisenä tai jäljennöksenä ostajalle tai vuokralaiselle. Myynti- tai
    vuokrausilmoituksessa on oltava myytävän kohteen energiatehokkuusluokka. Rakennuksen omistaja tai haltija vastaa
    siitä, että rakennuksen energiatodistus hankitaan ja sitä käytetään laissa säädetyissä tilanteissa. </p>

<p>Jos rakennuksen omistaja ei täytä säädettyjä velvollisuuksia, ARA kehottaa korjaamaan asian ja antaa määräajan
    korjaukselle. Jos asiaa ei korjata määräajassa, ARA antaa rakennuksen omistajalle varoituksen ja uuden määräajan.
    Jos asiaa ei edelleenkään korjata, ARA voi antaa omistajaa velvoittavan käskypäätöksen, jota voidaan tehostaa
    uhkasakolla.</p>

<div class="sivunvaihto"></div>

<h1>Uppmaning</h1>

<p>Finansierings- och utvecklingscentralen för boendet (ARA) har till uppgift att övervaka användningen av
    energicertifikat vid försäljning och uthyrning. ARA har sänt er en begäran om information rörande marknadsföring av
    byggnad/bostad utan energicertifikat. Om den offentliga marknadsföringen av objektet fortsätter eller om det säljs
    <b>uppmanar ARA er att skicka in ett energicertifikat senast {{määräpäivä}} per e-post till energiatodistus@ara.fi eller
        per post.</b></p>

<p>Vid försäljning eller uthyrning av en byggnad eller lägenhet eller besittningsrätten till dem måste det finnas ett
    energicertifikat. Energicertifikatet ska överlämnas till köparen eller hyrestagaren antingen i original eller som
    kopia. Meddelandet om försäljning eller uthyrning ska innehålla objektets energieffektivitetsklass. Byggnadens ägare
    eller innehavare ansvarar för att byggnadens energicertifikat skaffas och används i de situationer som regleras i
    lagen. </p>

<p>Om byggnadens ägare inte uppfyller de reglerade skyldigheterna uppmanar ARA ägaren att korrigera saken och ställer
    upp en tidsfrist för korrigeringen. Om saken inte korrigeras inom tidsfristen ger ARA byggnadens ägare en varning
    och en ny tidsfrist. Om saken alltjämt inte korrigeras kan ARA ge ägaren ett förpliktande beslut om order, som kan
    förenas med ett vite. </p>

<p>
    {{#valvoja}}
    {{etunimi}} {{sukunimi}}
    {{/valvoja}}<br/>
    energia-asiantuntija/energiexpert
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
