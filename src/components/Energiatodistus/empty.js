import * as R from 'ramda';

import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';
import * as EtUtils from './energiatodistus-utils';

const ValidNone = R.compose(Either.Right, Maybe.None);

const emptyRakennusVaippa = _ => ({
  ala: ValidNone(),
  U: ValidNone()
});

const emptyIkkuna = _ => ({
  ala: ValidNone(),
  U: ValidNone(),
  'g-ks': ValidNone()
});

const emptyIV = _ => ({
  poisto: ValidNone(),
  tulo: ValidNone(),
  sfp: ValidNone()
});

const emptyLammitys = _ => ({
  'tuoton-hyotysuhde': ValidNone(),
  'jaon-hyotysuhde': ValidNone(),
  lampokerroin: ValidNone(),
  apulaitteet: ValidNone(),
  'lampohavio-lammittamaton-tila': ValidNone(),
  'lampopumppu-tuotto-osuus': ValidNone()
});

const emptyLammitysMaaraTuotto = _ => ({
  maara: ValidNone(),
  tuotto: ValidNone()
});

const emptyToimenpide = _ => ({
  'nimi-fi': Maybe.None(),
  'nimi-sv': Maybe.None(),
  lampo: ValidNone(),
  sahko: ValidNone(),
  jaahdytys: ValidNone(),
  'eluvun-muutos': ValidNone()
});

const emptyHuomio = _ => ({
  'teksti-fi': Maybe.None(),
  'teksti-sv': Maybe.None(),
  toimenpide: [emptyToimenpide(), emptyToimenpide(), emptyToimenpide()]
});

const emptySahkoLampo = _ => ({
  sahko: ValidNone(),
  lampo: ValidNone()
});

const emptySisKuorma = _ => ({
  kayttoaste: ValidNone(),
  lampokuorma: ValidNone()
});

const emptyMuuPolttoaine = _ => ({
  nimi: Maybe.None(),
  yksikko: Maybe.None(),
  muunnoskerroin: ValidNone(),
  'maara-vuodessa': ValidNone()
});

const formalDescription = _ => ({
  id: Maybe.None(),
  'kuvaus-fi': Maybe.None(),
  'kuvaus-sv': Maybe.None()
});

export const energiatodistus2018 = _ => ({
  versio: 2018,
  'tila-id': EtUtils.tila.draft,
  'laatija-id': Maybe.None(),
  'laskutettava-yritys-id': Maybe.None(),
  'korvaava-energiatodistus-id': Maybe.None(),
  'korvattu-energiatodistus-id': Maybe.None(),
  'draft-visible-to-paakayttaja': false,
  'bypass-validation-limits': false,
  'bypass-validation-limits-reason': Maybe.None(),
  laskuriviviite: Maybe.None(),
  laskutusaika: Maybe.None(),
  perustiedot: {
    nimi: Maybe.None(),
    rakennustunnus: Maybe.None(),
    kiinteistotunnus: Maybe.None(),
    kieli: Maybe.None(),
    rakennusosa: Maybe.None(),
    'katuosoite-fi': Maybe.None(),
    'katuosoite-sv': Maybe.None(),
    postinumero: Maybe.None(),
    valmistumisvuosi: ValidNone(),
    'julkinen-rakennus': false,
    tilaaja: Maybe.None(),
    yritys: {
      nimi: Maybe.None(),
      katuosoite: Maybe.None(),
      postinumero: Maybe.None(),
      postitoimipaikka: Maybe.None()
    },
    kayttotarkoitus: Maybe.None(),
    laatimisvaihe: Maybe.None(),
    havainnointikaynti: Maybe.None(),
    'keskeiset-suositukset-fi': Maybe.None(),
    'keskeiset-suositukset-sv': Maybe.None()
  },
  lahtotiedot: {
    'lammitetty-nettoala': ValidNone(),
    rakennusvaippa: {
      ilmanvuotoluku: ValidNone(),
      lampokapasiteetti: ValidNone(),
      ilmatilavuus: ValidNone(),
      'kylmasillat-UA': ValidNone(),
      ulkoseinat: emptyRakennusVaippa(),
      ylapohja: emptyRakennusVaippa(),
      alapohja: emptyRakennusVaippa(),
      ikkunat: emptyRakennusVaippa(),
      ulkoovet: emptyRakennusVaippa()
    },
    ikkunat: {
      pohjoinen: emptyIkkuna(),
      koillinen: emptyIkkuna(),
      ita: emptyIkkuna(),
      kaakko: emptyIkkuna(),
      etela: emptyIkkuna(),
      lounas: emptyIkkuna(),
      lansi: emptyIkkuna(),
      luode: emptyIkkuna(),
      valokupu: emptyIkkuna(),
      katto: emptyIkkuna()
    },
    ilmanvaihto: {
      erillispoistot: emptyIV(),
      ivjarjestelma: emptyIV(),
      paaiv: R.mergeRight(
        {
          lampotilasuhde: ValidNone(),
          jaatymisenesto: ValidNone()
        },
        emptyIV()
      ),
      'tyyppi-id': Maybe.None(),
      'kuvaus-fi': Maybe.None(),
      'kuvaus-sv': Maybe.None(),
      'lto-vuosihyotysuhde': ValidNone(),
      'tuloilma-lampotila': ValidNone()
    },
    lammitys: {
      'lammitysmuoto-1': formalDescription(),
      'lammitysmuoto-2': formalDescription(),
      lammonjako: formalDescription(),
      'tilat-ja-iv': emptyLammitys(),
      'lammin-kayttovesi': emptyLammitys(),
      takka: emptyLammitysMaaraTuotto(),
      ilmalampopumppu: emptyLammitysMaaraTuotto()
    },
    jaahdytysjarjestelma: {
      'jaahdytyskauden-painotettu-kylmakerroin': ValidNone()
    },
    'lkvn-kaytto': {
      ominaiskulutus: ValidNone(),
      'lammitysenergian-nettotarve': ValidNone()
    },
    'sis-kuorma': {
      henkilot: emptySisKuorma(),
      kuluttajalaitteet: emptySisKuorma(),
      valaistus: emptySisKuorma()
    }
  },
  tulokset: {
    'kaytettavat-energiamuodot': {
      'fossiilinen-polttoaine': ValidNone(),
      sahko: ValidNone(),
      kaukojaahdytys: ValidNone(),
      kaukolampo: ValidNone(),
      'uusiutuva-polttoaine': ValidNone()
    },
    'uusiutuvat-omavaraisenergiat': {
      aurinkosahko: ValidNone(),
      tuulisahko: ValidNone(),
      aurinkolampo: ValidNone(),
      muulampo: ValidNone(),
      muusahko: ValidNone(),
      lampopumppu: ValidNone()
    },
    kuukausierittely: [],
    'tekniset-jarjestelmat': {
      'tilojen-lammitys': emptySahkoLampo(),
      'tuloilman-lammitys': emptySahkoLampo(),
      'kayttoveden-valmistus': emptySahkoLampo(),
      'iv-sahko': ValidNone(),
      jaahdytys: R.assoc('kaukojaahdytys', ValidNone(), emptySahkoLampo()),
      'kuluttajalaitteet-ja-valaistus-sahko': ValidNone()
    },
    nettotarve: {
      'tilojen-lammitys-vuosikulutus': ValidNone(),
      'ilmanvaihdon-lammitys-vuosikulutus': ValidNone(),
      'kayttoveden-valmistus-vuosikulutus': ValidNone(),
      'jaahdytys-vuosikulutus': ValidNone()
    },
    lampokuormat: {
      aurinko: ValidNone(),
      ihmiset: ValidNone(),
      kuluttajalaitteet: ValidNone(),
      valaistus: ValidNone(),
      kvesi: ValidNone()
    },
    laskentatyokalu: Maybe.None()
  },
  'toteutunut-ostoenergiankulutus': {
    'ostettu-energia': {
      'kaukolampo-vuosikulutus': ValidNone(),
      'kokonaissahko-vuosikulutus': ValidNone(),
      'kiinteistosahko-vuosikulutus': ValidNone(),
      'kayttajasahko-vuosikulutus': ValidNone(),
      'kaukojaahdytys-vuosikulutus': ValidNone()
    },
    'ostetut-polttoaineet': {
      'kevyt-polttooljy': ValidNone(),
      'pilkkeet-havu-sekapuu': ValidNone(),
      'pilkkeet-koivu': ValidNone(),
      puupelletit: ValidNone(),
      muu: R.times(emptyMuuPolttoaine, 4)
    },
    'sahko-vuosikulutus-yhteensa': ValidNone(),
    'kaukolampo-vuosikulutus-yhteensa': ValidNone(),
    'polttoaineet-vuosikulutus-yhteensa': ValidNone(),
    'kaukojaahdytys-vuosikulutus-yhteensa': ValidNone()
  },
  huomiot: {
    lammitys: emptyHuomio(),
    'alapohja-ylapohja': emptyHuomio(),
    'iv-ilmastointi': emptyHuomio(),
    ymparys: emptyHuomio(),
    'valaistus-muut': emptyHuomio(),
    'suositukset-fi': Maybe.None(),
    'suositukset-sv': Maybe.None(),
    'lisatietoja-fi': Maybe.None(),
    'lisatietoja-sv': Maybe.None()
  },
  'lisamerkintoja-fi': Maybe.None(),
  'lisamerkintoja-sv': Maybe.None(),
  kommentti: Maybe.None()
});

const emptyMuuEnergiamuoto = _ => ({
  nimi: Maybe.None(),
  muotokerroin: ValidNone(),
  ostoenergia: ValidNone()
});

const emptyMuuEnergia = _ => ({
  'nimi-fi': Maybe.None(),
  'nimi-sv': Maybe.None(),
  vuosikulutus: ValidNone()
});

export const energiatodistus2013 = R.compose(
  R.assocPath(
    ['toteutunut-ostoenergiankulutus', 'ostetut-polttoaineet', 'muu'],
    R.times(emptyMuuPolttoaine, 10)
  ),
  R.assocPath(
    ['toteutunut-ostoenergiankulutus', 'ostettu-energia', 'muu'],
    R.times(emptyMuuEnergia, 6)
  ),
  R.assocPath(
    ['tulokset', 'kaytettavat-energiamuodot', 'muu'],
    R.times(emptyMuuEnergiamuoto, 1)
  ),
  R.assocPath(
    ['tulokset', 'uusiutuvat-omavaraisenergiat'],
    R.times(emptyMuuEnergia, 5)
  ),
  R.assocPath(['perustiedot', 'uudisrakennus'], true),
  R.dissocPath(['perustiedot', 'laatimisvaihe']),
  R.assoc('versio', 2013),
  energiatodistus2018
);
