<script>
  import * as R from 'ramda';
  import { locale, _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';
  import * as et from './energiatodistus-utils';
  import * as Postinumero from '@Component/address/postinumero-fi';
  import * as Laatimisvaiheet from '././laatimisvaiheet';

  import H2 from '@Component/H/H2';
  import Input from '@Pages/energiatodistus/Input';
  import Checkbox from '@Component/Checkbox/Checkbox';
  import Select from '@Component/Select/Select';
  import Autocomplete from '@Component/Autocomplete/Autocomplete.svelte';
  import * as LocaleUtils from '@Language/locale-utils';
  import * as Kayttajat from '@Utility/kayttajat';

  export let schema;
  export let disabled;
  export let energiatodistus;
  export let inputLanguage;
  export let whoami;

  export let kayttotarkoitusluokat;
  export let alakayttotarkoitusluokat;
  export let postinumerot;

  const center = false;

  $: labelLocale = LocaleUtils.label($locale);

  $: postinumeroNames = R.map(
    Postinumero.fullLabel($locale),
    R.filter(
      R.allPass([R.propEq(true, 'valid'), Postinumero.isNormal]),
      postinumerot
    )
  );

  let kayttotarkoitusluokkaId = Maybe.None();
  $: if (
    kayttotarkoitusluokkaId.isSome() &&
    !kayttotarkoitusluokkaId.equals(
      et.findKayttotarkoitusluokkaId(
        energiatodistus.perustiedot.kayttotarkoitus,
        alakayttotarkoitusluokat
      )
    )
  ) {
    energiatodistus = R.set(
      R.lensPath(['perustiedot', 'kayttotarkoitus']),
      et.findAlakayttotarkoitusluokkaId(
        kayttotarkoitusluokkaId,
        alakayttotarkoitusluokat
      ),
      energiatodistus
    );
  } else if (energiatodistus.perustiedot.kayttotarkoitus.isSome()) {
    kayttotarkoitusluokkaId = et.findKayttotarkoitusluokkaId(
      energiatodistus.perustiedot.kayttotarkoitus,
      alakayttotarkoitusluokat
    );
  }

  $: selectableAlakayttotarkoitusluokat = et.filterAlakayttotarkoitusLuokat(
    kayttotarkoitusluokkaId,
    alakayttotarkoitusluokat
  );
</script>

<H2 text="Rakennuksen perustiedot" />

<div class="flex lg:flex-row flex-col gap-x-8">
  <div class="lg:w-3/5 w-full py-4">
    <Input
      disabled={Kayttajat.isPaakayttaja(whoami) ? false : disabled}
      {schema}
      {center}
      inputLanguage={Maybe.Some(inputLanguage)}
      bind:model={energiatodistus}
      path={['perustiedot', 'nimi']} />
  </div>

  <div class="lg:w-2/5 py-4">
    <Input
      {disabled}
      {schema}
      {center}
      bind:model={energiatodistus}
      path={['perustiedot', 'valmistumisvuosi']} />
  </div>
</div>

<div class="flex lg:flex-row flex-col gap-x-8 my-4">
  <div class="w-full py-4">
    <Input
      {disabled}
      {schema}
      {center}
      bind:model={energiatodistus}
      path={['perustiedot', 'rakennusosa']} />
  </div>
</div>

<div class="flex lg:flex-row flex-col gap-x-8 my-4">
  <div class="lg:w-2/3 w-full py-4">
    <Input
      {disabled}
      {schema}
      {center}
      inputLanguage={Maybe.Some(inputLanguage)}
      bind:model={energiatodistus}
      path={['perustiedot', 'katuosoite']} />
  </div>

  <div class="lg:w-1/3 w-full py-4">
    <Autocomplete items={postinumeroNames} size={10}>
      <Input
        {disabled}
        {schema}
        {center}
        search={true}
        bind:model={energiatodistus}
        format={Maybe.fold(
          '',
          Postinumero.formatPostinumero(postinumerot, $locale)
        )}
        path={['perustiedot', 'postinumero']} />
    </Autocomplete>
  </div>
</div>

<div class="flex lg:flex-row flex-col gap-x-8 my-4">
  <div class="lg:w-1/2 w-full py-4">
    <Input
      disabled={Kayttajat.isLaskuttaja(whoami) ||
        R.allPass([R.propEq(2018, 'versio'), Laatimisvaiheet.isRakennuslupa])(
          energiatodistus
        )}
      {schema}
      {center}
      bind:model={energiatodistus}
      path={['perustiedot', 'rakennustunnus']} />
  </div>

  <div class="lg:w-1/2 w-full py-4">
    <Input
      {disabled}
      {schema}
      {center}
      bind:model={energiatodistus}
      path={['perustiedot', 'kiinteistotunnus']} />
  </div>
</div>

<div class="flex lg:flex-row flex-col gap-x-8 my-4">
  <div class="lg:w-1/2 w-full py-4">
    <Select
      id={'perustiedot.kayttotarkoitusluokka'}
      label={$_('energiatodistus.perustiedot.kayttotarkoitusluokka')}
      required={true}
      validation={schema.$signature}
      allowNone={false}
      {disabled}
      bind:model={kayttotarkoitusluokkaId}
      lens={R.lens(R.identity, R.identity)}
      parse={Maybe.Some}
      format={et.selectFormat(labelLocale, kayttotarkoitusluokat)}
      items={R.pluck('id', kayttotarkoitusluokat)} />
  </div>

  <div class="lg:w-1/2 w-full py-4">
    <Select
      id={'perustiedot.kayttotarkoitus'}
      label={$_('energiatodistus.perustiedot.alakayttotarkoitusluokka')}
      required={true}
      validation={schema.$signature}
      allowNone={false}
      {disabled}
      bind:model={energiatodistus}
      lens={R.lensPath(['perustiedot', 'kayttotarkoitus'])}
      parse={Maybe.Some}
      format={et.selectFormat(labelLocale, alakayttotarkoitusluokat)}
      items={R.pluck('id', selectableAlakayttotarkoitusluokat)} />
  </div>
</div>

<div class="flex flex-col gap-x-8 my-4">
  <div class="w-full py-4">
    <Checkbox
      bind:model={energiatodistus}
      lens={R.lensPath(['perustiedot', 'julkinen-rakennus'])}
      label={$_('energiatodistus.perustiedot.julkinen-rakennus')}
      {disabled} />
  </div>
</div>
