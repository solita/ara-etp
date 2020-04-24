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
  import { schema } from './schema-2018';

  import H1 from '@Component/H/H1';
  import H2 from '@Component/H/H2';
  import H3 from '@Component/H/H3';

  import Button from '@Component/Button/Button';
  import Select from '@Component/Select/Select';
  import Checkbox from '@Component/Checkbox/Checkbox';

  import Input from './Input';

  import RakennuksenPerustiedot from './RakennuksenPerustiedot';
  import ToimenpideEhdotukset from './ToimenpideEhdotukset';
  import Rakennusvaippa from './FormParts/Lahtotiedot/Rakennusvaippa';
  import Ikkunat from './FormParts/Lahtotiedot/Ikkunat';
  import Ilmanvaihtojarjestelma from './FormParts/Lahtotiedot/Ilmanvaihtojarjestelma';
  import Lammitysjarjestelma from './FormParts/Lahtotiedot/Lammitysjarjestelma';
  import Jaahdytysjarjestelma from './FormParts/Lahtotiedot/Jaahdytysjarjestelma';
  import SisaisetLampokuormat from './FormParts/Lahtotiedot/SisaisetLampokuormat';
  import Lamminkayttovesi from './FormParts/Lahtotiedot/Lamminkayttovesi';

  import ELuvunErittely from './FormParts/Tulokset/ELuvunErittely';
  import UusiutuvatOmavaraisenergiat from './FormParts/Tulokset/UusiutuvatOmavaraisenergiat';
  import TeknistenjarjestelmienEnergiankulutus from './FormParts/Tulokset/TeknistenjarjestelmienEnergiankulutus';
  import Nettotarve from './FormParts/Tulokset/Nettotarve';
  import Lampokuormat from './FormParts/Tulokset/Lampokuormat';
  import Laskentatyokalu from './FormParts/Tulokset/Laskentatyokalu';

  import EnergiaverkostaOstetut from './FormParts/ToteutunutKulutus/EnergiaverkostaOstetut';
  import OstetutPolttoaineet from './FormParts/ToteutunutKulutus/OstetutPolttoaineet';
  import ToteutunutOstoenergia from './FormParts/ToteutunutKulutus/ToteutunutOstoenergia';

  import Huomio from './FormParts/Huomiot/Huomio';
  import Suositukset from './FormParts/Huomiot/Suositukset';

  import { flashMessageStore } from '@/stores';

  export let title = '';
  export let submit;
  export let energiatodistus;
  const originalEnergiatodistus = R.clone(energiatodistus);

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

<form
  on:submit|preventDefault={_ => {
    if (et.isValidForm(et.validators(schema), energiatodistus)) {
      flashMessageStore.flush();
      submit(energiatodistus);
    } else {
      flashMessageStore.add('Energiatodistus', 'error', $_('energiatodistus.messages.validation-error'));
    }
  }}>
  <div class="w-full mt-3">
    <H1 text={title} />
    <div class="flex flex-col py-4 -mx-4">

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
          id={'perustiedot.kieli'}
          name={'perustiedot.kieli'}
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
          id={'perustiedot.laatimisvaihe'}
          name={'perustiedot.laatimisvaihe'}
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

    <RakennuksenPerustiedot
      {schema}
      {disabled}
      bind:energiatodistus
      {labelLocale}
      {kayttotarkoitusluokat}
      {alakayttotarkoitusluokat} />

    <ToimenpideEhdotukset {disabled} {schema} bind:energiatodistus />

    <H2 text="E-luvun laskennan lähtotiedot" />

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

    <H2 text={$_('energiatodistus.toteutunut-ostoenergiankulutus.header')} />
    <EnergiaverkostaOstetut {disabled} {schema} bind:energiatodistus />
    <OstetutPolttoaineet {disabled} {schema} bind:energiatodistus />
    <ToteutunutOstoenergia {disabled} {schema} bind:energiatodistus />

    <H2 text={$_('energiatodistus.huomiot.header')} />
    <Huomio {disabled} {schema} huomio={'ymparys'} bind:energiatodistus />
    <Huomio
      {disabled}
      {schema}
      huomio={'alapohja-ylapohja'}
      bind:energiatodistus />
    <Huomio {disabled} {schema} huomio={'lammitys'} bind:energiatodistus />
    <Huomio
      {disabled}
      {schema}
      huomio={'iv-ilmastointi'}
      bind:energiatodistus />
    <Huomio
      {disabled}
      {schema}
      huomio={'valaistus-muut'}
      bind:energiatodistus />
    <Suositukset {disabled} {schema} bind:energiatodistus />
  </div>

  <div class="flex -mx-4 pt-8">
    <div class="px-4">
      <Button type={'submit'} text={$_('tallenna')} />
    </div>
    <div class="px-4">
      <Button
        on:click={event => {
          event.preventDefault();
          energiatodistus = R.clone(originalEnergiatodistus);
        }}
        text={$_('peruuta')}
        type={'reset'}
        style={'secondary'} />
    </div>
  </div>
</form>
