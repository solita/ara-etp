<script>
  import * as R from 'ramda';
  import Radio from '@Component/Radio/Radio';
  import { _ } from '@Language/i18n';

  export let values;
  export let nameprefix;
  export let index = 0;
  export let value = R.head(values);

  let strValue = value + '' === 'true' ? 'true' : 'false';
</script>

<div class="flex justify-between">
  <Radio
    bind:group={strValue}
    value={'true'}
    label={$_('energiatodistus.haku.true')}
    name={`${nameprefix}_value_${index}`} />
  <Radio
    bind:group={strValue}
    value={'false'}
    label={$_('energiatodistus.haku.false')}
    name={`${nameprefix}_value_${index}`} />
</div>
