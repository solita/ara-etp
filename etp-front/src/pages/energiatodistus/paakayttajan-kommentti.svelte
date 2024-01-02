<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  import * as KayttajaUtils from '@Utility/kayttajat';
  import Textarea from '@Component/Textarea/Textarea';
  import * as inputs from './inputs';
  import * as formats from '@Utility/formats';

  import HR from '@Component/HR/HR';

  export let whoami;
  export let model;
  export let schema;
  export let path;

  const type = inputs.type(schema, path);
  const format = formats.optionalString;
  const id = inputs.id(path);
</script>

{#if KayttajaUtils.isPaakayttaja(whoami)}
  <div class="w-full">
    <Textarea
      {id}
      disabled={false}
      name={id}
      label={$_(['energiatodistus', ...path].join('.'))}
      lens={R.lensPath(path)}
      {schema}
      bind:model
      parse={type.parse}
      format={type.format}
      validators={type.validators}
      i18n={$_} />
  </div>
  <HR />
{/if}
