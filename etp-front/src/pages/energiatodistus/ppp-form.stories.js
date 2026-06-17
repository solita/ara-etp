import PPPForm from './ppp-form';

import * as empty from '@Pages/energiatodistus/empty';
import * as schema from '@Pages/energiatodistus/schema';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';
import * as R from 'ramda';

const pppValidation = {
  requiredAll: [],
  requiredBypass: [],
  numeric: [],
  vaiheNumeric: [],
  vaiheAll: [],
  vaiheBypass: []
};

/* For ET this comes from the /validation/required API endpoint */
const requiredFields = [
  'passin-perustiedot.havainnointikaynti',
  'passin-perustiedot.passin-esittely'
];

const saveSchema = R.compose(
  R.reduce(schema.assocRequired, R.__, requiredFields)
)(schema.perusparannuspassi);

export default {
  title: 'Pages/Energiatodistus/PPP Form',
  component: PPPForm
};

const testEnergiatodistus = () => {
  const energiatodistus = empty.energiatodistus2018();
  energiatodistus.id = 442;

  energiatodistus.lahtotiedot['lammitetty-nettoala'] = Either.Right(
    Maybe.Some(150)
  );
  energiatodistus.tulokset['kaytettavat-energiamuodot'].sahko = Either.Right(
    Maybe.Some(3000)
  );

  energiatodistus.lahtotiedot.lammitys['lammitysmuoto-1'] = R.map(
    Maybe.Some,
    luokittelut.lammitysmuoto[0]
  );

  energiatodistus.tulokset['kaytettavat-energiamuodot'].kaukolampo =
    Either.Right(Maybe.Some(12000));
  energiatodistus.tulokset['kaytettavat-energiamuodot'][
    'uusiutuva-polttoaine'
  ] = Either.Right(Maybe.Some(1000));
  energiatodistus.tulokset['kaytettavat-energiamuodot'][
    'fossiilinen-polttoaine'
  ] = Either.Right(Maybe.None());
  energiatodistus.tulokset['kaytettavat-energiamuodot'].kaukojaahdytys =
    Either.Right(Maybe.None());
  energiatodistus.tulokset['uusiutuvat-omavaraisenergiat'].aurinkosahko =
    Either.Right(Maybe.None());

  energiatodistus['toteutunut-ostoenergiankulutus']['ostettu-energia'][
    'kaukolampo-vuosikulutus'
  ] = Either.Right(Maybe.Some(11000));
  energiatodistus['toteutunut-ostoenergiankulutus']['ostettu-energia'][
    'kokonaissahko-vuosikulutus'
  ] = Either.Right(Maybe.Some(4000));

  return energiatodistus;
};

const testPpp = energiatodistusId => {
  const ppp = empty.perusparannuspassi(energiatodistusId);

  ppp.id = 4245;

  ppp.tulokset['fossiiliset-pat-hinta'] = Either.Right(Maybe.Some(13.5));
  ppp.tulokset['kaukojaahdytys-hinta'] = Either.Right(Maybe.Some(7.0));
  ppp.tulokset['kaukolampo-hinta'] = Either.Right(Maybe.Some(11.5));
  ppp.tulokset['sahko-hinta'] = Either.Right(Maybe.Some(22.0));
  ppp.tulokset['uusiutuvat-pat-hinta'] = Either.Right(Maybe.Some(9.0));

  ppp.vaiheet[0].tulokset['vaiheen-alku-pvm'] = Either.Right(Maybe.Some(2030));
  ppp.vaiheet[0].valid = true;
  ppp.vaiheet[0].tulokset['ostoenergian-tarve-kaukolampo'] = Either.Right(
    Maybe.Some(10000)
  );
  ppp.vaiheet[0].tulokset['ostoenergian-tarve-sahko'] = Either.Right(
    Maybe.Some(3000)
  );

  ppp.vaiheet[0].tulokset['toteutunut-ostoenergia-kaukolampo'] = Either.Right(
    Maybe.Some(9000)
  );
  ppp.vaiheet[0].tulokset['toteutunut-ostoenergia-sahko'] = Either.Right(
    Maybe.Some(4000)
  );
  ppp.vaiheet[0].tulokset['uusiutuvan-energian-hyodynnetty-osuus'] =
    Either.Right(Maybe.Some('1000'));

  ppp.vaiheet[1].tulokset['vaiheen-alku-pvm'] = Either.Right(Maybe.Some(2035));
  ppp.vaiheet[1].valid = true;

  return ppp;
};

const Template = args => ({
  Component: PPPForm,
  props: args
});

const testData = () => {
  const energiatodistus = testEnergiatodistus();
  const perusparannuspassi = testPpp(energiatodistus.id);

  return { energiatodistus, perusparannuspassi };
};

const luokittelut = {
  lammitysmuoto: [
    {
      id: 0,
      'label-fi': 'Kaukolämpö',
      'label-sv': 'Fjärrvärme',
      valid: true
    },
    {
      id: 1,
      'label-fi': 'Sähkö',
      'label-sv': 'El',
      valid: true
    },
    {
      id: 2,
      'label-fi': 'Puu',
      'label-sv': 'Trä',
      valid: true
    },
    {
      id: 3,
      'label-fi': 'Pelletti',
      'label-sv': 'Pellet',
      valid: true
    },
    {
      id: 4,
      'label-fi': 'Öljy',
      'label-sv': 'Olja',
      valid: true
    },
    {
      id: 5,
      'label-fi': 'Kaasu',
      'label-sv': 'Gas',
      valid: true
    },
    {
      id: 6,
      'label-fi': 'Maalämpöpumppu',
      'label-sv': 'Jordvärmepump',
      valid: true
    },
    {
      id: 7,
      'label-fi': 'Vesi-ilmalämpöpumppu',
      'label-sv': 'Vatten-luftvärmepump',
      valid: true
    },
    {
      id: 8,
      'label-fi': 'Poistoilmalämpöpumppu',
      'label-sv': 'Frånluftsvärmepump',
      valid: true
    },
    {
      id: 9,
      'label-fi': 'Muu lämmitysjärjestelmä',
      'label-sv': 'Annat uppvärmningssystem',
      valid: true
    }
  ],
  ilmanvaihtotyypit: [
    {
      id: 0,
      'label-fi': 'Painovoimainen ilmanvaihtojärjestelmä',
      'label-sv': 'Självdragsventilationssystem',
      valid: true
    },
    {
      id: 1,
      'label-fi': 'Koneellinen poistoilmanvaihtojärjestelmä',
      'label-sv': 'Maskinellt frånluftsventilationssystem',
      valid: true
    },
    {
      id: 7,
      'label-fi':
        'Koneellinen poistoilmanvaihtojärjestelmä lämmöntalteenotolla',
      'label-sv': 'Mekaniskt frånluftsventilationssystem med värmeåtervinning',
      valid: true
    },
    {
      id: 2,
      'label-fi': 'Koneellinen tulo- ja poistoilmanvaihtojärjestelmä',
      'label-sv': 'Maskinellt till- och frånluftsventilationssystem',
      valid: true
    },
    {
      id: 8,
      'label-fi':
        'Koneellinen tulo- ja poistoilmanvaihtojärjestelmä lämmöntalteenotolla',
      'label-sv':
        'Mekaniskt till- och frånluftsventilationssystem med värmeåtervinning',
      valid: true
    },
    {
      id: 3,
      'label-fi': 'Poistoilmalämpöpumppu (vain käyttötarkoitusluokka 1)',
      'label-sv': 'Frånluftsvärmepump (endast användningsklass 1)',
      valid: true
    },
    {
      id: 4,
      'label-fi': 'Painovoimainen/koneellinen poisto',
      'label-sv': 'Naturlig/maskinell frånluftsventilation',
      valid: true
    },
    {
      id: 5,
      'label-fi': 'Painovoimainen/koneellinen tulo- ja poisto',
      'label-sv': 'Naturlig/maskinell till- och frånluftsventilation',
      valid: true
    },
    {
      id: 6,
      'label-fi': 'Muu ilmanvaihtojärjestelmä',
      'label-sv': 'Annat ventilationssystem',
      valid: true
    }
  ],
  uusiutuvaEnergia: [
    {
      id: 0,
      'label-fi': 'Ei ole',
      'label-sv': 'Ei ole (sv)',
      valid: true
    },
    {
      id: 1,
      'label-fi': 'Aurinkosähkö',
      'label-sv': 'Aurinkosähkö (sv)',
      valid: true
    },
    {
      id: 2,
      'label-fi': 'Aurinkolämpö',
      'label-sv': 'Aurinkolämpö (sv)',
      valid: true
    },
    {
      id: 3,
      'label-fi': 'Aurinkosähkö ja -lämpö',
      'label-sv': 'Aurinkosähkö ja -lämpö (sv)',
      valid: true
    },
    {
      id: 4,
      'label-fi': 'Tuulivoima',
      'label-sv': 'Tuulivoima (sv)',
      valid: true
    },
    {
      id: 5,
      'label-fi': 'Muu',
      'label-sv': 'Muu (sv)',
      valid: true
    }
  ],
  mahdollisuusLiittya: [
    {
      id: 0,
      'label-fi': 'Kyllä, kaukolämpöön',
      'label-sv': 'Kyllä, kaukolämpöön (sv)',
      valid: true
    },
    {
      id: 1,
      'label-fi': 'Kyllä, Kaukojäähdytykseen',
      'label-sv': 'Kyllä, kaukolämpöön (sv)',
      valid: true
    },
    {
      id: 2,
      'label-fi': 'Kyllä, molempiin',
      'label-sv': 'Kyllä, molempiin (sv)',
      valid: true
    },
    {
      id: 3,
      'label-fi': 'Ei kumpaankaan',
      'label-sv': 'Ei kumpaankaan (sv)',
      valid: true
    }
  ],
  jaahdytys: [
    {
      id: 0,
      'label-fi': 'Ei ole',
      'label-sv': 'Ei ole (sv)',
      valid: true
    },
    {
      id: 1,
      'label-fi': 'Aurinkosuojaukset',
      'label-sv': 'Aurinkosuojaukset (sv)',
      valid: true
    },
    {
      id: 2,
      'label-fi': 'Muu',
      'label-sv': 'Muu (sv)',
      valid: true
    }
  ]
};
export const Default = Template.bind({});
Default.args = {
  schema: saveSchema,
  inputLanguage: 'fi',
  luokittelut,
  pppValidation,
  ...testData()
};
