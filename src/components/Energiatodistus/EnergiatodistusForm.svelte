<script>
  import * as R from 'ramda';

  import * as et from './energiatodistus-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as schemas from './schema';
  import { _ } from '@Language/i18n';
  import * as localstorage from './local-storage';

  import ET2018Form from './ET2018Form';
  import ET2013Form from './ET2013Form';
  import * as EtUtils from './energiatodistus-utils';
  import * as Validations from './validation';
  import * as Inputs from './inputs';

  import ToolBar from '@Component/ToolBar/ToolBar';
  import Button from '@Component/Button/Button';

  import { flashMessageStore } from '@/stores';

  export let version;
  export let energiatodistus;
  export let luokittelut;
  export let whoami;
  export let validation;

  export let submit;
  export let title = '';

  let schema = R.reduce(
    schemas.redefineNumericValidation,
    schemas['v' + version],
    validation.numeric
  );

  let inputLanguage = 'fi';

  const forms = {
    '2018': ET2018Form,
    '2013': ET2013Form
  };
  const ETForm = forms[version];

  $: disabled = !R.and(
    energiatodistus['laatija-id'].fold(true)(R.equals(whoami.id)),
    R.propEq('tila-id', EtUtils.tila.draft, energiatodistus)
  );

  const validateAndSubmit = onSuccessfulSave => () => {
    if (et.isValidForm(et.validators(schema), energiatodistus)) {
      flashMessageStore.flush();
      submit(energiatodistus, onSuccessfulSave);
      if (energiatodistus['laatija-id'].map(R.equals(whoami.id)).orSome(true)) {
        localstorage.setDefaultLaskutettavaYritysId(
          energiatodistus['laskutettava-yritys-id']
        );
      }
    } else {
      flashMessageStore.add(
        'Energiatodistus',
        'error',
        $_('energiatodistus.messages.validation-error')
      );
    }
  };

  const language = R.compose(
    R.map(R.slice(-2, Infinity)),
    R.filter(R.either(R.endsWith('-fi'), R.endsWith('-sv'))),
    Maybe.Some);

  const validateCompleteAndSubmit= onSuccessfulSave => () => {
    const missing = Validations.missingProperties(validation.required, energiatodistus);
    if (R.isEmpty(missing)) {
      validateAndSubmit(onSuccessfulSave)();
    } else {
      const missingTxt = R.compose(
        R.join(', '),
        R.map(R.converge(
          R.partial(Inputs.label, [$_]),
          [language, R.split('.')])),
      )(missing);

      flashMessageStore.add(
        'Energiatodistus',
        'error',
        $_('energiatodistus.messages.validation-required-error') + missingTxt);

      Inputs.scrollIntoView(document, missing[0]);
    }
  }

  const noop = () => {};

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
      <form on:submit|preventDefault={validateAndSubmit(noop)}>
        <ETForm
          {title}
          bind:energiatodistus
          {inputLanguage}
          {disabled}
          {schema}
          {luokittelut}
          {whoami} />
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
        save={validateAndSubmit}
        saveComplete={validateCompleteAndSubmit}
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
