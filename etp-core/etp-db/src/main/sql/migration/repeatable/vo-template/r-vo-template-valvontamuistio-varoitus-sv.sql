insert into vo_template (id, label_fi, label_sv, ordinal, toimenpidetype_id, language, content)
values (12, 'Valvontamuistio / varoitus (sv)', 'Övervaknings-pm / varning (sv)', 2, 10, 'sv',
$$
<div class="otsikko">
    <b>VARNING</b> <br />
    <b>{{päivä}}</b> <br />
    {{diaarinumero}}
</div>

<p class="oikeellisuus-kohde">
    <span class="isot-kirjaimet">{{#laatija}} {{etunimi}} {{sukunimi}} {{/laatija}}</span>

    {{#energiatodistus}}
    <table class="sarake">
        <tr>
            <td>Objekt:</td>
            <td>
                <div class="nowrap">{{nimi}}</div>
                <div class="nowrap">{{katuosoite}}</div>
                <div class="nowrap">{{postinumero}} {{postitoimipaikka}}</div>
            </td>
        </tr>
    </table>
    Certifikatets beteckning: {{tunnus}} <br />
    Datum för övervaknings-PM: {{#valvontamuistio}} {{valvontamuistio-pvm}} {{/valvontamuistio}} <br />
    Datum för uppmaning: {{#valvontamuistio}} {{valvontamuistio-kehotus-pvm}} {{/valvontamuistio}}
    {{/energiatodistus}}
</p>

<p>Finansierings- och utvecklingscentralen för boendet (ARA) har till uppgift att övervaka riktigheten hos
    energicertifikat. Riktighetskontrollerna rör energicertifikatens utgångsuppgifter, beräkningen av
    energieffektivitetstal och riktigheten hos besparingsrekommendationer.</p>

<p>ARA har sänt er ett övervaknings-PM om detta energicertifikat. ARA har i övervaknings-PM:et konstaterat att
    energicertifikatet kan betydande felaktigheter. <b>ARA tilldelar er en varning och kräver att ni korrigerar
    energicertifikatet senast {{määräpäivä}}.</b></p>


<p>ARA har rätt att få de uppgifter och dokument som behövs för övervakning, inklusive uppgifter om uppdraget.
    Upprättaren ska bevara beredningshandlingarna, beräkningarna och övriga uppgifter som han eller hon har utarbetat
    eller skaffat för upprättandet av energicertifikat samt uppgifterna om observationer som gjorts på det objekt som
    certifikatet gäller. Upprättaren ska ha ett arkiv över certifikaten. Handlingarna, uppgifterna och certifikaten ska
    bevaras i minst 12 år.</p>

<p>Om upprättaren av energicertifikatet inte uppfyller de reglerade skyldigheterna uppmanar ARA upprättaren att
    korrigera saken och ställer upp en tidsfrist för korrigeringen. Om saken inte korrigeras inom tidsfristen tilldelar
    ARA upprättaren en varning och en ny tidsfrist. Om saken alltjämt inte korrigeras utfärdar ARA ett förbud att
    använda certifikatet och förpliktar upprättaren av energicertifikatet att ersätta det felaktiga certifikatet med ett
    nytt. Energicertifikatet kan vid behov också låta upprättas av en annan upprättare av energicertifikat. Den som
    upprättat det felaktiga certifikatet svarar för kostnaderna för det nya certifikatet.</p>

<p>ARA kan också ge upprättaren ett förbud om upprättande, om upprättaren har agerat i strid med bestämmelserna på ett
    väsentligt eller betydande sätt.</p>

<p>
    {{#valvoja}}
    {{etunimi}} {{sukunimi}}
    {{/valvoja}}<br />
    energiexpert
</p>

<table class="sarake max-width">
    <tr>
        <td><b>Tillämpade förordningar:</b></td>
        <td>Lag om energicertifikat för byggnader (50/2013)</td>
    </tr>
    <tr>
        <td><b>Mer information:</b></td>
        <td><a href="https://www.energiatodistusrekisteri.fi">www.energiatodistusrekisteri.fi</a></td>
    </tr>
</table>
$$)
on conflict (id) do update set
  label_fi = excluded.label_fi,
  label_sv = excluded.label_sv,
  ordinal = excluded.ordinal,
  toimenpidetype_id = excluded.toimenpidetype_id,
  language = excluded.language,
  content = excluded.content;