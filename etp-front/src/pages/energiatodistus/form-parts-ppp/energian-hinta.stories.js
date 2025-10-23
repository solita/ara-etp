import EnergianHinta from './energian-hinta.svelte';

import * as empty from '@Pages/energiatodistus/empty';
import * as schema from '@Pages/energiatodistus/schema';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';
import * as R from 'ramda';

export default {
  title: 'Pages/Energiatodistus/PPP Energian Hinta',
  component: EnergianHinta
};

const Template = args => ({
  Component: EnergianHinta,
  props: args
});

export const Default = Template.bind({});
Default.args = {
  schema: schema.perusparannuspassi,
  perusparannuspassi: R.compose(
    R.assocPath(['tulokset', 'kaukolampo-hinta'], Either.Right(Maybe.Some(8.5))),
    R.assocPath(['tulokset', 'sahko-hinta'], Either.Right(Maybe.Some(15.2))),
    R.assocPath(['tulokset', 'uusiutuvat-pat-hinta'], Either.Right(Maybe.Some(6.0))),
    R.assocPath(['tulokset', 'fossiiliset-pat-hinta'], Either.Right(Maybe.Some(10.0))),
    R.assocPath(['tulokset', 'kaukojaahdytys-hinta'], Either.Right(Maybe.Some(5.5)))
  )(empty.perusparannuspassi(1)),
  disabled: false
};


