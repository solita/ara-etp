insert into vo_template (id, label_fi, label_sv, ordinal, toimenpidetype_id, language, content)
values (10, 'Valvontamuistio (sv)', 'Övervaknings-pm (sv)', 2, 7, 'sv',
$$
<div class="otsikko">
    <b>ÖVERVAKNINGS-PM</b> <br />
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
    {{/energiatodistus}}
</p>


<p>Finansierings- och utvecklingscentralen för boendet (ARA) har med stöd av 18 § i lagen om energicertifikat (50/2013)
    kontrollerat det energicertifikat som ni har upprättat. Riktighetskontrollen rörde energicertifikatens
    utgångsuppgifter, beräkningen av energieffektivitetstal och riktigheten hos besparingsrekommendationer. På basis av
    kontrollen fäste man uppmärksamhet vid följande saker:</p>

<ul>
    {{#valvontamuistio}}
        {{#virheet}}
           <li>{{&description}}</li>
        {{/virheet}}
    {{/valvontamuistio}}
</ul>

{{#valvontamuistio}}
    {{#vakavuus}}
        {{#ei-huomioitavaa}}
            <p><b>ARA konstaterar att det inte observerats några anmärkningar.</b></p>
        {{/ei-huomioitavaa}}
        {{#ei-toimenpiteitä}}
            <p><b>ARA konstaterar att det inte observerats några andra anmärkningar än de som nämns. Observationerna kräver inga
                    åtgärder av den som upprättat certifikatet.</b></p>
        {{/ei-toimenpiteitä}}
        {{#virheellinen}}
            <p><b>ARA konstaterar att certifikatet på basis av följande uppräknade grunder kan ha betydande felaktigheter, och
                    certifikatets innehåll har inte blivit genomgått i övrigt.</b> ARA ber er att presentera era egna grunder för de
                observationer som gjorts i kontrollen och skicka dem till ARA:s energicertifikatsregister senast {{määräpäivä}}.</p>
        {{/virheellinen}}
    {{/vakavuus}}
{{/valvontamuistio}}


<p>Om upprättaren av energicertifikatet inte uppfyller de reglerade skyldigheterna uppmanar ARA upprättaren att
    korrigera saken och ställer upp en tidsfrist för korrigeringen. Om saken inte korrigeras inom tidsfristen tilldelar
    ARA upprättaren en varning och en ny tidsfrist. Om saken alltjämt inte korrigeras utfärdar ARA ett förbud att
    använda certifikatet och förpliktar upprättaren av energicertifikatet att ersätta det felaktiga certifikatet med ett
    nytt. Energicertifikatet kan vid behov också låta upprättas av en annan upprättare av energicertifikat. Den som
    upprättat det felaktiga certifikatet svarar för kostnaderna för det nya certifikatet. </p>

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
        <td><b>För kännedom:</b></td>
        <td>
            <div>{{#tiedoksi}}{{nimi}} {{email}}<br />{{/tiedoksi}}</div>
        </td>
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