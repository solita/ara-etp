<script>
  import * as R from 'ramda';

  import { locale, _ } from '@Language/i18n';
  import * as Either from '@Utility/either-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as validation from '@Utility/validation';
  import * as parsers from '@Utility/parsers';
  import * as Future from '@Utility/future-utils';
  import * as et from './energiatodistus-utils';
  import * as LocaleUtils from '@Language/locale-utils';
  import * as api from './energiatodistus-api';

  import H1 from '@Component/H/H1';
  import H2 from '@Component/H/H2';
  import Select from '@Component/Select/Select';
  import HR from '@Component/HR/HR';

  import Input from './Input';
  import BasicInput from '@Component/Input/Input';

  import RakennuksenPerustiedot from './RakennuksenPerustiedot';
  import ToimenpideEhdotukset from './ToimenpideEhdotukset';

  import Rakennusvaippa from './form-parts/lahtotiedot/rakennusvaippa';
  import Ikkunat from './form-parts/lahtotiedot/ikkunat';
  import Ilmanvaihtojarjestelma from './form-parts/lahtotiedot/ilmanvaihtojarjestelma';
  import Lammitysjarjestelma from './form-parts/lahtotiedot/lammitysjarjestelma';
  import Jaahdytysjarjestelma from './form-parts/lahtotiedot/jaahdytysjarjestelma';
  import SisaisetLampokuormat from './form-parts/lahtotiedot/sisaiset-lampokuormat';
  import Lamminkayttovesi from './form-parts/lahtotiedot/lamminkayttovesi';

  import ELuvunErittely from './form-parts/tulokset/e-luvun-erittely';
  import UusiutuvatOmavaraisenergiat from './form-parts/tulokset/uusiutuvat-omavaraisenergiat';
  import TeknistenjarjestelmienEnergiankulutus from './form-parts/tulokset/teknistenjarjestelmien-energiankulutus';
  import Nettotarve from './form-parts/tulokset/nettotarve';
  import Lampokuormat from './form-parts/tulokset/lampokuormat';
  import Laskentatyokalu from './form-parts/tulokset/laskentatyokalu';

  import EnergiaverkostaOstetut from './form-parts/toteutunut-kulutus/energiaverkosta-ostetut';
  import OstetutPolttoaineet from './form-parts/toteutunut-kulutus/ostetut-polttoaineet';
  import ToteutunutOstoenergia from './form-parts/toteutunut-kulutus/toteutunut-ostoenergia';

  import Huomio from './form-parts/huomiot/huomio';
  import Suositukset from './form-parts/huomiot/suositukset';

  export let title = '';
  export let energiatodistus;
  export let schema;
  export let disabled = false;

  $: labelLocale = LocaleUtils.label($locale);

  let kielisyys = Either.Left('Not initialized');
  Future.fork(
    _ => {},
    result => (kielisyys = Either.Right(result)),
    api.kielisyys
  );

  let laatimisvaiheet = Either.Left('Not initialized');
  Future.fork(
    _ => {},
    result => (laatimisvaiheet = Either.Right(result)),
    api.laatimisvaiheet
  );

  let kayttotarkoitusluokat = Either.Left('Not initialized');
  Future.fork(
    _ => {},
    result => (kayttotarkoitusluokat = Either.Right(result)),
    api.kayttotarkoitusluokat2018
  );

  let alakayttotarkoitusluokat = Either.Left('Not initialized');
  Future.fork(
    _ => {},
    result => (alakayttotarkoitusluokat = Either.Right(result)),
    api.alakayttotarkoitusluokat2018
  );

  $: console.log(energiatodistus);
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
    @apply border-l-1 border-disabled;
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

<div class="w-full mt-3">
  <H1 text={title} />
  <div class="flex flex-col -mx-4">

    {#if R.complement(R.isNil)(energiatodistus['laatija-fullname'])}
      <div class="lg:w-1/2 w-full px-4 py-2">
        <BasicInput
          id="energiatodistus.laatija-fullname"
          name="energiatodistus.laatija-fullname"
          label={$_('energiatodistus.laatija-fullname')}
          disabled={true}
          format={Maybe.orSome('')}
          bind:model={energiatodistus}
          lens={R.lensProp('laatija-fullname')}
          i18n={$_} />
      </div>
    {/if}

    <div class="lg:w-1/2 w-full px-4 py-4">
      <Input
        {disabled}
        {schema}
        bind:model={energiatodistus}
        path={['perustiedot', 'yritys', 'nimi']} />
    </div>

    <div class="lg:w-1/2 w-full px-4 py-4">
      <Input
        {disabled}
        {schema}
        bind:model={energiatodistus}
        path={['perustiedot', 'tilaaja']} />
    </div>

    <div class="lg:w-1/2 w-full px-4 py-4">
      <Select
        label={$_('energiatodistus.perustiedot.kieli')}
        required={false}
        {disabled}
        bind:model={energiatodistus}
        lens={R.lensPath(['perustiedot', 'kieli'])}
        parse={Maybe.Some}
        format={et.selectFormat(labelLocale, kielisyys)}
        items={Either.foldRight([], R.pluck('id'), kielisyys)} />
    </div>

    <div class="lg:w-1/2 w-full px-4 py-4">
      <Select
        label={$_('energiatodistus.perustiedot.laatimisvaihe')}
        required={false}
        {disabled}
        bind:model={energiatodistus}
        lens={R.lensPath(['perustiedot', 'laatimisvaihe'])}
        parse={Maybe.Some}
        format={et.selectFormat(labelLocale, laatimisvaiheet)}
        items={Either.foldRight([], R.pluck('id'), laatimisvaiheet)} />
    </div>
  </div>

  <HR />

  <RakennuksenPerustiedot
    {schema}
    {disabled}
    bind:energiatodistus
    {labelLocale}
    {kayttotarkoitusluokat}
    {alakayttotarkoitusluokat} />

  <HR />
  <ToimenpideEhdotukset {disabled} {schema} bind:energiatodistus />

  <HR />
  <H2 text="Lähtötiedot" />

  <div class="w-1/5 py-4 mb-4">
    <Input
      {disabled}
      {schema}
      bind:model={energiatodistus}
      path={['lahtotiedot', 'lammitetty-nettoala']} />
  </div>

  <Rakennusvaippa {disabled} {schema} bind:energiatodistus />
  <Ikkunat {disabled} {schema} bind:energiatodistus />
  <Ilmanvaihtojarjestelma {disabled} {schema} bind:energiatodistus />
  <Lammitysjarjestelma {disabled} {schema} bind:energiatodistus />
  <Jaahdytysjarjestelma {disabled} {schema} bind:energiatodistus />
  <SisaisetLampokuormat {disabled} {schema} bind:energiatodistus />
  <Lamminkayttovesi {disabled} {schema} bind:energiatodistus />

  <HR />

  <H2 text={$_('energiatodistus.tulokset.header')} />
  <ELuvunErittely {disabled} {schema} bind:energiatodistus />
  <UusiutuvatOmavaraisenergiat {disabled} {schema} bind:energiatodistus />
  <TeknistenjarjestelmienEnergiankulutus
    {disabled}
    {schema}
    bind:energiatodistus />
  <Nettotarve {disabled} {schema} bind:energiatodistus />
  <Lampokuormat {disabled} {schema} bind:energiatodistus />
  <Laskentatyokalu {disabled} {schema} bind:energiatodistus />

  <HR />
  <H2 text={$_('energiatodistus.toteutunut-ostoenergiankulutus.header')} />
  <EnergiaverkostaOstetut {disabled} {schema} bind:energiatodistus />
  <OstetutPolttoaineet {disabled} {schema} bind:energiatodistus />
  <ToteutunutOstoenergia {disabled} {schema} bind:energiatodistus />

  <HR />
  <H2 text={$_('energiatodistus.huomiot.header')} />
  <Huomio {disabled} {schema} huomio={'ymparys'} bind:energiatodistus />
  <Huomio
    {disabled}
    {schema}
    huomio={'alapohja-ylapohja'}
    bind:energiatodistus />
  <Huomio {disabled} {schema} huomio={'lammitys'} bind:energiatodistus />
  <Huomio {disabled} {schema} huomio={'iv-ilmastointi'} bind:energiatodistus />
  <Huomio {disabled} {schema} huomio={'valaistus-muut'} bind:energiatodistus />
  <Suositukset {disabled} {schema} bind:energiatodistus />
</div>
