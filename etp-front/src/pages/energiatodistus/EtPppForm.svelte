<script>
  // TODO: AE-2690: RENAME FILES TO kebab-case!
  import * as R from 'ramda';
  import { loc, replace } from 'svelte-spa-router';
  import { tick } from 'svelte';

  import * as et from './energiatodistus-utils';
  import * as EtUtils from './energiatodistus-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Validations from '@Utility/validation';
  import * as schemas from './schema';
  import { _ } from '@Language/i18n';
  import { pppRequired } from './perusparannuspassi-utils.js';

  import Signing from './signing/SigningDialog.svelte';
  import EnergiatodistusForm from './EnergiatodistusForm.svelte';
  import PppForm from './PPPForm.svelte';
  import * as EtValidations from './validation';
  import * as Inputs from './inputs';
  import * as Postinumero from '@Component/address/postinumero-fi';
  import * as Kayttajat from '@Utility/kayttajat';

  import ToolBar from './ToolBar/toolbar';
  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';

  import { announcementsForModule } from '@Utility/announce';
  import ET2018Form from './ET2018Form';
  import ET2013Form from './ET2013Form';

  export let config;

  export let version;
  export let valvonta;
  export let pppValidation;

  export let energiatodistus;
  export let luokittelut;
  export let validation;
  export let whoami;
  export let verkkolaskuoperaattorit;
  export let laskutusosoitteet;

  // TODO: AE-2690: Can we unify this and use the same one everywhere?
  export let perusparannuspassi;

  export let submit;
  export let title = '';

  const { announceError, clearAnnouncements } =
    announcementsForModule('Energiatodistus');

  const forms = {
    '2018': ET2018Form,
    '2013': ET2013Form,
    '2026': ET2018Form // Use ET2018Form for 2026 initially
  };
  const ETForm = forms[version];

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
  export let dirty = false;

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
        R.map(Inputs.propertyLabel($_, 'energiatodistus')),
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
      submit(energiatodistus, perusparannuspassi, (...args) => {
        dirty = false;
        onSuccessfulSave(...args);
      });
    } else {
      showKorvausErrorMessage(korvausError);
      showInvalidPropertiesMessage(invalid);
    }
  };

  export const showMissingProperties = (missing, i18nRoot) => {
    return R.compose(
      R.join(', '),
      R.map(Inputs.propertyLabel($_, i18nRoot))
    )(missing);
  };

  export const showError = (allMissingFields, allMissing) => {
    announceError(
      $_('energiatodistus.messages.validation-required-error') +
        allMissingFields
    );
    Inputs.scrollIntoView(document, allMissing[0]);
  };

  let etFormElement;
  const validateCompleteAndSubmit = onSuccessfulSave => () => {
    const missing = EtValidations.missingProperties(
      required(energiatodistus),
      energiatodistus
    );

    const missingPPP = [];

    // TODO: AE-2690: Should this depend on perusparannuspassi.valid instead?
    if (perusparannuspassi && perusparannuspassi.id) {
      missingPPP.push(
        ...EtValidations.missingProperties(
          pppRequired(
            perusparannuspassi,
            pppValidation,
            energiatodistus['bypass-validation-limits']
          ),
          R.assocPath(
            ['perustiedot', 'kieli'],
            energiatodistus.perustiedot.kieli,
            perusparannuspassi
          )
        )
      );
    }

    const allMissing = [...missing, ...missingPPP];

    if (R.isEmpty(allMissing)) {
      validateAndSubmit(onSuccessfulSave)();
    } else {
      const missingETFields = showMissingProperties(missing, 'energiatodistus');
      const missingPPPFields = showMissingProperties(
        missingPPP,
        'perusparannuspassi'
      );

      const allMissingFields = missingETFields + ', ' + missingPPPFields;
      showError(allMissingFields, allMissing);
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

{#if R.isNotNil(ETForm)}
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
        <EnergiatodistusForm
          {ETForm}
          {version}
          {luokittelut}
          {whoami}
          {validation}
          {laskutusosoitteet}
          {verkkolaskuoperaattorit}
          {title}
          {disabled}
          {disabledForPaakayttaja}
          {inputLanguage}
          bind:energiatodistus
          bind:eTehokkuus
          bind:dirty
          bind:korvausError />
        <PppForm
          {energiatodistus}
          {inputLanguage}
          {luokittelut}
          schema={schemas.perusparannuspassi}
          {pppValidation}
          bind:perusparannuspassi />
      </form>
    </div>
  </div>
{:else}
  <p>Energiatodistusversiota {version} ei ole olemassa.</p>
{/if}

{#if R.propEq(et.tila['in-signing'], 'tila-id', energiatodistus)}
  <Signing {energiatodistus} reload={reset} />
{/if}
