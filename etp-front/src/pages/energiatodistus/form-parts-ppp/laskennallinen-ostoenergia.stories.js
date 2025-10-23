import LaskennallinenOstoenergia from './laskennallinen-ostoenergia.svelte';

import * as empty from '@Pages/energiatodistus/empty';
import * as schema from '@Pages/energiatodistus/schema';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';
import * as R from 'ramda';

export default {
  title: 'Pages/Energiatodistus/PPP Laskennallinen Ostoenergia',
  component: LaskennallinenOstoenergia
};

const Template = args => ({
  Component: LaskennallinenOstoenergia,
  props: args
});

// Mock energiatodistus with consumption data
const mockEnergiatodistus = R.compose(
  R.assocPath(
    ['tulokset', 'kaytettavat-energiamuodot', 'kaukolampo'],
    Either.Right(Maybe.Some(15000))
  ),
  R.assocPath(
    ['tulokset', 'kaytettavat-energiamuodot', 'sahko'],
    Either.Right(Maybe.Some(10000))
  ),
  R.assocPath(
    ['tulokset', 'kaytettavat-energiamuodot', 'uusiutuva-polttoaine'],
    Either.Right(Maybe.Some(800))
  ),
  R.assocPath(
    ['tulokset', 'kaytettavat-energiamuodot', 'fossiilinen-polttoaine'],
    Either.Right(Maybe.Some(500))
  ),
  R.assocPath(
    ['tulokset', 'kaytettavat-energiamuodot', 'kaukojaahdytys'],
    Either.Right(Maybe.Some(300))
  )
)(empty.energiatodistus2018(1));

const mockPerusparannuspassi = R.compose(
  R.assocPath(['tulokset', 'kaukolampo-hinta'], Either.Right(Maybe.Some(8.5))),
  R.assocPath(['tulokset', 'sahko-hinta'], Either.Right(Maybe.Some(15.2))),
  R.assocPath(
    ['tulokset', 'uusiutuvat-pat-hinta'],
    Either.Right(Maybe.Some(6.0))
  ),
  R.assocPath(
    ['tulokset', 'fossiiliset-pat-hinta'],
    Either.Right(Maybe.Some(10.0))
  ),
  R.assocPath(
    ['tulokset', 'kaukojaahdytys-hinta'],
    Either.Right(Maybe.Some(5.5))
  )
)(empty.perusparannuspassi(1));

export const Default = Template.bind({});
Default.args = {
  schema: schema.perusparannuspassi,
  perusparannuspassi: mockPerusparannuspassi,
  energiatodistus: mockEnergiatodistus,
  disabled: false
};
