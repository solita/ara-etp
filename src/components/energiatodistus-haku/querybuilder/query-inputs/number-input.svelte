<script>
  import SimpleInput from '@Component/Input/SimpleInput';
  import * as parsers from '@Utility/parsers';
  import * as Either from '@Utility/either-utils';
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  export let value;
  export let nameprefix;
  export let index;

  let strValue = value + '';

  $: valid = R.compose(
    Either.isRight,
    parsers.parseNumber
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
      {$_('parsing.invalid-number')}
    </div>
  {/if}
</div>
