<script>
  import * as R from 'ramda';

  import * as EtUtils from './energiatodistus-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Formats from '@Utility/formats';
  import * as schemas from './schema';
  import { _ } from '@Language/i18n';

  import H1 from '@Component/H/H1';
  import H2 from '@Component/H/H2';
  import Checkbox from '@Component/Checkbox/Checkbox';
  import PaakayttajanKommentti from './paakayttajan-kommentti';
  import EnergiatodistusKorvattu from './korvaavuus/korvattu';
  import EnergiatodistusKorvaava from './korvaavuus/korvaava';
  import Laskutus from './laskutus';

  import Input from './Input';
  import * as Postinumero from '@Component/address/postinumero-fi';
  import * as Kayttajat from '@Utility/kayttajat';

  export let version;
  export let energiatodistus;
  export let luokittelut;
  export let whoami;
  export let validation;
  export let laskutusosoitteet;
  export let verkkolaskuoperaattorit;
  export let title = '';

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

  let schema = saveSchema;

  export let eTehokkuus;
  export let inputLanguage;

  export let korvausError;
  export let dirty = false;

  export let ETForm;

  export let disabled;
  export let disabledForPaakayttaja;
</script>

<div class="mt-3 w-full">
  <H1 text={title} />

  {#if EtUtils.isSigned(energiatodistus)}
    <div class="mb-5">
      {$_('energiatodistus.tila.signed')}:
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
    <H2 id="perustiedot" text={$_('energiatodistus.perustiedot-header')} />
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
