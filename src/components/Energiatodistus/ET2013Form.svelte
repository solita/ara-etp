<script>
  import * as R from 'ramda';

  import { locale, _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';
  import * as et from './energiatodistus-utils';
  import * as LocaleUtils from '@Language/locale-utils';

  import H1 from '@Component/H/H1';
  import H2 from '@Component/H/H2';
  import Select from '@Component/Select/Select';
  import HR from '@Component/HR/HR';

  import Input from './Input';
  import BasicInput from '@Component/Input/Input';
  import Checkbox from '@Component/Checkbox/Checkbox.svelte';
  import Textarea from './Textarea';

  import RakennuksenPerustiedot from './RakennuksenPerustiedot';
  import ToimenpideEhdotukset from './ToimenpideEhdotukset';

  import Rakennusvaippa from './form-parts/lahtotiedot/rakennusvaippa';
  import Ikkunat from './form-parts/lahtotiedot/ikkunat';
  import Ilmanvaihtojarjestelma from './form-parts/lahtotiedot/ilmanvaihtojarjestelma';
  import Lammitysjarjestelma from './form-parts/lahtotiedot/lammitysjarjestelma';
  import Jaahdytysjarjestelma from './form-parts/lahtotiedot/jaahdytysjarjestelma';
  import SisaisetLampokuormat from './form-parts/lahtotiedot/sisaiset-lampokuormat';
  import Lamminkayttovesi from './form-parts/lahtotiedot/lamminkayttovesi';

  import ELuku from './form-parts/tulokset/e-luku';
  import ELuvunErittely from './form-parts/tulokset/e-luvun-erittely';
  import UusiutuvatOmavaraisenergiat from './form-parts/tulokset/uusiutuvat-omavaraisenergiat-2013';
  import TeknistenjarjestelmienEnergiankulutus from './form-parts/tulokset/teknistenjarjestelmien-energiankulutus';
  import Nettotarve from './form-parts/tulokset/nettotarve';
  import Lampokuormat from './form-parts/tulokset/lampokuormat';

  import EnergiaverkostaOstetut from './form-parts/toteutunut-kulutus/energiaverkosta-ostetut';
  import OstetutPolttoaineet from './form-parts/toteutunut-kulutus/ostetut-polttoaineet';
  import ToteutunutOstoenergia from './form-parts/toteutunut-kulutus/toteutunut-ostoenergia';

  import Huomio from './form-parts/huomiot/huomio';
  import Suositukset from './form-parts/huomiot/suositukset';

  export let title = '';
  export let energiatodistus;
  export let inputLanguage;
  export let luokittelut;
  export let schema;
  export let disabled = false;

  $: labelLocale = LocaleUtils.label($locale);

</script>

<div class="w-full mt-3">
  <H1 text={title} />
  <H2 text={$_('energiatodistus.perustiedot.header')} />

  <div class="flex lg:flex-row flex-col -mx-4">
    {#if R.complement(R.isNil)(energiatodistus.id)}
      <div class="lg:w-1/2 w-full px-4 py-2">
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
  </div>
  <div class="flex lg:flex-row flex-col -mx-4">
    <div class="lg:w-1/2 w-full px-4 py-4">
      <Input
          {disabled}
          {schema}
          center={false}
          bind:model={energiatodistus}
          path={['perustiedot', 'yritys', 'nimi']} />
    </div>

    <div class="lg:w-1/2 w-full px-4 py-4">
      <Input
          {disabled}
          {schema}
          center={false}
          bind:model={energiatodistus}
          path={['perustiedot', 'tilaaja']} />
    </div>
  </div>

  <div class="flex flex-col -mx-4">
    <div class="lg:w-1/2 w-full px-4 py-4">
      <Select
          label={$_('energiatodistus.perustiedot.kieli')}
          required={false}
          {disabled}
          bind:model={energiatodistus}
          lens={R.lensPath(['perustiedot', 'kieli'])}
          parse={Maybe.Some}
          format={et.selectFormat(labelLocale, luokittelut.kielisyys)}
          items={R.pluck('id', luokittelut.kielisyys)} />
    </div>

    <div class="w-full px-4 py-4 mt-4">
      <Checkbox
          bind:model={energiatodistus}
          lens={R.lensPath(['perustiedot', 'uudisrakennus'])}
          label={$_('energiatodistus.perustiedot.uudisrakennus')}
          {disabled} />
    </div>

    <div class="lg:w-1/2 w-full  px-4 py-4">
      <Input
          {disabled}
          {schema}
          center={false}
          bind:model={energiatodistus}
          path={['tulokset', 'laskentatyokalu']} />
    </div>
  </div>

  <HR />

  <RakennuksenPerustiedot
      {schema}
      {inputLanguage}
      {disabled}
      bind:energiatodistus
      {labelLocale}
      kayttotarkoitusluokat = {luokittelut.kayttotarkoitusluokat}
      alakayttotarkoitusluokat = {luokittelut.alakayttotarkoitusluokat} />

  <HR />
  <ToimenpideEhdotukset versio={'2013'} {disabled} {schema} bind:energiatodistus />

  <HR />
  <H2 text={$_('energiatodistus.lahtotiedot.header')} />

  <div class="w-1/5 py-4 mb-4 flex flex-row items-end">
    <div class="w-5/6">
      <Input
          {disabled}
          {schema}
          center={false}
          bind:model={energiatodistus}
          path={['lahtotiedot', 'lammitetty-nettoala']} />
    </div>
    <div class="w-1/6 pl-2">m²</div>
  </div>

  <Rakennusvaippa {disabled} {schema} bind:energiatodistus />
  <Ikkunat {disabled} {schema} bind:energiatodistus />
  <Ilmanvaihtojarjestelma {disabled} {schema} bind:energiatodistus />
  <Lammitysjarjestelma {disabled} {schema} bind:energiatodistus />
  <Jaahdytysjarjestelma {disabled} {schema} bind:energiatodistus />
  <Lamminkayttovesi {disabled} {schema} bind:energiatodistus />
  <SisaisetLampokuormat {disabled} {schema} bind:energiatodistus />

  <HR />

  <H2 text={$_('energiatodistus.tulokset.header')} />

  <ELuku {schema} {energiatodistus} />
  <ELuvunErittely {disabled} {schema} bind:energiatodistus versio={2013} />
  <UusiutuvatOmavaraisenergiat {disabled} {schema} bind:energiatodistus />
  <TeknistenjarjestelmienEnergiankulutus
      {disabled}
      {schema}
      bind:energiatodistus />
  <Nettotarve {disabled} {schema} bind:energiatodistus />
  <Lampokuormat {disabled} {schema} bind:energiatodistus />

  <HR />
  <H2 text={$_('energiatodistus.toteutunut-ostoenergiankulutus.header')} />
  <EnergiaverkostaOstetut versio={2013} {disabled} {schema} bind:energiatodistus />
  <OstetutPolttoaineet {disabled} {schema} bind:energiatodistus />
  <ToteutunutOstoenergia {disabled} {schema} bind:energiatodistus />

  <HR />
  <H2 text={$_('energiatodistus.huomiot.header.2013')} />
  <Huomio {disabled} {schema} huomio={'ymparys'} bind:energiatodistus />
  <Huomio
      {disabled}
      {schema}
      huomio={'alapohja-ylapohja'}
      bind:energiatodistus />
  <Huomio {disabled} {schema} huomio={'lammitys'} bind:energiatodistus />
  <Huomio {disabled} {schema} huomio={'iv-ilmastointi'} bind:energiatodistus />
  <Huomio {disabled} {schema} huomio={'valaistus-muut'} bind:energiatodistus />
  <Suositukset versio={2013} {disabled} {schema} bind:energiatodistus />

  <H2 text={$_('energiatodistus.lisamerkintoja')} />
  <div class="w-full py-4 mb-4">
    <Textarea
      {disabled}
      {schema}
      inputLanguage={Maybe.Some(inputLanguage)}
      bind:model={energiatodistus}
      path={['lisamerkintoja']} />
  </div>
</div>
