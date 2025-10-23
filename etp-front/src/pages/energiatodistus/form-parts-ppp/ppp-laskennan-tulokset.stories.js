import Component from './ppp-laskennan-tulokset.svelte';

import * as empty from '@Pages/energiatodistus/empty';
import * as schema from '@Pages/energiatodistus/schema';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';
import * as R from 'ramda';

/* For ET this comes from the /validation/required API endpoint */
const requiredFields = [
  'passin-perustiedot.havainnointikaynti',
  'passin-perustiedot.passin-esittely'
];

const saveSchema = R.compose(
  R.reduce(schema.assocRequired, R.__, requiredFields)
)(schema.perusparannuspassi);

export default {
  title: 'Pages/Energiatodistus/PPP Laskennan tulokset',
  component: Component
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
  ppp.vaiheet[0].tulokset['vaiheen-alku-pvm'] = Either.Right(Maybe.Some(2030));
  ppp.vaiheet[0].valid = true;

  return ppp;
};

const Template = args => ({
  Component,
  props: args
});

const testData = () => {
  const energiatodistus = testEnergiatodistus();
  const perusparannuspassi = testPpp(energiatodistus.id);

  return { energiatodistus, perusparannuspassi };
};

export const Default = Template.bind({});
Default.args = {
  schema: saveSchema,
  ...testData()
};
