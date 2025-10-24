import Energiajarjestelmat from './energiajarjestelmat.svelte';

import * as empty from '@Pages/energiatodistus/empty.js';
import * as schema from '@Pages/energiatodistus/schema.js';
import * as R from 'ramda';

const requiredFields = [];

const saveSchema = R.compose(
  R.reduce(schema.assocRequired, R.__, requiredFields)
)(schema.perusparannuspassi);

export default {
  title: 'Pages/Energiatodistus/PPP energiajarjestelmat',
  component: Energiajarjestelmat
};

const Template = args => ({
  Component: Energiajarjestelmat,
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
  ),
  luokittelut: {
    lammitysmuoto: [],
    ilmanvaihtotyypit: [],
    uusiutuvaEnergia: [],
    jaahdytys: [],
    mahdollisuusLiittya: []
  },
  inputLanguage: 'fi',
  disabled: false
};
