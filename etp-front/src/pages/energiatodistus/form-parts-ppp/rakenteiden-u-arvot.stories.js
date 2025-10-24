import RakenteidenUArvot from './rakenteiden-u-arvot.svelte';

import * as empty from '@Pages/energiatodistus/empty.js';
import * as schema from '@Pages/energiatodistus/schema.js';
import * as R from 'ramda';

const requiredFields = [
  'passin-perustiedot.havainnointikaynti',
  'passin-perustiedot.passin-esittely'
];

const saveSchema = R.compose(
  R.reduce(schema.assocRequired, R.__, requiredFields)
)(schema.perusparannuspassi);

export default {
  title: 'Pages/Energiatodistus/PPP rakenteiden u-arvot',
  component: RakenteidenUArvot
};

const Template = args => ({
  Component: RakenteidenUArvot,
  props: args
});

const energiatodistus = empty.energiatodistus2018();

export const Default = Template.bind({});
Default.args = {
  schema: saveSchema,
  energiatodistus,
  perusparannuspassi: R.mergeDeepLeft(
    {
      id: 4245
    },
    empty.perusparannuspassi(442)
  )
};
