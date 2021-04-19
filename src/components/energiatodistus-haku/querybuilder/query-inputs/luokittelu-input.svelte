<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import { _, locale } from '@Language/i18n';
  import * as LocaleUtils from '@Language/locale-utils';
  import Select from '@Component/Select/Select';

  export let nameprefix;
  export let index = 0;
  export let luokittelut = [];
  export let luokittelu;

  $: labelLocale = LocaleUtils.label($locale);

  $: selectedLuokittelu = R.prop(luokittelu, luokittelut);
  $: values = R.map(R.prop('id'), selectedLuokittelu);
  $: value = R.head(values);
</script>

<Select
  name={`${nameprefix}_value_${index}`}
  allowNone={false}
  model={value}
  format={R.compose(
    labelLocale,
    R.find(R.__, selectedLuokittelu),
    R.propEq('id')
  )}
  items={values}
  lens={R.identity} />
