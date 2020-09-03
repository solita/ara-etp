import * as R from 'ramda';

import * as Maybe from '@Utility/maybe-utils';

import * as EtUtils from './energiatodistus-utils';

const emptyRakennusVaippa = _ => ({
  ala: Maybe.None(),
  U: Maybe.None()
});

const emptyIkkuna = _ => ({
  ala: Maybe.None(),
  U: Maybe.None(),
  'g-ks': Maybe.None()
});

const emptyIV = _ => ({
  poisto: Maybe.None(),
  tulo: Maybe.None(),
  sfp: Maybe.None()
});

const emptyLammitys = _ => ({
  'tuoton-hyotysuhde': Maybe.None(),
  'jaon-hyotysuhde': Maybe.None(),
  lampokerroin: Maybe.None(),
  apulaitteet: Maybe.None(),
  'lampohavio-lammittamaton-tila': Maybe.None(),
  'lampopumppu-tuotto-osuus': Maybe.None()
});

const emptyLammitysMaaraTuotto = _ => ({
  maara: Maybe.None(),
  tuotto: Maybe.None()
});

const emptyToimenpide = _ => ({
  'nimi-fi': Maybe.None(),
  'nimi-sv': Maybe.None(),
  lampo: Maybe.None(),
  sahko: Maybe.None(),
  jaahdytys: Maybe.None(),
  'eluvun-muutos': Maybe.None()
});

const emptyHuomio = _ => ({
  'teksti-fi': Maybe.None(),
  'teksti-sv': Maybe.None(),
  toimenpide: [emptyToimenpide(), emptyToimenpide(), emptyToimenpide()]
});

const emptySahkoLampo = _ => ({
  sahko: Maybe.None(),
  lampo: Maybe.None()
});

const emptySisKuorma = _ => ({
  kayttoaste: Maybe.None(),
  lampokuorma: Maybe.None()
});

const emptyMuuPolttoaine = _ => ({
  nimi: Maybe.None(),
  yksikko: Maybe.None(),
  muunnoskerroin: Maybe.None(),
  'maara-vuodessa': Maybe.None()
});

const formalDescription = _ => ({
  id: Maybe.None(),
  'kuvaus-fi': Maybe.None(),
  'kuvaus-sv': Maybe.None()
});

export const energiatodistus2018 = _ => ({
  'tila-id': EtUtils.tila.draft,
  'laatija-id': Maybe.None(),
  'laskutettava-yritys-id': Maybe.None(),
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
    valmistumisvuosi: Maybe.None(),
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
    'lammitetty-nettoala': Maybe.None(),
    rakennusvaippa: {
      ilmanvuotoluku: Maybe.None(),
      lampokapasiteetti: Maybe.None(),
      ilmatilavuus: Maybe.None(),
      'kylmasillat-UA': Maybe.None(),
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
          lampotilasuhde: Maybe.None(),
          jaatymisenesto: Maybe.None()
        },
        emptyIV()
      ),
      'tyyppi-id': Maybe.None(),
      'kuvaus-fi': Maybe.None(),
      'kuvaus-sv': Maybe.None(),
      'lto-vuosihyotysuhde': Maybe.None(),
      'tuloilma-lampotila': Maybe.None()
    },
    lammitys: {
      'lammitysmuoto-1': formalDescription(),
      'lammitysmuoto-2': formalDescription(),
      lammonjako: formalDescription(),
      'tilat-ja-iv': emptyLammitys(),
      'lammin-kayttovesi': emptyLammitys(),
      takka: emptyLammitysMaaraTuotto(),
      ilmanlampopumppu: emptyLammitysMaaraTuotto()
    },
    jaahdytysjarjestelma: {
      'jaahdytyskauden-painotettu-kylmakerroin': Maybe.None()
    },
    'lkvn-kaytto': {
      ominaiskulutus: Maybe.None(),
      'lammitysenergian-nettotarve': Maybe.None()
    },
    'sis-kuorma': {
      henkilot: emptySisKuorma(),
      kuluttajalaitteet: emptySisKuorma(),
      valaistus: emptySisKuorma()
    }
  },
  tulokset: {
    'kaytettavat-energiamuodot': {
      'fossiilinen-polttoaine': Maybe.None(),
      sahko: Maybe.None(),
      kaukojaahdytys: Maybe.None(),
      kaukolampo: Maybe.None(),
      'uusiutuva-polttoaine': Maybe.None()
    },
    'uusiutuvat-omavaraisenergiat': {
      aurinkosahko: Maybe.None(),
      tuulisahko: Maybe.None(),
      aurinkolampo: Maybe.None(),
      muulampo: Maybe.None(),
      muusahko: Maybe.None(),
      lampopumppu: Maybe.None()
    },
    kuukausierittely: [],
    'tekniset-jarjestelmat': {
      'tilojen-lammitys': emptySahkoLampo(),
      'tuloilman-lammitys': emptySahkoLampo(),
      'kayttoveden-valmistus': emptySahkoLampo(),
      'iv-sahko': Maybe.None(),
      jaahdytys: R.assoc('kaukojaahdytys', Maybe.None(), emptySahkoLampo()),
      'kuluttajalaitteet-ja-valaistus-sahko': Maybe.None()
    },
    nettotarve: {
      'tilojen-lammitys-vuosikulutus': Maybe.None(),
      'ilmanvaihdon-lammitys-vuosikulutus': Maybe.None(),
      'kayttoveden-valmistus-vuosikulutus': Maybe.None(),
      'jaahdytys-vuosikulutus': Maybe.None()
    },
    lampokuormat: {
      aurinko: Maybe.None(),
      ihmiset: Maybe.None(),
      kuluttajalaitteet: Maybe.None(),
      valaistus: Maybe.None(),
      kvesi: Maybe.None()
    },
    laskentatyokalu: Maybe.None()
  },
  'toteutunut-ostoenergiankulutus': {
    'ostettu-energia': {
      'kaukolampo-vuosikulutus': Maybe.None(),
      'kokonaissahko-vuosikulutus': Maybe.None(),
      'kiinteistosahko-vuosikulutus': Maybe.None(),
      'kayttajasahko-vuosikulutus': Maybe.None(),
      'kaukojaahdytys-vuosikulutus': Maybe.None()
    },
    'ostetut-polttoaineet': {
      'kevyt-polttooljy': Maybe.None(),
      'pilkkeet-havu-sekapuu': Maybe.None(),
      'pilkkeet-koivu': Maybe.None(),
      puupelletit: Maybe.None(),
      muu: R.times(emptyMuuPolttoaine, 4)
    },
    'sahko-vuosikulutus-yhteensa': Maybe.None(),
    'kaukolampo-vuosikulutus-yhteensa': Maybe.None(),
    'polttoaineet-vuosikulutus-yhteensa': Maybe.None(),
    'kaukojaahdytys-vuosikulutus-yhteensa': Maybe.None()
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
  'korvattu-energiatodistus-id': Maybe.None()
});

const emptyMuuEnergiamuoto = _ => ({
  nimi: Maybe.None(),
  muotokerroin: Maybe.None(),
  ostoenergia: Maybe.None()
});

const emptyMuuEnergia = _ => ({
  'nimi-fi': Maybe.None(),
  'nimi-sv': Maybe.None(),
  vuosikulutus: Maybe.None()
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
    R.times(emptyMuuEnergiamuoto, 3)
  ),
  R.assocPath(
    ['tulokset', 'uusiutuvat-omavaraisenergiat'],
    R.times(emptyMuuEnergia, 5)
  ),
  R.assocPath(['perustiedot', 'uudisrakennus'], false),
  R.dissocPath(['perustiedot', 'laatimisvaihe']),
  energiatodistus2018
);
