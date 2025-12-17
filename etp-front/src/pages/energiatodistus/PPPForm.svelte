<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import * as schemas from './schema';

  import H2 from '@Component/H/H2';
  import H3 from '@Component/H/H3';
  import HR from '@Component/HR/HR';
  import TextButton from '@Component/Button/TextButton';
  import * as Maybe from '@Utility/maybe-utils';
  import * as empty from './empty.js';

  import Perustiedot from './form-parts-ppp/ppp-perustiedot';
  import RakenteidenUArvot from './form-parts-ppp/rakenteiden-u-arvot';
  import Energiajarjestelmat from './form-parts-ppp/energiajarjestelmat';
  import Vaiheistus from './form-parts-ppp/laskennan-tulokset-vaiheistus';
  import LaskennallinenOstoenergia from './form-parts-ppp/laskennan-tulokset-laskennallinen-ostoenergia';
  import UusiutuvaEnergia from './form-parts-ppp/laskennan-tulokset-uusiutuva-energia';
  import ToteutunutOstoenergia from './form-parts-ppp/laskennan-tulokset-toteutunut-ostoenergia';
  import EnergianHinta from './form-parts-ppp/laskennan-tulokset-energian-hinta';
  import LaskennallinenEnergiaKustannukset from './form-parts-ppp/laskennan-tulokset-kustannukset-laskennallinen-ostoenergia';
  import ToteutunutOstoenergiaKustannukset from './form-parts-ppp/laskennan-tulokset-kustannukset-toteutunut-ostoenergia';
  import Toimenpiteet from './form-parts-ppp/toimenpiteet';

  export let energiatodistus;
  export let inputLanguage;
  export let luokittelut;
  export let schema;
  export let pppValidation;
  export let dirty;

  export let perusparannuspassi;

  const onAddPPP = () => {
    perusparannuspassi = R.assoc('valid', true, perusparannuspassi);
    dirty = true;
  };

  const onDeletePPP = () => {
    perusparannuspassi = R.assoc('valid', false, perusparannuspassi);
    dirty = true;
  };

  const required = perusparannuspassi =>
    perusparannuspassi['bypass-validation-limits']
      ? pppValidation.requiredBypass
      : pppValidation.requiredAll;

  const saveSchema = R.compose(
    R.reduce(schemas.assocRequired, R.__, required(perusparannuspassi)),
    schema =>
      perusparannuspassi['bypass-validation-limits']
        ? schema
        : R.reduce(
            schemas.redefineNumericValidation,
            schema,
            pppValidation.numeric
          )
  )(schemas.perusparannuspassi);

  const vaiheSchema = R.reduce(
    schemas.redefineNumericValidation,
    schemas.perusparannuspassi,
    pppValidation.vaiheNumeric
  );
</script>

<style>
  .ppp-section :global(h2) {
    margin-bottom: 0;
  }
</style>

<HR />
<div class="ppp-section flex flex-col gap-6">
  <div class="flex items-baseline justify-between">
    <H2
      id="perusparannuspassi"
      text={$_('energiatodistus.perusparannuspassi.header')} />
    {#if !perusparannuspassi.valid}
      <TextButton
        icon="add_circle_outline"
        text={$_('energiatodistus.perusparannuspassi.add-button')}
        type="button"
        largeIcon={true}
        on:click={onAddPPP} />
    {:else}
      <TextButton
        icon="delete_forever"
        text={$_('energiatodistus.perusparannuspassi.delete-button')}
        type="button"
        largeIcon={true}
        on:click={onDeletePPP} />
    {/if}
  </div>
  <div class="flex items-start items-center bg-tertiary p-4">
    <span class="mr-2 font-icon text-2xl">info_outline</span>
    <span>
      {$_(
        perusparannuspassi.valid
          ? 'energiatodistus.perusparannuspassi.info-text-when-PPP'
          : 'energiatodistus.perusparannuspassi.info-text'
      )}
    </span>
  </div>

  {#if !perusparannuspassi.valid}
    <p>
      {$_('energiatodistus.perusparannuspassi.not-added')}
    </p>
    <p>
      {$_('energiatodistus.perusparannuspassi.disclaimer')}
    </p>
  {:else}
    <p>
      {$_('energiatodistus.perusparannuspassi.disclaimer')}
    </p>
    <HR />
    <div class="flex flex-col gap-6">
      <H3 text={$_('perusparannuspassi.passin-perustiedot.header')} />

      <Perustiedot bind:perusparannuspassi schema={saveSchema} />

      <H3 text={$_('perusparannuspassi.rakennuksen-perustiedot.header')} />

      <RakenteidenUArvot
        bind:perusparannuspassi
        {energiatodistus}
        schema={saveSchema} />

      <Energiajarjestelmat
        bind:perusparannuspassi
        {energiatodistus}
        {inputLanguage}
        {luokittelut}
        {schema} />

      <H3 text={$_('perusparannuspassi.laskennan-tulokset.header')} />
      <Vaiheistus bind:perusparannuspassi schema={vaiheSchema} />

      <LaskennallinenOstoenergia
        bind:perusparannuspassi
        {energiatodistus}
        {schema} />
      <UusiutuvaEnergia bind:perusparannuspassi {energiatodistus} {schema} />
      <ToteutunutOstoenergia
        bind:perusparannuspassi
        {energiatodistus}
        {schema} />
      <EnergianHinta
        bind:perusparannuspassi
        {energiatodistus}
        schema={saveSchema} />

      <LaskennallinenEnergiaKustannukset
        bind:perusparannuspassi
        {energiatodistus}
        {schema} />

      <ToteutunutOstoenergiaKustannukset
        bind:perusparannuspassi
        {energiatodistus}
        {schema} />

      <Toimenpiteet
        {energiatodistus}
        bind:perusparannuspassi
        pppSchema={schema}
        toimenpideEhdotuksetLuokittelu={R.prop(
          'toimenpide-ehdotus',
          luokittelut
        )}
        {inputLanguage} />
    </div>
  {/if}
</div>
<HR />
