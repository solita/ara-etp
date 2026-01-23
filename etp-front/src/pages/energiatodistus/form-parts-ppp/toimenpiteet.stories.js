import PppToimenpiteet from './toimenpiteet.svelte';
import PppToimenpiteetOsio from './toimenpiteet-osio.svelte';

import * as empty from '@Pages/energiatodistus/empty';
import * as schema from '@Pages/energiatodistus/schema';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';
import { locale, waitLocale } from 'svelte-i18n';
import * as R from 'ramda';

const ValidSome = R.compose(Either.Right, Maybe.Some);

const requiredFields = [];

const toimenpideEhdotusGroup = [
  {
    id: 0,
    'label-fi': 'Lämmitys',
    'label-sv': 'Lämmitys (sv)',
    ordinal: 1
  },
  {
    id: 1,
    'label-fi': 'Lämmin käyttövesi',
    'label-sv': 'Lämmin käyttövesi (sv)',
    ordinal: 2
  },
  {
    id: 2,
    'label-fi': 'Ilmanvaihto',
    'label-sv': 'Ilmanvaihto (sv)',
    ordinal: 3
  },
  {
    id: 3,
    'label-fi': 'Valaistus',
    'label-sv': 'Valaistus (sv)',
    ordinal: 4
  },
  {
    id: 4,
    'label-fi': 'Jäähdytys',
    'label-sv': 'Jäähdytys (sv)',
    ordinal: 5
  },
  {
    id: 5,
    'label-fi': 'Uusiutuva energia',
    'label-sv': 'Uusiutuva energia (sv)',
    ordinal: 6
  },
  {
    id: 6,
    'label-fi': 'Ikkunat',
    'label-sv': 'Ikkunat (sv)',
    ordinal: 7
  },
  {
    id: 7,
    'label-fi': 'Ulko-ovet',
    'label-sv': 'Ulko-ovet (sv)',
    ordinal: 8
  },
  {
    id: 8,
    'label-fi': 'Yläpohja',
    'label-sv': 'Yläpohja (sv)',
    ordinal: 9
  },
  {
    id: 9,
    'label-fi': 'Alapohja',
    'label-sv': 'Alapohja (sv)',
    ordinal: 10
  },
  {
    id: 10,
    'label-fi': 'Julkisivu',
    'label-sv': 'Julkisivu (sv)',
    ordinal: 11
  },
  {
    id: 11,
    'label-fi': 'Muut toimenpiteet',
    'label-sv': 'Muut toimenpiteet (sv)',
    ordinal: 12
  }
];

const toimenpideEhdotukset = [
  {
    id: 0,
    'label-fi': 'Siirtyminen kaukolämpöön',
    'label-sv': 'Siirtyminen kaukolämpöön (sv)',
    'group-id': 0,
    valid: true
  },
  {
    id: 1,
    'label-fi': 'Maalämpöpumppu',
    'label-sv': 'Maalämpöpumppu (sv)',
    'group-id': 0,
    valid: true
  },
  {
    id: 2,
    'label-fi': 'Ilma-vesilämpöpumppu',
    'label-sv': 'Ilma-vesilämpöpumppu (sv)',
    'group-id': 0,
    valid: true
  },
  {
    id: 3,
    'label-fi': 'Poistoilmalämpöpumppu',
    'label-sv': 'Poistoilmalämpöpumppu (sv)',
    'group-id': 0,
    valid: true
  },
  {
    id: 4,
    'label-fi': 'Muu lämpöpumppu',
    'label-sv': 'Muu lämpöpumppu (sv)',
    'group-id': 0,
    valid: true
  },
  {
    id: 5,
    'label-fi': 'Hybridilämmitys',
    'label-sv': 'Hybridilämmitys (sv)',
    'group-id': 0,
    valid: true
  },
  {
    id: 6,
    'label-fi': 'Bioenergia',
    'label-sv': 'Bioenergia (sv)',
    'group-id': 0,
    valid: true
  },
  {
    id: 7,
    'label-fi': 'Muu lämmitystoimenpide',
    'label-sv': 'Muu lämmitystoimenpide (sv)',
    'group-id': 0,
    valid: true
  },
  {
    id: 8,
    'label-fi': 'Käyttövesivaraajan lisäys/muutos',
    'label-sv': 'Käyttövesivaraajan lisäys/muutos (sv)',
    'group-id': 1,
    valid: true
  },
  {
    id: 9,
    'label-fi': 'Vakiopaineventtiili',
    'label-sv': 'Vakiopaineventtiili (sv)',
    'group-id': 1,
    valid: true
  },
  {
    id: 10,
    'label-fi': 'Muu lämpimän käyttöveden toimenpide',
    'label-sv': 'Muu lämpimän käyttöveden toimenpide (sv)',
    'group-id': 1,
    valid: true
  },
  {
    id: 11,
    'label-fi': 'Koneellisen ilmanvaihdon muutos',
    'label-sv': 'Koneellisen ilmanvaihdon muutos (sv)',
    'group-id': 2,
    valid: true
  },
  {
    id: 12,
    'label-fi': 'Lämmöntalteenoton lisäys/parannus',
    'label-sv': 'Lämmöntalteenoton lisäys/parannus (sv)',
    'group-id': 2,
    valid: true
  },
  {
    id: 13,
    'label-fi': 'Ilmanvaihtokoneen uusiminen',
    'label-sv': 'Ilmanvaihtokoneen uusiminen (sv)',
    'group-id': 2,
    valid: true
  },
  {
    id: 14,
    'label-fi': 'Tarpeenmukainen ilmanvaihto',
    'label-sv': 'Tarpeenmukainen ilmanvaihto (sv)',
    'group-id': 2,
    valid: true
  },
  {
    id: 15,
    'label-fi': 'Muu ilmanvaihdon toimenpide',
    'label-sv': 'Muu ilmanvaihdon toimenpide (sv)',
    'group-id': 2,
    valid: true
  },
  {
    id: 16,
    'label-fi': 'Valaistuksen tehotiheyden muutos',
    'label-sv': 'Valaistuksen tehotiheyden muutos (sv)',
    'group-id': 3,
    valid: true
  },
  {
    id: 17,
    'label-fi': 'Tarpeenmukainen valaistus',
    'label-sv': 'Tarpeenmukainen valaistus (sv)',
    'group-id': 3,
    valid: true
  },
  {
    id: 18,
    'label-fi': 'Muu valaistuksen toimenpide',
    'label-sv': 'Muu valaistuksen toimenpide (sv)',
    'group-id': 3,
    valid: true
  },
  {
    id: 19,
    'label-fi': 'Siirtyminen kaukojäähdytykseen',
    'label-sv': 'Siirtyminen kaukojäähdytykseen (sv)',
    'group-id': 4,
    valid: true
  },
  {
    id: 20,
    'label-fi': 'Jäähdytyskoneen muutos/uusiminen',
    'label-sv': 'Jäähdytyskoneen muutos/uusiminen (sv)',
    'group-id': 4,
    valid: true
  },
  {
    id: 21,
    'label-fi': 'Passiivisen jäähdytyksen lisääminen',
    'label-sv': 'Passiivisen jäähdytyksen lisääminen (sv)',
    'group-id': 4,
    valid: true
  },
  {
    id: 22,
    'label-fi': 'Muu jäähdytystoimenpide',
    'label-sv': 'Muu jäähdytystoimenpide (sv)',
    'group-id': 4,
    valid: true
  },
  {
    id: 23,
    'label-fi': 'Aurinkosähkö',
    'label-sv': 'Aurinkosähkö (sv)',
    'group-id': 5,
    valid: true
  },
  {
    id: 24,
    'label-fi': 'Aurinkolämpö',
    'label-sv': 'Aurinkolämpö (sv)',
    'group-id': 5,
    valid: true
  },
  {
    id: 25,
    'label-fi': 'Tuulivoima',
    'label-sv': 'Tuulivoima (sv)',
    'group-id': 5,
    valid: true
  },
  {
    id: 26,
    'label-fi': 'Muu uusiutuvan energian toimenpide',
    'label-sv': 'Muu uusiutuvan energian toimenpide (sv)',
    'group-id': 5,
    valid: true
  },
  {
    id: 27,
    'label-fi': 'Muu ikkunatoimenpide',
    'label-sv': 'Muu ikkunatoimenpide (sv)',
    'group-id': 6,
    valid: true
  },
  {
    id: 28,
    'label-fi': 'Muu ulko-ovitoimenpide',
    'label-sv': 'Muu ulko-ovitoimenpide (sv)',
    'group-id': 7,
    valid: true
  },
  {
    id: 29,
    'label-fi': 'Muu yläpohjan toimenpide',
    'label-sv': 'Muu yläpohjan toimenpide (sv)',
    'group-id': 8,
    valid: true
  },
  {
    id: 30,
    'label-fi': 'Muu alapohjan toimenpide',
    'label-sv': 'Muu alapohjan toimenpide (sv)',
    'group-id': 9,
    valid: true
  },
  {
    id: 31,
    'label-fi': 'Tiiveyden parantaminen',
    'label-sv': 'Tiiveyden parantaminen (sv)',
    'group-id': 10,
    valid: true
  },
  {
    id: 32,
    'label-fi': 'Muu julkisivun toimenpide',
    'label-sv': 'Muu julkisivun toimenpide (sv)',
    'group-id': 10,
    valid: true
  },
  {
    id: 33,
    'label-fi': 'Energian varastointi',
    'label-sv': 'Energian varastointi (sv)',
    'group-id': 11,
    valid: true
  },
  {
    id: 34,
    'label-fi': 'Kulutusjousto',
    'label-sv': 'Kulutusjousto (sv)',
    'group-id': 11,
    valid: true
  },
  {
    id: 35,
    'label-fi': 'Varaavat tulisijat',
    'label-sv': 'Varaavat tulisijat (sv)',
    'group-id': 0,
    valid: false
  },
  {
    id: 36,
    'label-fi': 'Lämmönjaon hyötysuhteen muutos',
    'label-sv': 'Lämmönjaon hyötysuhteen muutos (sv)',
    'group-id': 0,
    valid: false
  },
  {
    id: 37,
    'label-fi': 'Lämmitysverkoston eristyksen muutos',
    'label-sv': 'Lämmitysverkoston eristyksen muutos (sv)',
    'group-id': 0,
    valid: false
  },
  {
    id: 38,
    'label-fi': 'Energiavaraajan muutos/lisäys',
    'label-sv': 'Energiavaraajan muutos/lisäys (sv)',
    'group-id': 0,
    valid: false
  },
  {
    id: 39,
    'label-fi': 'Lämmitysverkoston eristyksen muutos',
    'label-sv': 'Lämmitysverkoston eristyksen muutos (sv)',
    'group-id': 0,
    valid: false
  },
  {
    id: 40,
    'label-fi': 'Märkätilojen lämmityksen muutos',
    'label-sv': 'Märkätilojen lämmityksen muutos (sv)',
    'group-id': 0,
    valid: false
  },
  {
    id: 41,
    'label-fi': 'Lämmin käyttövesi',
    'label-sv': 'Lämmin käyttövesi (sv)',
    'group-id': 1,
    valid: false
  },
  {
    id: 42,
    'label-fi': 'Lämpimän käyttövesiverkoston eristyksen muutos',
    'label-sv': 'Lämpimän käyttövesiverkoston eristyksen muutos (sv)',
    'group-id': 1,
    valid: false
  },
  {
    id: 43,
    'label-fi': 'Lämpimän käyttöveden kiertojohdon muutos',
    'label-sv': 'Lämpimän käyttöveden kiertojohdon muutos (sv)',
    'group-id': 1,
    valid: false
  },
  {
    id: 44,
    'label-fi': 'Painovoimaisen IV:n muutos koneelliseksi',
    'label-sv': 'Painovoimaisen IV:n muutos koneelliseksi (sv)',
    'group-id': 2,
    valid: false
  },
  {
    id: 45,
    'label-fi': 'Jäteveden lämmöntalteenotto',
    'label-sv': 'Jäteveden lämmöntalteenotto (sv)',
    'group-id': 1,
    valid: false
  },
  {
    id: 46,
    'label-fi': 'Ikkunoiden uusiminen',
    'label-sv': 'Ikkunoiden uusiminen (sv)',
    'group-id': 6,
    valid: true
  },
  {
    id: 47,
    'label-fi': 'Ulko-ovien uusiminen',
    'label-sv': 'Ulko-ovien uusiminen (sv)',
    'group-id': 7,
    valid: true
  },
  {
    id: 48,
    'label-fi': 'Yläpohjan lisäeristys',
    'label-sv': 'Yläpohjan lisäeristys (sv)',
    'group-id': 8,
    valid: true
  },
  {
    id: 49,
    'label-fi': 'Alapohjan lisäeristys',
    'label-sv': 'Alapohjan lisäeristys (sv)',
    'group-id': 9,
    valid: true
  },
  {
    id: 50,
    'label-fi': 'Julkisivun lisäeristys',
    'label-sv': 'Julkisivun lisäeristys (sv)',
    'group-id': 10,
    valid: true
  }
];

const luokittelut = {
  'toimenpide-ehdotus-group': toimenpideEhdotusGroup,
  'toimenpide-ehdotus': toimenpideEhdotukset
};

const testEnergiatodistus = () => {
  const energiatodistus = empty.energiatodistus2018();
  energiatodistus.id = 442;

  energiatodistus.lahtotiedot['lammitetty-nettoala'] = Either.Right(
    Maybe.Some(150)
  );
  energiatodistus.tulokset['kaytettavat-energiamuodot'].sahko = Either.Right(
    Maybe.Some(12000)
  );
  energiatodistus.tulokset['kaytettavat-energiamuodot'].kaukolampo =
    Either.Right(Maybe.Some(8000));
  energiatodistus.tulokset['kaytettavat-energiamuodot'][
    'uusiutuva-polttoaine'
  ] = Either.Right(Maybe.Some(5000));
  energiatodistus.tulokset['kaytettavat-energiamuodot'][
    'fossiilinen-polttoaine'
  ] = Either.Right(Maybe.Some(12000));
  energiatodistus.tulokset['kaytettavat-energiamuodot'].kaukojaahdytys =
    Either.Right(Maybe.Some(5000));
  energiatodistus.tulokset['uusiutuvat-omavaraisenergiat'].aurinkosahko =
    Either.Right(Maybe.Some(5000));
  energiatodistus['toteutunut-ostoenergiankulutus']['ostettu-energia'][
    'kaukolampo-vuosikulutus'
  ] = Either.Right(Maybe.Some(5000));

  return energiatodistus;
};

const testPpp = energiatodistusId => {
  const ppp = empty.perusparannuspassi(energiatodistusId);

  ppp.id = 4245;
  ppp.vaiheet[0].tulokset['vaiheen-alku-pvm'] = ValidSome(2031);
  ppp.vaiheet[0].toimenpiteet['toimenpideseloste-fi'] = ValidSome('HELLO');
  ppp.vaiheet[0].toimenpiteet['toimenpideseloste-sv'] = ValidSome('TJENA');
  ppp.vaiheet[0].toimenpiteet['toimenpide-ehdotukset'] = R.map(
    Maybe.Some,
    [1, 0, 2, 1, 0, 2]
  );

  ppp.vaiheet[1].tulokset['vaiheen-alku-pvm'] = ValidSome(2035);

  ppp.vaiheet[0].tulokset['ostoenergian-tarve-kaukolampo'] = ValidSome(15000);
  ppp.vaiheet[1].tulokset['ostoenergian-tarve-kaukolampo'] = ValidSome(10000);

  ppp.vaiheet[1].tulokset['uusiutuvan-energian-hyodynnetty-osuus'] =
    ValidSome(9000);

  ppp.tulokset['kaukolampo-hinta'] = ValidSome(0.18);
  return ppp;
};

const saveSchema = R.compose(
  R.reduce(schema.assocRequired, R.__, requiredFields)
)(schema.perusparannuspassi);
export default {
  title: 'Pages/Energiatodistus/PPP Toimenpiteet',
  component: PppToimenpiteet
};

const Template = args => ({
  Component: PppToimenpiteet,
  props: args
});

export const Default = Template.bind({});
Default.args = {
  energiatodistus: testEnergiatodistus(),
  toimenpideEhdotuksetLuokittelu: toimenpideEhdotukset,
  pppSchema: saveSchema,
  inputLanguage: 'fi',
  perusparannuspassi: testPpp(442)
};

const OsioTemplate = args => ({
  Component: PppToimenpiteetOsio,
  props: args
});

export const Osio = OsioTemplate.bind({});
Osio.args = {
  energiatodistus: testEnergiatodistus(),
  luokittelut: luokittelut,
  pppSchema: saveSchema,
  open: true,
  vaihe: testPpp(442).vaiheet[1],
  inputLanguage: 'fi',
  perusparannuspassi: testPpp(442)
};
