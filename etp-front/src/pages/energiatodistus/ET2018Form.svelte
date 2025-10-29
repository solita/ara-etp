<script>
  import * as R from 'ramda';

  import { locale, _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as et from './energiatodistus-utils';
  import * as Laatimisvaiheet from './laatimisvaiheet';
  import * as LocaleUtils from '@Language/locale-utils';
  import * as Empty from './empty';
  import * as Schema from './schema';
  import * as versionApi from '@Component/Version/version-api';
  import { isEtp2026Enabled } from '@Utility/config_utils.js';

  import H2 from '@Component/H/H2';
  import H3 from '@Component/H/H3';
  import Select from '@Component/Select/Select';
  import HR from '@Component/HR/HR';
  import TextButton from '@Component/Button/TextButton';

  import Input from './Input';
  import BasicInput from '@Component/Input/Input';
  import Textarea from './Textarea';

  import RakennuksenPerustiedot from './RakennuksenPerustiedot';
  import ToimenpideEhdotukset from './ToimenpideEhdotukset';
  import EnergiatodistusLaatija from './EnergiatodistusLaatija';

  import Rakennusvaippa from './form-parts/lahtotiedot/rakennusvaippa';
  import Ikkunat from './form-parts/lahtotiedot/ikkunat';
  import Ilmanvaihtojarjestelma from './form-parts/lahtotiedot/ilmanvaihtojarjestelma';
  import Lammitysjarjestelma from './form-parts/lahtotiedot/lammitysjarjestelma';
  import Jaahdytysjarjestelma from './form-parts/lahtotiedot/jaahdytysjarjestelma';
  import SisaisetLampokuormat from './form-parts/lahtotiedot/sisaiset-lampokuormat';
  import Lamminkayttovesi from './form-parts/lahtotiedot/lamminkayttovesi';

  import ELuku from './form-parts/tulokset/e-luku';
  import ELuvunErittely from './form-parts/tulokset/e-luvun-erittely';
  import UusiutuvatOmavaraisenergiat from './form-parts/tulokset/uusiutuvat-omavaraisenergiat';
  import TeknistenjarjestelmienEnergiankulutus from './form-parts/tulokset/teknistenjarjestelmien-energiankulutus';
  import Nettotarve from './form-parts/tulokset/nettotarve';
  import Lampokuormat from './form-parts/tulokset/lampokuormat';

  import EnergiaverkostaOstetut from './form-parts/toteutunut-kulutus/energiaverkosta-ostetut';
  import OstetutPolttoaineet from './form-parts/toteutunut-kulutus/ostetut-polttoaineet';
  import ToteutunutOstoenergia from './form-parts/toteutunut-kulutus/toteutunut-ostoenergia';

  import Huomio from './form-parts/huomiot/huomio';
  import Suositukset from './form-parts/huomiot/suositukset';

  import Area from './form-parts/units/area';
  import PPPForm from './ppp-form.svelte';

  export let energiatodistus;
  export let inputLanguage;
  export let luokittelut;
  export let schema;
  export let disabled = false;
  export let validation;
  export let eTehokkuus = Maybe.None();
  export let whoami;

  $: labelLocale = LocaleUtils.label($locale);

  // Load config directly (like laatijat.svelte pattern)
  let config = {};
  Future.fork(
    _ => {
      config = {}; // Fallback to empty config on error
    },
    loadedConfig => {
      config = loadedConfig;
    },
    versionApi.getConfig
  );

  // PPP state
  let perusparannuspassi = null;
  let showPPP = false;

  const addPPP = () => {
    if (!perusparannuspassi) {
      // Initialize PPP structure with energiatodistus ID
      perusparannuspassi = Empty.perusparannuspassi(energiatodistus.id);
      // Add id field using Maybe monad (will be set by backend when saved)
      perusparannuspassi.id = Maybe.None();
    }
    showPPP = true;
  };
</script>

<style>
  /* Min-height is bigger than other textarea so that the table of contents
  links are visible, even after scrolling to last textarea via anchor link*/
  .lisamerkintoja-textarea :global(textarea) {
    min-height: 400px;
    max-height: 600px;
  }

  /* Override H2 margin for PPP section */
  .ppp-section :global(h2) {
    margin-bottom: 0; /* 24px / mb-6 */
  }
</style>

<div class="mb-8">
  <H3 text={$_('energiatodistus.perustiedot.header')} />
  <div class="flex flex-col gap-x-8 lg:flex-row">
    {#if R.complement(R.isNil)(energiatodistus.id)}
      <div class="w-full py-2 lg:w-1/2">
        <BasicInput
          id="energiatodistus.id"
          name="energiatodistus.id"
          label={$_('energiatodistus.id')}
          disabled={true}
          bind:model={energiatodistus}
          lens={R.lensProp('id')}
          i18n={$_} />
      </div>
    {/if}
    {#if R.complement(R.isNil)(energiatodistus['laatija-fullname'])}
      <div class="w-full py-2 lg:w-1/2">
        <EnergiatodistusLaatija {whoami} {energiatodistus} />
      </div>
    {/if}
  </div>
  <div class="flex flex-col gap-x-8 lg:flex-row">
    <div class="w-full py-4 lg:w-1/2">
      <Input
        {disabled}
        {schema}
        center={false}
        bind:model={energiatodistus}
        path={['perustiedot', 'yritys', 'nimi']} />
    </div>

    <div class="w-full py-4 lg:w-1/2">
      <Input
        {disabled}
        {schema}
        center={false}
        bind:model={energiatodistus}
        path={['perustiedot', 'tilaaja']} />
    </div>
  </div>

  <div class="flex flex-col gap-x-8">
    <div class="w-full py-4 lg:w-1/2">
      <Select
        id={'perustiedot.kieli'}
        name={'perustiedot.kieli'}
        label={$_('energiatodistus.perustiedot.kieli')}
        required={true}
        validation={schema.$signature}
        {disabled}
        bind:model={energiatodistus}
        lens={R.lensPath(['perustiedot', 'kieli'])}
        allowNone={false}
        parse={Maybe.Some}
        format={et.selectFormat(labelLocale, luokittelut.kielisyys)}
        items={R.pluck('id', luokittelut.kielisyys)} />
    </div>

    <div class="w-full py-4 lg:w-1/2">
      <Select
        id={'perustiedot.laatimisvaihe'}
        label={$_('energiatodistus.perustiedot.laatimisvaihe')}
        required={true}
        validation={schema.$signature}
        {disabled}
        bind:model={energiatodistus}
        lens={R.lensPath(['perustiedot', 'laatimisvaihe'])}
        parse={Maybe.Some}
        format={et.selectFormat(labelLocale, luokittelut.laatimisvaiheet)}
        items={R.pluck('id', luokittelut.laatimisvaiheet)} />
    </div>
    {#if Laatimisvaiheet.isOlemassaOlevaRakennus(energiatodistus)}
      <div class="w-full py-4 lg:w-1/2">
        <Input
          {disabled}
          {schema}
          required={true}
          center={false}
          bind:model={energiatodistus}
          path={['perustiedot', 'havainnointikaynti']} />
      </div>
    {/if}

    <div class="w-full py-4 lg:w-1/2">
      <Input
        {disabled}
        {schema}
        center={false}
        bind:model={energiatodistus}
        path={['tulokset', 'laskentatyokalu']} />
    </div>
  </div>
</div>
<div class="mb-8">
  <RakennuksenPerustiedot
    {schema}
    {inputLanguage}
    {disabled}
    {whoami}
    bind:energiatodistus
    postinumerot={luokittelut.postinumerot}
    kayttotarkoitusluokat={luokittelut.kayttotarkoitusluokat}
    alakayttotarkoitusluokat={luokittelut.alakayttotarkoitusluokat} />
  <ELuku {eTehokkuus} idSuffix="perustiedot" />
</div>
<ToimenpideEhdotukset
  versio={'2018'}
  {disabled}
  {inputLanguage}
  {schema}
  bind:energiatodistus />

<HR />
<H2
  id="laskennan-lahtotiedot"
  text={$_('energiatodistus.lahtotiedot.header')} />

<div class="flex w-full">
  <div class="mb-12 w-1/3">
    <Input
      {disabled}
      {schema}
      center={false}
      bind:model={energiatodistus}
      path={['lahtotiedot', 'lammitetty-nettoala']}
      unit={Area} />
  </div>
</div>

<Rakennusvaippa {disabled} {schema} bind:energiatodistus />
<Ikkunat {disabled} {schema} bind:energiatodistus />
<Ilmanvaihtojarjestelma
  {disabled}
  {schema}
  bind:energiatodistus
  ilmanvaihtotyypit={luokittelut.ilmanvaihtotyypit}
  {inputLanguage} />

<Lammitysjarjestelma
  {disabled}
  {schema}
  bind:energiatodistus
  lammitysmuoto={luokittelut.lammitysmuoto}
  lammonjako={luokittelut.lammonjako}
  {inputLanguage} />
<Jaahdytysjarjestelma {disabled} {schema} bind:energiatodistus />
<Lamminkayttovesi {disabled} {schema} bind:energiatodistus />
<SisaisetLampokuormat
  {disabled}
  {schema}
  kuormat={validation.kuormat}
  alakayttotarkoitusluokat={luokittelut.alakayttotarkoitusluokat}
  bind:energiatodistus />

<HR />

<H2 id="tulokset" text={$_('energiatodistus.tulokset.header')} />

<ELuku {eTehokkuus} idSuffix="tulokset" />
<ELuvunErittely
  bind:eTehokkuus
  {disabled}
  {schema}
  bind:energiatodistus
  versio={2018} />
<UusiutuvatOmavaraisenergiat {disabled} {schema} bind:energiatodistus />
<TeknistenjarjestelmienEnergiankulutus
  {disabled}
  {schema}
  bind:energiatodistus />
<Nettotarve {disabled} {schema} bind:energiatodistus />
<Lampokuormat {disabled} {schema} bind:energiatodistus />

<HR />
<H2
  id="toteutunut-ostoenergiankulutus"
  text={$_('energiatodistus.toteutunut-ostoenergiankulutus.header')} />
<EnergiaverkostaOstetut
  versio={2018}
  {disabled}
  {schema}
  {inputLanguage}
  bind:energiatodistus />
<OstetutPolttoaineet {disabled} {schema} bind:energiatodistus />
<ToteutunutOstoenergia {disabled} {schema} bind:energiatodistus />

<HR />
<H2
  id="toimenpide-ehdotukset"
  text={$_('energiatodistus.huomiot.header.2018')} />
<Huomio
  {disabled}
  {schema}
  {inputLanguage}
  huomio={'ymparys'}
  bind:energiatodistus />
<Huomio
  {disabled}
  {schema}
  {inputLanguage}
  huomio={'alapohja-ylapohja'}
  bind:energiatodistus />
<Huomio
  {disabled}
  {schema}
  {inputLanguage}
  huomio={'lammitys'}
  bind:energiatodistus />
<Huomio
  {disabled}
  {schema}
  {inputLanguage}
  huomio={'iv-ilmastointi'}
  bind:energiatodistus />
<Huomio
  {disabled}
  {schema}
  {inputLanguage}
  huomio={'valaistus-muut'}
  bind:energiatodistus />

<Suositukset
  versio={2018}
  {disabled}
  {schema}
  {inputLanguage}
  bind:energiatodistus />

<HR />
<H2 id="lisamerkintoja" text={$_('energiatodistus.lisamerkintoja')} />
<div class="lisamerkintoja-textarea mb-4 w-full py-4">
  <Textarea
    {disabled}
    {schema}
    inputLanguage={Maybe.Some(inputLanguage)}
    bind:model={energiatodistus}
    path={['lisamerkintoja']} />
</div>

{#if isEtp2026Enabled(config)}
  <HR />
  <div class="flex flex-col gap-6 ppp-section">
    <div class="flex justify-between items-baseline">
      <H2
        id="perusparannuspassi"
        text={$_('energiatodistus.perusparannuspassi.header')} />
      <TextButton
        icon="add_circle_outline"
        text={$_('energiatodistus.perusparannuspassi.add-button')}
        type="button"
        on:click={addPPP} />
    </div>
    <div class="flex items-start p-4 bg-tertiary items-center">
      <span class="font-icon mr-2 text-2xl">info_outline</span>
      <span>{$_('energiatodistus.perusparannuspassi.info-text')}</span>
    </div>

    {#if !showPPP}
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
      <PPPForm
        {energiatodistus}
        {inputLanguage}
        {luokittelut}
        bind:perusparannuspassi
        schema={Schema.perusparannuspassi} />
    {/if}
  </div>
{/if}
<HR />
