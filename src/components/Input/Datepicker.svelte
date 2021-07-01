<script>
  import * as R from 'ramda';
  import * as Formats from '@Utility/formats';
  import * as Parsers from '@Utility/parsers';
  import { litepicker } from '@Component/Datepicker/litepicker';

  import { locale } from '@Language/i18n';
  import Input from './Input';

  export let id;
  export let name;
  export let label = '';

  export let caret = false;
  export let search = false;
  export let required = false;
  export let autocomplete = 'off';
  export let disabled = false;
  export let compact = false;
  export let center = false;

  export let model;
  export let lens;
  export let parse = Parsers.parseDate;
  export let format = Formats.formatDateInstant;

  export let validators = [];
  export let warnValidators = [];

  export let labelUnit;
  export let i18n;

  export let transform = R.identity;

  $: inputProps = {
    id,
    name,
    label,
    caret,
    search,
    required,
    autocomplete,
    disabled,
    compact,
    center,
    lens,
    format,
    parse,
    validators,
    warnValidators,
    i18n,
    labelUnit
  };
</script>

<style type="text/postcss">
</style>

{#if !disabled}
  <div
    use:litepicker={{
      lang: $locale,
      update: date => {
        model = R.set(lens, transform(date), model);
      }
    }}>
    <Input bind:model {...inputProps} />
  </div>
{:else}
  <div>
    <Input bind:model {...inputProps} />
  </div>
{/if}
