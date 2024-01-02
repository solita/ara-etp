insert into vo_template (id, label_fi, label_sv, ordinal, toimenpidetype_id, language, content)
values (4, 'Valvontamuistio (fi)', 'Övervaknings-pm (fi)', 1, 7, 'fi', 
$$
<div class="otsikko">
    <b>VALVONTAMUISTIO</b> <br/>
    <b>{{päivä}}</b> <br/>
    {{diaarinumero}}
</div>

<p class="oikeellisuus-kohde">
    <span class="isot-kirjaimet">{{#laatija}} {{etunimi}} {{sukunimi}} {{/laatija}}</span>

    {{#energiatodistus}}
    <table class="sarake">
        <tr>
            <td>Kohde:</td> 
            <td>
                <div class="nowrap">{{nimi}}</div>
                <div class="nowrap">{{katuosoite}}</div>  
                <div class="nowrap">{{postinumero}} {{postitoimipaikka}}</div>  
            </td>
        </tr>
    </table>
    Todistustunnus: {{tunnus}} <br/>
    {{/energiatodistus}}
</p>

<p>Asumisen rahoitus- ja kehittämiskeskus (ARA) on energiatodistuslain (50/2013) <span class="nowrap">18 §:n</span>
    nojalla tarkastanut laatimanne energiatodistuksen. Oikeellisuustarkastus on kohdistunut energiatodistuksen
    lähtötietoihin, energiatehokkuusluvun laskentaan sekä säästösuositusten oikeellisuuteen. Tarkastuksen perusteella
    kiinnitettiin huomiota seuraaviin asioihin:</p>

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
            <p><b>ARA toteaa, että energiatodistuksessa ei ole havaittu huomautettavaa.</b></p>
        {{/ei-huomioitavaa}}
        {{#ei-toimenpiteitä}}
            <p><b>ARA toteaa, että energiatodistuksessa ei ole havaittu muuta huomautettavaa kuin edellä mainitut. Havainnot eivät
               edellytä laatijalta toimenpiteitä.</b></p>
        {{/ei-toimenpiteitä}}
        {{#virheellinen}}
          <p><b>ARA toteaa, että energiatodistus voi edellä luetelluin perustein olla olennaisesti virheellinen eikä todistuksen
             sisältöä käyty muilta osin lävitse.</b> ARA pyytää esittämään omat perustelunne tarkastuksessa tehdyistä havainnoista ja
             toimittamaan ne ARAn energiatodistusrekisteriin {{määräpäivä}} mennessä.</p>
        {{/virheellinen}}
    {{/vakavuus}}
{{/valvontamuistio}}


<p>Jos energiatodistuksen laatija ei täytä säädettyjä velvollisuuksia, ARA kehottaa korjaamaan asian ja antaa määräajan
    korjaukselle. Jos asiaa ei korjata määräajassa, ARA antaa laatijalle varoituksen ja uuden määräajan. Jos asiaa ei
    edelleenkään korjata, ARA laittaa todistuksen käyttökieltoon ja velvoittaa energiatodistuksen laatijan korvaamaan
    virheellisen todistuksen uudella todistuksella. Energiatodistus voidaan tarvittaessa teettää myös toisella
    energiatodistuksen laatijalla. Uuden todistuksen kustannuksista vastaa virheellisen todistuksen laatinut
    energiatodistuksen laatija. </p>

<p>ARAlla on myös mahdollisuus antaa laatijalle laatimiskielto, jos laatija on toiminut olennaisella tai merkittävällä
    tavalla säännösten vastaisesti.</p>

<p>
    {{#valvoja}}
    {{etunimi}} {{sukunimi}}
    {{/valvoja}}<br/>
    energia-asiantuntija
</p>

<table class="sarake max-width">
    <tr>
        <td><b>Sovelletut säännökset:</b></td>
        <td>Laki rakennuksen energiatodistuksesta (50/2013)</td>
    </tr>
    <tr>
        <td><b>Tiedoksi:</b></td>
        <td><div>{{#tiedoksi}}{{nimi}} {{email}}<br />{{/tiedoksi}}</div></td>
    </tr>
    <tr>
        <td><b>Lisätietoja:</b></td>
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