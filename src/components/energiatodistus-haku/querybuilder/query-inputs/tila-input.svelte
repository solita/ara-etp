<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  import * as EtUtils from '@Component/Energiatodistus/energiatodistus-utils';

  import Select from '@Component/Select/Select';

  export let values = [...R.values(EtUtils.tila)];
  export let nameprefix;
  export let index = 0;
  export let value = R.head(values);
</script>

<Select
  name={`${nameprefix}_value_${index}`}
  allowNone={false}
  model={value}
  format={R.compose(
    $_,
    R.concat('energiatodistus.tila.'),
    R.nth(R.__, EtUtils.tilat)
  )}
  items={R.values(EtUtils.tila)}
  lens={R.identity} />
