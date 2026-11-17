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
  luokittelut: {
    a0Merkinta: [
      { id: 0, 'label-fi': 'Ei', 'label-sv': 'Ei (sv)', valid: true },
      { id: 1, 'label-fi': 'A0', 'label-sv': 'A0', valid: true },
      {
        id: 2,
        'label-fi': 'A0_korjattu',
        'label-sv': 'A0_korjattu',
        valid: true
      },
      { id: 3, 'label-fi': 'A0_MPR', 'label-sv': 'A0_MPR', valid: true }
    ]
  },
  perusparannuspassi: R.mergeDeepLeft(
    {
      id: 4245
    },
    empty.perusparannuspassi(442)
  )
};
