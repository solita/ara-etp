insert into vo_template (id, label_fi, label_sv, ordinal, toimenpidetype_id, language, content)
values (3, 'Tietopyyntö / varoitus (fi)', 'Begäran om inlämning / varning (fi)', 1, 6, 'fi', 
$$
<div class="otsikko">
    <b>VAROITUS</b> <br/>
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
    Tietopyynnön päivämäärä: {{#tietopyynto}} {{tietopyynto-pvm}} {{/tietopyynto}} <br />
    Kehotuksen päivämäärä: {{#tietopyynto}} {{tietopyynto-kehotus-pvm}} {{/tietopyynto}}
    {{/energiatodistus}}
</p>


<p>Asumisen rahoitus- ja kehittämiskeskuksen (ARA) tehtävänä on valvoa energiatodistusten oikeellisuutta.
    Oikeellisuustarkastukset kohdistuvat energiatodistusten lähtötietoihin, energiatehokkuusluvun laskentaan sekä
    säästösuositusten oikeellisuuteen.</p>

<p>ARA on lähettänyt teille tästä energiatodistuksesta taustamateriaalin tietopyynnön ja kehotuksen. 
    <b>ARA tulee tarkastamaan todistuksen oikeellisuuden tämän materiaalin pohjalta.</b> Pyydämme, että toimitatte
    seuraavat todistuksen laadinnassa käytetyt taustamateriaalit ARAn energiatodistusrekisteriin {{määräpäivä}}
    mennessä:</p>

<ul>
    <li>Pääpiirustukset (asema-, pohja-, julkisivu- ja leikkauspiirustukset sekä U-arvot)</li>
    <li>E-lukulaskentaan vaikuttavat ilmanvaihto-, jäähdytys- ja lämmitysjärjestelmien laskelmat ja tekniset tiedot (ei
        pohjakuvia)
    </li>
    <li>Valaistuslaskelmat, jos E-lukulaskennassa on käytetty tarpeenmukaista valaistusta</li>
    <li>Tiiveysmittausraportti, jos mittaus on suoritettu</li>
    <li>Energiaselvitys (uudiskohteet)</li>
    <li>Havainnointipöytäkirja ja muu materiaali paikan päällä käynnistä (olemassa olevat rakennukset)</li>
</ul>

<p>ARAlla on oikeus saada valvontaa varten tarvittavat tiedot ja asiakirjat, mukaan lukien toimeksiantoja koskevat
    tiedot. Laatijan on säilytettävä valmisteluasiakirjat, laskelmat ja muut tiedot, jotka laatija on tehnyt tai
    hankkinut todistuksen laatimista varten sekä tiedot todistuksen kohteessa tehdystä havainnoinnista. Laatijan on
    pidettävä arkistoa laatimistaan todistuksista. Asiakirjat, tiedot ja todistukset on säilytettävä vähintään 12
    vuotta.</p>

<p>Jos energiatodistuksen laatija ei täytä säädettyjä velvollisuuksia, ARA kehottaa korjaamaan asian ja antaa määräajan
    korjaukselle. Jos asiaa ei korjata määräajassa, ARA antaa laatijalle varoituksen ja uuden määräajan. Jos asiaa ei
    edelleenkään korjata, ARA voi antaa laatijalle velvoittavan käskypäätöksen/käyttökieltopäätöksen, jota voidaan
    tehostaa uhkasakolla. ARAlla on myös mahdollisuus antaa laatijalle laatimiskielto, jos laatija on toiminut
    olennaisella tai merkittävällä tavalla säännösten vastaisesti.</p>

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