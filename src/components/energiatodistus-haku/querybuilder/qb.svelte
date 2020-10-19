<script>
  import qs from 'qs';
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import Radio from '@Component/Radio/Radio';
  import Select from '@Component/Select/Select';
  import { _ } from '@Language/i18n';

  import * as Inputs from '@Component/Energiatodistus/inputs';
  import * as EtHakuUtils from '@Component/energiatodistus-haku/energiatodistus-haku-utils';

  import QueryInput from './qi';

  export let operator;
  export let key;
  export let values;
  export let index;
  export let schema;
</script>

<div class="flex-grow flex items-center justify-start">
  <div class="w-1/2 mr-4">
    <Select
      name={`${index}_key`}
      items={R.keys(schema)}
      bind:model={key}
      format={Inputs.propertyLabel($_)}
      parse={R.identity}
      lens={R.identity}
      allowNone={false} />
  </div>
  <div class="w-1/2">
    <QueryInput
      nameprefix={`${index}`}
      operation={operator}
      {values}
      operations={R.prop(key, schema)} />
  </div>
</div>
