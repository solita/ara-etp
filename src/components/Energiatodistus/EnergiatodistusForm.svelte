<script>
  import * as R from 'ramda';

  import * as et from './energiatodistus-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as deep from '@Utility/deep-objects';
  import { schema as schema2018 } from './schema-2018';
  import { schema as schema2013 } from './schema-2013';
  import { _ } from '@Language/i18n';

  import ET2018Form from './ET2018Form';
  import ET2013Form from './ET2013Form';
  import ToolBar from '@Component/ToolBar/ToolBar';
  import Button from '@Component/Button/Button';

  import { flashMessageStore } from '@/stores';

  export let version;
  export let energiatodistus;

  export let submit;
  export let disabled = false;
  export let title = '';

  const schemas = {
    '2018': schema2018,
    '2013': schema2013
  };
  let schema = schemas[version];

  const forms = {
    '2018': ET2018Form,
    '2013': ET2013Form
  };
  const ETForm = forms[version];

  $: submit$ = _ => {
    if (et.isValidForm(et.validators(schema), energiatodistus)) {
      flashMessageStore.flush();
      submit(energiatodistus);
    } else {
      flashMessageStore.add(
        'Energiatodistus',
        'error',
        $_('energiatodistus.messages.validation-error')
      );
    }
  };

  const cancel = event => {
    event.preventDefault();
    window.location.reload();
  };
</script>

{#if !R.isNil(ETForm)}

  <div class="w-full relative flex">
    <div class="w-5/6">
      <form on:submit|preventDefault={submit$}>
        <ETForm {title} bind:energiatodistus {disabled} {schema} />
        <div class="flex -mx-4 pt-8">
          <div class="px-4">
            <Button type={'submit'} text={$_('tallenna')} />
          </div>
          <div class="px-4">
            <Button
              on:click={cancel}
              text={$_('peruuta')}
              type={'reset'}
              style={'secondary'} />
          </div>
        </div>
      </form>
    </div>
    <div class="sticky top-3em w-1/6 self-start flex justify-end">
      <ToolBar
        save={submit$}
        {cancel}
        {version}
        id={Maybe.fromNull(energiatodistus.id)} />
    </div>
  </div>
{:else}
  <p>Energiatodistusversiota {version} ei ole olemassa.</p>
{/if}
