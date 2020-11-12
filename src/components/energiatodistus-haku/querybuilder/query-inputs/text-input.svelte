<script>
  import SimpleInput from '@Component/Input/SimpleInput';
  import * as parsers from '@Utility/parsers';
  import * as Either from '@Utility/either-utils';
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  export let values = [];
  export let nameprefix;
  export let index = 0;
  export let value = R.head(values);

  if (value === 'true' || value === 'false') {
    value = '';
  }

  let strValue = value + '';

  $: valid = R.compose(
    R.lt(0),
    R.length
  )(strValue);
</script>

<style type="text/postcss">
  .validation-label {
    @apply absolute top-auto;
    font-size: smaller;
  }
</style>

<div>
  <SimpleInput
    {valid}
    validationResult={{ type: 'error' }}
    center={true}
    bind:rawValue={strValue}
    rawValueAsViewValue={true}
    name={`${nameprefix}_value_${index}`} />
  {#if !valid}
    <div class="validation-label">
      <span class="font-icon text-error">error</span>
      {$_('validation.required')}
    </div>
  {/if}
</div>
