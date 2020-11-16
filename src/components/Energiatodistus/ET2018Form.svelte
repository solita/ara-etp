<script>
  import * as R from 'ramda';

  import { locale, _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';
  import * as et from './energiatodistus-utils';
  import * as Laatimisvaiheet from './laatimisvaiheet';
  import * as LocaleUtils from '@Language/locale-utils';

  import H2 from '@Component/H/H2';
  import Select from '@Component/Select/Select';
  import HR from '@Component/HR/HR';

  import Input from './Input';
  import BasicInput from '@Component/Input/Input';
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

  export let energiatodistus;
  export let inputLanguage;
  export let luokittelut;
  export let schema;
  export let disabled = false;
  export let validation;
  export let eTehokkuus = Maybe.None();

  $: labelLocale = LocaleUtils.label($locale);

</script>

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
      id={'perustiedot.kieli'}
      label={$_('energiatodistus.perustiedot.kieli')}
      required={false}
      {disabled}
      bind:model={energiatodistus}
      lens={R.lensPath(['perustiedot', 'kieli'])}
      allowNone={false}
      parse={Maybe.Some}
      format={et.selectFormat(labelLocale, luokittelut.kielisyys)}
      items={R.pluck('id', luokittelut.kielisyys)} />
  </div>

  <div class="lg:w-1/2 w-full px-4 py-4">
    <Select
      id={'perustiedot.laatimisvaihe'}
      label={$_('energiatodistus.perustiedot.laatimisvaihe')}
      required={false}
      {disabled}
      bind:model={energiatodistus}
      lens={R.lensPath(['perustiedot', 'laatimisvaihe'])}
      parse={Maybe.Some}
      format={et.selectFormat(labelLocale, luokittelut.laatimisvaiheet)}
      items={R.pluck('id', luokittelut.laatimisvaiheet)} />
  </div>
  {#if Laatimisvaiheet.isOlemassaOlevaRakennus(energiatodistus)}
    <div class="lg:w-1/2 w-full px-4 py-4">
      <Input
        {disabled}
        {schema}
        required={true}
        center={false}
        bind:model={energiatodistus}
        path={['perustiedot', 'havainnointikaynti']} />
    </div>
  {/if}

  <div class="lg:w-1/2 w-full px-4 py-4">
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
  postinumerot={luokittelut.postinumerot}
  kayttotarkoitusluokat={luokittelut.kayttotarkoitusluokat}
  alakayttotarkoitusluokat={luokittelut.alakayttotarkoitusluokat} />

<ELuku {eTehokkuus} />
<HR />
<ToimenpideEhdotukset
  versio={'2018'}
  {disabled}
  {inputLanguage}
  {schema}
  bind:energiatodistus />

<HR />
<H2 text={$_('energiatodistus.lahtotiedot.header')} />

<div class="w-1/5 py-4 mb-4 flex flex-row items-end">
  <Input
    {disabled}
    {schema}
    center={false}
    bind:model={energiatodistus}
    path={['lahtotiedot', 'lammitetty-nettoala']}
    unit={Area} />
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

<H2 text={$_('energiatodistus.tulokset.header')} />

<ELuku {eTehokkuus} />
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
<H2 text={$_('energiatodistus.toteutunut-ostoenergiankulutus.header')} />
<EnergiaverkostaOstetut
  versio={2018}
  {disabled}
  {schema}
  {inputLanguage}
  bind:energiatodistus />
<OstetutPolttoaineet {disabled} {schema} bind:energiatodistus />
<ToteutunutOstoenergia {disabled} {schema} bind:energiatodistus />

<HR />
<H2 text={$_('energiatodistus.huomiot.header.2018')} />
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

<H2 text={$_('energiatodistus.lisamerkintoja')} />
<div class="w-full py-4 mb-4">
  <Textarea
    {disabled}
    {schema}
    inputLanguage={Maybe.Some(inputLanguage)}
    bind:model={energiatodistus}
    path={['lisamerkintoja']} />
</div>
