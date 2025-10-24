import PppToimenpiteet from './toimenpiteet.svelte';

import * as empty from '@Pages/energiatodistus/empty';
import * as schema from '@Pages/energiatodistus/schema';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';
import { locale, waitLocale } from 'svelte-i18n';
import * as R from 'ramda';

const ValidSome = R.compose(Either.Right, Maybe.Some);

const requiredFields = [];

// Using subset of lammitysmuoto as it will be similiar
const luokittelu = [
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

  ppp.vaiheet[0].valid = true;
  ppp.vaiheet[1].valid = true;

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
