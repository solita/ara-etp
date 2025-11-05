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

const luokittelu = [
  {
    id: 0,
    'label-fi': 'Siirtyminen kaukolämpöön',
    'label-sv': 'Siirtyminen kaukolämpöön (sv)',
    valid: true
  },
  {
    id: 1,
    'label-fi': 'Maalämpöpumppu',
    'label-sv': 'Maalämpöpumppu (sv)',
    valid: true
  },
  {
    id: 2,
    'label-fi': 'Ilma-vesilämpöpumppu',
    'label-sv': 'Ilma-vesilämpöpumppu (sv)',
    valid: true
  },
  {
    id: 3,
    'label-fi': 'Poistoilmalämpöpumppu',
    'label-sv': 'Poistoilmalämpöpumppu (sv)',
    valid: true
  },
  {
    id: 4,
    'label-fi': 'Muu lämpöpumppu',
    'label-sv': 'Muu lämpöpumppu (sv)',
    valid: true
  },
  {
    id: 5,
    'label-fi': 'Hybridilämmitys',
    'label-sv': 'Hybridilämmitys (sv)',
    valid: true
  },
  {
    id: 6,
    'label-fi': 'Bioenergia',
    'label-sv': 'Bioenergia (sv)',
    valid: true
  },
  {
    id: 7,
    'label-fi': 'Muu lämmitystoimenpide',
    'label-sv': 'Muu lämmitystoimenpide (sv)',
    valid: true
  },
  {
    id: 8,
    'label-fi': 'Käyttövesivaraajan lisäys/muutos',
    'label-sv': 'Käyttövesivaraajan lisäys/muutos (sv)',
    valid: true
  },
  {
    id: 9,
    'label-fi': 'Vakiopaineventtiili',
    'label-sv': 'Vakiopaineventtiili (sv)',
    valid: true
  },
  {
    id: 10,
    'label-fi': 'Muu lämpimän käyttöveden toimenpide',
    'label-sv': 'Muu lämpimän käyttöveden toimenpide (sv)',
    valid: true
  },
  {
    id: 11,
    'label-fi': 'Koneellisen ilmanvaihdon muutos',
    'label-sv': 'Koneellisen ilmanvaihdon muutos (sv)',
    valid: true
  },
  {
    id: 12,
    'label-fi': 'Lämmöntalteenoton lisäys/parannus',
    'label-sv': 'Lämmöntalteenoton lisäys/parannus (sv)',
    valid: true
  },
  {
    id: 13,
    'label-fi': 'Ilmanvaihtokoneen uusiminen',
    'label-sv': 'Ilmanvaihtokoneen uusiminen (sv)',
    valid: true
  },
  {
    id: 14,
    'label-fi': 'Tarpeenmukainen ilmanvaihto',
    'label-sv': 'Tarpeenmukainen ilmanvaihto (sv)',
    valid: true
  },
  {
    id: 15,
    'label-fi': 'Muu ilmanvaihdon toimenpide',
    'label-sv': 'Muu ilmanvaihdon toimenpide (sv)',
    valid: true
  },
  {
    id: 16,
    'label-fi': 'Valaistuksen tehotiheyden muutos',
    'label-sv': 'Valaistuksen tehotiheyden muutos (sv)',
    valid: true
  },
  {
    id: 17,
    'label-fi': 'Tarpeenmukainen valaistus',
    'label-sv': 'Tarpeenmukainen valaistus (sv)',
    valid: true
  },
  {
    id: 18,
    'label-fi': 'Muu valaistuksen toimenpide',
    'label-sv': 'Muu valaistuksen toimenpide (sv)',
    valid: true
  },
  {
    id: 19,
    'label-fi': 'Siirtyminen kaukojäähdytykseen',
    'label-sv': 'Siirtyminen kaukojäähdytykseen (sv)',
    valid: true
  },
  {
    id: 20,
    'label-fi': 'Jäähdytyskoneen muutos/uusiminen',
    'label-sv': 'Jäähdytyskoneen muutos/uusiminen (sv)',
    valid: true
  },
  {
    id: 21,
    'label-fi': 'Passiivisen jäähdytyksen lisääminen',
    'label-sv': 'Passiivisen jäähdytyksen lisääminen (sv)',
    valid: true
  },
  {
    id: 22,
    'label-fi': 'Muu jäähdytystoimenpide',
    'label-sv': 'Muu jäähdytystoimenpide (sv)',
    valid: true
  },
  {
    id: 23,
    'label-fi': 'Aurinkosähkö',
    'label-sv': 'Aurinkosähkö (sv)',
    valid: true
  },
  {
    id: 24,
    'label-fi': 'Aurinkolämpö',
    'label-sv': 'Aurinkolämpö (sv)',
    valid: true
  },
  {
    id: 25,
    'label-fi': 'Tuulivoima',
    'label-sv': 'Tuulivoima (sv)',
    valid: true
  },
  {
    id: 26,
    'label-fi': 'Muu uusiutuvan energian toimenpide',
    'label-sv': 'Muu uusiutuvan energian toimenpide (sv)',
    valid: true
  },
  {
    id: 27,
    'label-fi': 'Muu ikkunatoimenpide',
    'label-sv': 'Muu ikkunatoimenpide (sv)',
    valid: true
  },
  {
    id: 28,
    'label-fi': 'Muu ulko-ovitoimenpide',
    'label-sv': 'Muu ulko-ovitoimenpide (sv)',
    valid: true
  },
  {
    id: 29,
    'label-fi': 'Muu yläpohjan toimenpide',
    'label-sv': 'Muu yläpohjan toimenpide (sv)',
    valid: true
  },
  {
    id: 30,
    'label-fi': 'Muu alapohjan toimenpide',
    'label-sv': 'Muu alapohjan toimenpide (sv)',
    valid: true
  },
  {
    id: 31,
    'label-fi': 'Tiiveyden parantaminen',
    'label-sv': 'Tiiveyden parantaminen (sv)',
    valid: true
  },
  {
    id: 32,
    'label-fi': 'Muu julkisivun toimenpide',
    'label-sv': 'Muu julkisivun toimenpide (sv)',
    valid: true
  },
  {
    id: 33,
    'label-fi': 'Energian varastointi',
    'label-sv': 'Energian varastointi (sv)',
    valid: true
  },
  {
    id: 34,
    'label-fi': 'Kulutusjousto',
    'label-sv': 'Kulutusjousto (sv)',
    valid: true
  },
  {
    id: 35,
    'label-fi': 'Varaavat tulisijat',
    'label-sv': 'Varaavat tulisijat (sv)',
    valid: true
  },
  {
    id: 36,
    'label-fi': 'Lämmönjaon hyötysuhteen muutos',
    'label-sv': 'Lämmönjaon hyötysuhteen muutos (sv)',
    valid: true
  },
  {
    id: 37,
    'label-fi': 'Lämmitysverkoston eristyksen muutos',
    'label-sv': 'Lämmitysverkoston eristyksen muutos (sv)',
    valid: true
  },
  {
    id: 38,
    'label-fi': 'Energiavaraajan muutos/lisäys',
    'label-sv': 'Energiavaraajan muutos/lisäys (sv)',
    valid: true
  },
  {
    id: 39,
    'label-fi': 'Lämmitysverkoston eristyksen muutos',
    'label-sv': 'Lämmitysverkoston eristyksen muutos (sv)',
    valid: true
  },
  {
    id: 40,
    'label-fi': 'Märkätilojen lämmityksen muutos',
    'label-sv': 'Märkätilojen lämmityksen muutos (sv)',
    valid: true
  },
  {
    id: 41,
    'label-fi': 'Lämmin käyttövesi',
    'label-sv': 'Lämmin käyttövesi (sv)',
    valid: true
  },
  {
    id: 42,
    'label-fi': 'Lämpimän käyttövesiverkoston eristyksen muutos',
    'label-sv': 'Lämpimän käyttövesiverkoston eristyksen muutos (sv)',
    valid: true
  },
  {
    id: 43,
    'label-fi': 'Lämpimän käyttöveden kiertojohdon muutos',
    'label-sv': 'Lämpimän käyttöveden kiertojohdon muutos (sv)',
    valid: true
  },
  {
    id: 44,
    'label-fi': 'Painovoimaisen IV:n muutos koneelliseksi',
    'label-sv': 'Painovoimaisen IV:n muutos koneelliseksi (sv)',
    valid: true
  },
  {
    id: 45,
    'label-fi': 'Jäteveden lämmöntalteenotto',
    'label-sv': 'Jäteveden lämmöntalteenotto (sv)',
    valid: true
  }
];

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
  toimenpideEhdotuksetLuokittelu: luokittelu,
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
  toimenpideEhdotuksetLuokittelu: luokittelu,
  pppSchema: saveSchema,
  open: true,
  vaihe: testPpp(442).vaiheet[1],
  inputLanguage: 'fi',
  perusparannuspassi: testPpp(442)
};
