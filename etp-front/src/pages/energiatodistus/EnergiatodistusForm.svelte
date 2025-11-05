<script>
  import * as R from 'ramda';
  import { replace, loc } from 'svelte-spa-router';
  import { tick } from 'svelte';

  import * as et from './energiatodistus-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Formats from '@Utility/formats';
  import * as Validations from '@Utility/validation';
  import * as schemas from './schema';
  import { _ } from '@Language/i18n';

  import H1 from '@Component/H/H1';
  import H2 from '@Component/H/H2';
  import HR from '@Component/HR/HR';
  import Checkbox from '@Component/Checkbox/Checkbox';
  import PaakayttajanKommentti from './paakayttajan-kommentti';
  import EnergiatodistusKorvattu from './korvaavuus/korvattu';
  import EnergiatodistusKorvaava from './korvaavuus/korvaava';
  import Laskutus from './laskutus';

  import ET2018Form from './ET2018Form';
  import ET2013Form from './ET2013Form';
  import Signing from './signing/SigningDialog.svelte';
  import Input from './Input';

  import * as EtUtils from './energiatodistus-utils';
  import * as EtValidations from './validation';
  import * as Inputs from './inputs';
  import * as Postinumero from '@Component/address/postinumero-fi';
  import * as Kayttajat from '@Utility/kayttajat';

  import ToolBar from './ToolBar/toolbar';
  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';

  import { announcementsForModule } from '@Utility/announce';

  export let version;
  export let energiatodistus;
  export let luokittelut;
  export let whoami;
  export let validation;
  export let valvonta;
  export let laskutusosoitteet;
  export let verkkolaskuoperaattorit;

  export let submit;
  export let title = '';

  const { announceError, clearAnnouncements } =
    announcementsForModule('Energiatodistus');

  const required = energiatodistus =>
    energiatodistus['bypass-validation-limits']
      ? validation.requiredBypass
      : validation.requiredAll;

  const saveSchema = R.compose(
    R.reduce(schemas.assocRequired, R.__, required(energiatodistus)),
    schema =>
      energiatodistus['bypass-validation-limits']
        ? schema
        : R.reduce(
            schemas.redefineNumericValidation,
            schema,
            validation.numeric
          ),
    R.assocPath(
      ['perustiedot', 'postinumero'],
      Postinumero.Type(luokittelut.postinumerot)
    ),
    R.assoc(
      'laskutusosoite-id',
      schemas.EnumerationIdType(
        laskutusosoitteet,
        'energiatodistus.messages.invalid-laskutusosoite-id'
      )
    )
  )(schemas['v' + version]);

  const signatureSchema = schemas.appendRequiredValidators(
    R.assoc('$signature', true, saveSchema),
    isRequiredPredicate => isRequiredPredicate(inputLanguage)(energiatodistus)
  );

  let schema = saveSchema;

  let eTehokkuus = Maybe.None();
  let inputLanguage = 'fi';

  let korvausError = Maybe.None();
  let dirty = false;

  const forms = {
    '2018': ET2018Form,
    '2013': ET2013Form,
    '2026': ET2018Form // Use ET2018Form for 2026 initially
  };
  const ETForm = forms[version];

  $: disabled = !R.and(
    energiatodistus['laatija-id'].fold(true)(R.equals(whoami.id)),
    R.propEq(EtUtils.tila.draft, 'tila-id', energiatodistus)
  );

  $: disabledForPaakayttaja = !R.and(
    Kayttajat.isPaakayttaja(whoami),
    R.propEq(EtUtils.tila.draft, 'tila-id', energiatodistus)
  );

  const showInvalidPropertiesMessage = invalidProperties => {
    if (!R.isEmpty(invalidProperties)) {
      const invalidTxt = R.compose(
        R.join(', '),
        R.map(Inputs.propertyLabel($_)),
        R.map(R.nth(0))
      )(invalidProperties);

      announceError(
        $_('energiatodistus.messages.validation-error') + invalidTxt
      );

      Inputs.scrollIntoView(document, invalidProperties[0][0]);
    }
  };

  const showKorvausErrorMessage = R.forEach(
    R.compose(
      announceError,
      $_,
      R.concat('energiatodistus.korvaavuus.validation.')
    )
  );

  const validateAndSubmit = onSuccessfulSave => () => {
    const invalid = R.filter(
      R.propSatisfies(
        EtValidations.isValidationRequired(whoami, energiatodistus),
        0
      ),
      EtValidations.invalidProperties(saveSchema, energiatodistus)
    );

    if (R.isEmpty(invalid) && korvausError.isNone()) {
      clearAnnouncements();
      submit(energiatodistus, (...args) => {
        dirty = false;
        onSuccessfulSave(...args);
      });
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

    announceError(
      $_('energiatodistus.messages.validation-required-error') + missingTxt
    );

    Inputs.scrollIntoView(document, missing[0]);
  };

  let etFormElement;
  const validateCompleteAndSubmit = onSuccessfulSave => () => {
    const missing = EtValidations.missingProperties(
      required(energiatodistus),
      energiatodistus
    );
    if (R.isEmpty(missing)) {
      validateAndSubmit(onSuccessfulSave)();
    } else {
      showMissingProperties(missing);
      schema = signatureSchema;
      tick().then(_ => Validations.blurForm(etFormElement));
    }
  };

  const noop = () => {};
  const reset = _ => {
    dirty = false;
    replace(
      $loc.location +
        Maybe.fold('', R.concat('?', R.__), Maybe.fromEmpty($loc.querystring))
    );
  };
</script>

{#if !R.isNil(ETForm)}
  <DirtyConfirmation {dirty} />
  <div class="relative flex w-full">
    <div class="sticky top-3em z-10 flex justify-end self-start px-6">
      <ToolBar
        save={validateAndSubmit}
        saveComplete={validateCompleteAndSubmit}
        cancel={reset}
        {energiatodistus}
        {eTehokkuus}
        dirty={dirty || R.isNil(energiatodistus.id)}
        {whoami}
        {valvonta}
        bind:inputLanguage />
    </div>
    <div
      class="-mt-8 flex-grow overflow-x-hidden border-l border-disabled pl-10 pt-4">
      <form
        bind:this={etFormElement}
        on:submit|preventDefault={validateAndSubmit(noop)}
        on:input={_ => {
          dirty = true;
        }}
        on:change={_ => {
          dirty = true;
        }}
        on:reset={reset}>
        <div class="mt-3 w-full">
          <H1 text={title} />

          {#if version == 2026 && energiatodistus['perusparannuspassi-id'] && Maybe.isSome(energiatodistus['perusparannuspassi-id'])}
            <!-- Just for local development, wip indicator -->
            <div class="mb-3">
              <H1 text="PPP - Lisätty" />
            </div>
          {/if}

          {#if EtUtils.isSigned(energiatodistus)}
            <div class="mb-5">
              Voimassa:
              {Maybe.fold(
                '',
                Formats.inclusiveStartDate,
                energiatodistus.allekirjoitusaika
              )} -
              {Maybe.fold(
                '',
                Formats.inclusiveEndDate,
                energiatodistus['voimassaolo-paattymisaika']
              )}
            </div>
          {/if}
          <div class="perustiedot__container mb-12">
            <H2
              id="perustiedot"
              text={$_('energiatodistus.perustiedot-header')} />
            <div class="mb-5">
              <Checkbox
                bind:model={energiatodistus}
                lens={R.lensPath(['draft-visible-to-paakayttaja'])}
                label={$_('energiatodistus.draft-visible-to-paakayttaja')}
                {disabled} />
            </div>

            {#if R.or(energiatodistus['bypass-validation-limits'], Kayttajat.isPaakayttaja(whoami))}
              <div class="mb-5">
                <Checkbox
                  bind:model={energiatodistus}
                  lens={R.lensPath(['bypass-validation-limits'])}
                  label={$_('energiatodistus.bypass-validation-limits')}
                  disabled={disabledForPaakayttaja} />
              </div>
            {/if}

            {#if energiatodistus['bypass-validation-limits']}
              <div class="mb-5">
                <Input
                  disabled={disabledForPaakayttaja}
                  {schema}
                  center={false}
                  bind:model={energiatodistus}
                  path={['bypass-validation-limits-reason']} />
              </div>
            {/if}

            <PaakayttajanKommentti
              {whoami}
              {schema}
              path={['kommentti']}
              bind:model={energiatodistus} />

            <EnergiatodistusKorvaava
              postinumerot={luokittelut.postinumerot}
              {whoami}
              korvaavaEnergiatodistusId={energiatodistus[
                'korvaava-energiatodistus-id'
              ]} />
            <EnergiatodistusKorvattu
              bind:energiatodistus
              bind:dirty
              {whoami}
              postinumerot={luokittelut.postinumerot}
              bind:error={korvausError} />
          </div>

          <Laskutus
            {schema}
            {whoami}
            bind:energiatodistus
            {verkkolaskuoperaattorit}
            {laskutusosoitteet} />
          <ETForm
            bind:energiatodistus
            bind:eTehokkuus
            {inputLanguage}
            {disabled}
            {schema}
            {luokittelut}
            {validation}
            {whoami} />
        </div>
      </form>
      <!-- Slot for PPP form -->
      <!-- Todo: better placement, separate toolbar from this view too-->
      <slot />
    </div>
  </div>
{:else}
  <p>Energiatodistusversiota {version} ei ole olemassa.</p>
{/if}

{#if R.propEq(et.tila['in-signing'], 'tila-id', energiatodistus)}
  <Signing {energiatodistus} reload={reset} />
{/if}
