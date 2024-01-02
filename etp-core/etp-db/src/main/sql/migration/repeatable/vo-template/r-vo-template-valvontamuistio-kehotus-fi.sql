insert into vo_template (id, label_fi, label_sv, ordinal, toimenpidetype_id, language, content)
values (5, 'Valvontamuistio / kehotus (fi)', 'Övervaknings-pm / uppmaning (fi)', 1, 9, 'fi', 
$$
<div class="otsikko">
    <b>KEHOTUS</b> <br/>
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
    Valvontamuistion päivämäärä: {{#valvontamuistio}} {{valvontamuistio-pvm}} {{/valvontamuistio}}
    {{/energiatodistus}}
</p>


<p>Asumisen rahoitus- ja kehittämiskeskuksen (ARA) tehtävänä on valvoa energiatodistusten oikeellisuutta.
    Oikeellisuustarkastukset kohdistuvat energiatodistusten lähtötietoihin, energiatehokkuusluvun laskentaan sekä
    säästösuositusten oikeellisuuteen.</p>

<p>ARA on lähettänyt teille tästä energiatodistuksesta valvontamuistion. ARA on valvontamuistiossa todennut, että
    energiatodistus voi olla olennaisesti virheellinen. <b>ARA kehottaa teitä korjaamaan energiatodistuksen {{määräpäivä}}
    mennessä.</b></p>


<p>ARAlla on oikeus saada valvontaa varten tarvittavat tiedot ja asiakirjat, mukaan lukien toimeksiantoja koskevat
    tiedot. Laatijan on säilytettävä valmisteluasiakirjat, laskelmat ja muut tiedot, jotka laatija on tehnyt tai
    hankkinut todistuksen laatimista varten sekä tiedot todistuksen kohteessa tehdystä havainnoinnista. Laatijan on
    pidettävä arkistoa laatimistaan todistuksista. Asiakirjat, tiedot ja todistukset on säilytettävä vähintään 12
    vuotta.</p>

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