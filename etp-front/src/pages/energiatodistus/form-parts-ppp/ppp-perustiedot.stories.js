import PppPerustiedot from './ppp-perustiedot.svelte';

import * as empty from '@Pages/energiatodistus/empty';
import * as schema from '@Pages/energiatodistus/schema';
import * as Maybe from '@Utility/maybe-utils';
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
  title: 'Pages/Energiatodistus/PPP Perustiedot',
  component: PppPerustiedot
};

const Template = args => ({
  Component: PppPerustiedot,
  props: args
});

export const Default = Template.bind({});
Default.args = {
  schema: saveSchema,
  perusparannuspassi: R.mergeDeepLeft(
    {
      id: 4245
    },
    empty.perusparannuspassi(442)
  )
};
