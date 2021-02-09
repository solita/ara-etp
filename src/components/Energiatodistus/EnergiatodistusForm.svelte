<script>
  import * as R from 'ramda';
  import { replace, loc } from 'svelte-spa-router';

  import * as et from './energiatodistus-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Formats from '@Utility/formats';
  import * as schemas from './schema';
  import { locale, _ } from '@Language/i18n';
  import * as localstorage from './local-storage';

  import H1 from '@Component/H/H1';
  import H2 from '@Component/H/H2';
  import HR from '@Component/HR/HR';
  import PaakayttajanKommentti from './paakayttajan-kommentti';
  import EnergiatodistusKorvattu from './korvaavuus/korvattu';
  import EnergiatodistusKorvaava from './korvaavuus/korvaava';
  import Laskutus from './laskutus';

  import ET2018Form from './ET2018Form';
  import ET2013Form from './ET2013Form';
  import Signing from './signing';
  import * as EtUtils from './energiatodistus-utils';
  import * as Validations from './validation';
  import * as Inputs from './inputs';
  import * as Postinumero from './postinumero';

  import ToolBar from '@Component/ToolBar/ToolBar';
  import Button from '@Component/Button/Button';
  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';

  import { flashMessageStore } from '@/stores';

  export let version;
  export let energiatodistus;
  export let luokittelut;
  export let whoami;
  export let validation;

  export let submit;
  export let title = '';

  let schema = R.compose(
    R.reduce(schemas.assocRequired, R.__,
      validation.required),
    R.reduce(schemas.redefineNumericValidation, R.__,
      validation.numeric),
    R.assocPath(['perustiedot', 'postinumero'],
      Postinumero.Type(luokittelut.postinumerot))
  )(schemas['v' + version]);

  let eTehokkuus = Maybe.None();
  let inputLanguage = 'fi';

  let korvausError = Maybe.None();
  let dirty = false;

  const forms = {
    '2018': ET2018Form,
    '2013': ET2013Form
  };
  const ETForm = forms[version];

  $: disabled = !R.and(
    energiatodistus['laatija-id'].fold(true)(R.equals(whoami.id)),
    R.propEq('tila-id', EtUtils.tila.draft, energiatodistus)
  );

  const showInvalidPropertiesMessage = invalidProperties => {
    if (!R.isEmpty(invalidProperties)) {
      const invalidTxt = R.compose(
        R.join(', '),
        R.map(Inputs.propertyLabel($_)),
        R.map(R.nth(0))
      )(invalidProperties);

      flashMessageStore.add(
        'Energiatodistus',
        'error',
        $_('energiatodistus.messages.validation-error') + invalidTxt
      );

      Inputs.scrollIntoView(document, invalidProperties[0][0]);
    }
  };

  const showKorvausErrorMessage = R.forEach(
    R.compose(
      flashMessageStore.add('Energiatodistus', 'error'),
      $_,
      R.concat('energiatodistus.korvaavuus.validation.')
    )
  );

  const validateAndSubmit = onSuccessfulSave => () => {
    const invalid = Validations.invalidProperties(schema, energiatodistus);
    if (R.isEmpty(invalid) && korvausError.isNone()) {
      flashMessageStore.flush();
      submit(energiatodistus, (...args) => {
        dirty = false;
        onSuccessfulSave(...args);
      });
      if (energiatodistus['laatija-id'].map(R.equals(whoami.id)).orSome(true)) {
        localstorage.setDefaultLaskutettavaYritysId(
          energiatodistus['laskutettava-yritys-id']
        );
      }
    } else {
      showKorvausErrorMessage(korvausError);
      showInvalidPropertiesMessage(invalid);
    }
  };

  const language = R.compose(
    R.map(R.slice(-2, Infinity)),
    R.filter(R.either(R.endsWith('-fi'), R.endsWith('-sv'))),
    Maybe.Some
  );

  export const showMissingProperties = missing => {
    const missingTxt = R.compose(
      R.join(', '),
      R.map(Inputs.propertyLabel($_))
    )(missing);

    flashMessageStore.add(
      'Energiatodistus',
      'error',
      $_('energiatodistus.messages.validation-required-error') + missingTxt
    );

    Inputs.scrollIntoView(document, missing[0]);
  }

  const validateCompleteAndSubmit = onSuccessfulSave => () => {
    const missing = Validations.missingProperties(
      validation.required,
      energiatodistus
    );
    if (R.isEmpty(missing)) {
      validateAndSubmit(onSuccessfulSave)();
    } else {
      showMissingProperties(missing);
    }
  };

  const noop = () => {};
  const reset = _ => {
    dirty = false;
    replace($loc.location +
      Maybe.fold('', R.concat('?', R.__), Maybe.fromEmpty($loc.querystring)));
  }
</script>

<style type="text/postcss">
  :global(.et-table) {
    @apply border-b-1 border-disabled pb-8 table-fixed w-full overflow-x-auto;
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
  {#if R.propEq('tila-id', et.tila['in-signing'], energiatodistus)}
    <Signing {energiatodistus} reload={reset} />
  {/if}

  <DirtyConfirmation {dirty} />

  <div class="w-full relative flex">
    <div class="w-5/6">
      <form
        on:submit|preventDefault={validateAndSubmit(noop)}
        on:input={_ => { dirty = true; }}
        on:change={_ => { dirty = true; }}
        on:reset={reset}>
        <div class="w-full mt-3">
          <H1 text={title} />

          {#if EtUtils.isSigned(energiatodistus)}
            <div class="mb-5">Voimassa:
              {Maybe.fold('', Formats.inclusiveStartDate, energiatodistus.allekirjoitusaika)} -
              {Maybe.fold('', Formats.inclusiveEndDate, energiatodistus['voimassaolo-paattymisaika'])}</div>
          {/if}

          <PaakayttajanKommentti
            {whoami}
            {schema}
            path={['kommentti']}
            bind:model={energiatodistus} />

          <H2 text={$_('energiatodistus.korvaavuus.header.korvaavuus')} />
          <EnergiatodistusKorvaava
            postinumerot={luokittelut.postinumerot}
            korvaavaEnergiatodistusId={energiatodistus['korvaava-energiatodistus-id']} />
          <EnergiatodistusKorvattu
              bind:energiatodistus
              {whoami}
              postinumerot={luokittelut.postinumerot}
              bind:error={korvausError} />
          <HR />

          <Laskutus {schema} {whoami} bind:energiatodistus />
          <ETForm
            bind:energiatodistus
            bind:eTehokkuus
            {inputLanguage}
            {disabled}
            {schema}
            {luokittelut}
            {validation} />
        </div>
        <div class="flex -mx-4 pt-8">
          <div class="px-4">
            <Button type={'submit'} text={$_('tallenna')} disabled={!dirty} />
          </div>
          <div class="px-4">
            <Button text={$_('peruuta')} type={'reset'} style={'secondary'} disabled={!dirty} />
          </div>
        </div>
      </form>
    </div>
    <div class="sticky top-3em w-1/6 self-start flex justify-end">
      <ToolBar
        save={validateAndSubmit}
        saveComplete={validateCompleteAndSubmit}
        cancel={reset}
        {energiatodistus}
        {eTehokkuus}
        dirty={dirty || R.isNil(energiatodistus.id)}
        {whoami}
        bind:inputLanguage />
    </div>
  </div>
{:else}
  <p>Energiatodistusversiota {version} ei ole olemassa.</p>
{/if}
