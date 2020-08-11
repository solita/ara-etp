<script>
  import * as R from 'ramda';

  import * as et from './energiatodistus-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as deep from '@Utility/deep-objects';
  import * as schemas from './schema';
  import { _ } from '@Language/i18n';

  import ET2018Form from './ET2018Form';
  import ET2013Form from './ET2013Form';
  import ToolBar from '@Component/ToolBar/ToolBar';
  import Button from '@Component/Button/Button';

  import { flashMessageStore } from '@/stores';

  export let version;
  export let energiatodistus;
  export let luokittelut;

  export let submit;
  export let disabled = false;
  export let title = '';

  let schema = schemas['v' + version];

  let inputLanguage = 'fi';

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

<style type="text/postcss">
  :global(.et-table) {
    @apply border-b-1 border-disabled pb-8;
  }

  :global(.et-table__noborder) {
    @apply border-b-0;
  }

  :global(.et-table--th),
  :global(.et-table--td) {
    @apply px-4 py-2 font-bold;
  }

  :global(.et-table--th) {
    @apply text-primary text-sm text-center w-1/5;
    height: 4em;
  }

  :global(.et-table--tr > .et-table--th:not(:first-child)),
  :global(.et-table--tr > .et-table--td:not(:first-child)) {
    @apply border-l-1 border-tableborder;
  }

  :global(.et-table--thead) {
    @apply bg-tertiary;
  }

  :global(.et-table--th__sixth) {
    @apply w-1/6;
  }

  :global(.et-table--th__fourcells) {
    @apply w-4/5;
  }

  :global(.et-table--th__threecells) {
    @apply w-3/5;
  }

  :global(.et-table--th__twocells) {
    @apply w-2/5;
  }

  :global(.et-table--td__fifth) {
    @apply w-1/5;
  }

  :global(.et-table--tr:last-child) {
    @apply mb-5;
  }

  :global(.et-table--tr > .et-table--td:first-child) {
    @apply font-bold;
  }

  :global(.et-table--tr > .et-table--td:not(:first-child)) {
    @apply text-center;
  }
</style>

{#if !R.isNil(ETForm)}

  <div class="w-full relative flex">
    <div class="w-5/6">
      <form on:submit|preventDefault={submit$}>
        <ETForm
          {title}
          bind:energiatodistus
          {inputLanguage}
          {disabled}
          {schema}
          {luokittelut} />
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
        energiatodistusKieli={energiatodistus.perustiedot.kieli}
        bind:inputLanguage
        {cancel}
        {version}
        id={Maybe.fromNull(energiatodistus.id)} />
    </div>
  </div>
{:else}
  <p>Energiatodistusversiota {version} ei ole olemassa.</p>
{/if}
