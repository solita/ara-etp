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
  import Textarea from './Textarea';

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
      R.compose(
        Either.orSome(Maybe.None()),
        Either.map(alaluokat =>
          R.length(alaluokat) === 1 ? Maybe.Some(alaluokat[0].id) : Maybe.None()
        ),
        et.filterAlakayttotarkoitusLuokat(kayttotarkoitusluokkaId)
      )(alakayttotarkoitusluokat),
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

<style type="text/postcss">
  table {
    @apply w-full border-b-1 border-disabled pb-8;
  }

  tr > td:not(:first-child),
  tr > th:not(:first-child) {
    @apply border-l-1 border-disabled;
  }
  thead {
    @apply bg-tertiary;
  }

  th {
    @apply px-4 py-2 text-center;
  }

  tr:last-child > td {
    @apply pb-5;
  }

  td {
    @apply text-center py-2 px-4;
  }
</style>

<form
  on:submit|preventDefault={_ => {
    if (et.isValidForm(et.validators(schema), energiatodistus)) {
      flashMessageStore.flush();
      submit(energiatodistus);
    } else {
      flashMessageStore.add('EnergiaTodistus', 'error', $_('energiatodistus.messages.validation-error'));
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

    <H2 text="Rakennuksen perustiedot" />

    <div class="flex lg:flex-row flex-col -mx-4">
      <div class="lg:w-4/5 w-full px-4 py-4">
        <Input
          {disabled}
          {schema}
          bind:model={energiatodistus}
          path={['perustiedot', 'nimi']} />
      </div>

      <div class="lg:w-1/5 px-4 py-4">
        <Input
          {disabled}
          {schema}
          bind:model={energiatodistus}
          path={['perustiedot', 'valmistumisvuosi']} />
      </div>
    </div>

    <div class="flex lg:flex-row flex-col -mx-4 my-4">
      <div class="w-full px-4 py-4">
        <Input
          {disabled}
          {schema}
          bind:model={energiatodistus}
          path={['perustiedot', 'rakennusosa']} />
      </div>
    </div>

    <div class="flex lg:flex-row flex-col -mx-4 my-4">
      <div class="lg:w-4/5 w-full px-4 py-4">
        <Input
          {disabled}
          {schema}
          bind:model={energiatodistus}
          path={['perustiedot', 'katuosoite-fi']} />
      </div>

      <div class="lg:w-1/5 w-full px-4 py-4">
        <Input
          {disabled}
          {schema}
          bind:model={energiatodistus}
          path={['perustiedot', 'postinumero']} />
      </div>
    </div>

    <div class="flex lg:flex-row flex-col -mx-4 my-4">
      <div class="lg:w-1/2 w-full px-4 py-4">
        <Input
          {disabled}
          {schema}
          bind:model={energiatodistus}
          path={['perustiedot', 'rakennustunnus']} />
      </div>

      <div class="lg:w-1/2 w-full px-4 py-4">
        <Input
          {disabled}
          {schema}
          bind:model={energiatodistus}
          path={['perustiedot', 'kiinteistotunnus']} />
      </div>
    </div>

    <div class="flex lg:flex-row flex-col -mx-4 my-4">
      <div class="lg:w-1/2 w-full px-4 py-4">
        <Select
          id={'perustiedot.kayttotarkoitusluokka'}
          name={'perustiedot.kayttotarkoitusluokka'}
          label={$_('energiatodistus.perustiedot.kayttotarkoitusluokka')}
          required={true}
          {disabled}
          bind:model={kayttotarkoitusluokkaId}
          lens={R.lens(R.identity, R.identity)}
          parse={Maybe.Some}
          format={et.selectFormat(labelLocale, kayttotarkoitusluokat)}
          items={Either.foldRight([], R.pluck('id'), kayttotarkoitusluokat)} />
      </div>

      <div class="lg:w-1/2 w-full px-4 py-4">
        <Select
          id={'perustiedot.alakayttotarkoitusluokka'}
          name={'perustiedot.alakayttotarkoitusluokka'}
          label={$_('energiatodistus.perustiedot.alakayttotarkoitusluokka')}
          required={true}
          {disabled}
          bind:model={energiatodistus}
          lens={R.lensPath(['perustiedot', 'kayttotarkoitus'])}
          parse={Maybe.Some}
          format={et.selectFormat(labelLocale, alakayttotarkoitusluokat)}
          items={Either.foldRight([], R.pluck('id'), selectableAlakayttotarkoitusluokat)} />
      </div>
    </div>

    <div class="flex flex-col -mx-4 my-4">
      <div class="w-full px-4 py-4">
        <Checkbox
          bind:model={energiatodistus}
          lens={R.lensPath(['perustiedot', 'onko-julkinen-rakennus'])}
          label={$_('energiatodistus.perustiedot.onko-julkinen-rakennus')}
          {disabled} />
      </div>
    </div>

    <H2 text="Toimenpide-ehdotuksia e-luvun parantamiseksi" />
    <H3
      text="Keskeiset suositukset rakennuksen e-lukua parantaviksi
      toimenpiteiksi" />

    <div class="w-full py-4 mb-4">
      <Textarea
        {disabled}
        {schema}
        bind:model={energiatodistus}
        path={['perustiedot', 'keskeiset-suositukset-fi']} />
    </div>

    <H2 text="E-luvun laskennan lähtotiedot" />

    <div class="w-1/5 py-4 mb-4">
      <Input
        {disabled}
        {schema}
        bind:model={energiatodistus}
        path={['lahtotiedot', 'lammitetty-nettoala']} />
    </div>

    <H3 text="Rakennusvaippa" />

    <div class="w-1/5 py-4 mb-6">
      <Input
        {disabled}
        {schema}
        bind:model={energiatodistus}
        path={['lahtotiedot', 'rakennusvaippa', 'ilmanvuotoluku']} />
    </div>

    <table class="mb-6">
      <thead>
        <tr>
          <th />
          <th>Ala (m²)</th>
          <th>U (W/(m²K))</th>
          <th>U*A (W/K)</th>
          <th>Osuus lämpöhäviöistä</th>
        </tr>
      </thead>
      <tbody>
        {#each ['ulkoseinat', 'ylapohja', 'alapohja', 'ikkunat', 'ulkoovet'] as vaippa}
          <tr>
            <td>
              {$_(`energiatodistus.lahtotiedot.rakennusvaippa.${vaippa}.label`)}
            </td>
            <td>
              <Input
                {disabled}
                {schema}
                compact={true}
                bind:model={energiatodistus}
                path={['lahtotiedot', 'rakennusvaippa', vaippa, 'ala']} />
            </td>
            <td>
              <Input
                {disabled}
                {schema}
                compact={true}
                bind:model={energiatodistus}
                path={['lahtotiedot', 'rakennusvaippa', vaippa, 'U']} />
            </td>
            <td />
            <td />
          </tr>
        {/each}
        <tr>
          <td>
            {$_('energiatodistus.lahtotiedot.rakennusvaippa.kylmasillat')}
          </td>
          <td />
          <td />
          <td>
            <Input
              {disabled}
              {schema}
              compact={true}
              bind:model={energiatodistus}
              path={['lahtotiedot', 'rakennusvaippa', 'kylmasillat-UA']} />
          </td>
          <td />
        </tr>
      </tbody>
    </table>

    <H3 compact={true} text="Ikkunat ilmansuunnittain" />

    <table class="mb-6">
      <thead>
        <tr>
          <th>Ikkuna</th>
          <th>Ala (m²)</th>
          <th>U (W/(m²K))</th>
          <th>g-kohtisuora</th>
        </tr>
      </thead>
      <tbody>
        {#each ['pohjoinen', 'koillinen', 'ita', 'kaakko', 'etela', 'lounas', 'lansi', 'luode'] as ikkuna}
          <tr>
            <td>{$_(`energiatodistus.lahtotiedot.ikkunat.${ikkuna}.label`)}</td>
            <td>
              <Input
                {disabled}
                {schema}
                compact={true}
                bind:model={energiatodistus}
                path={['lahtotiedot', 'ikkunat', ikkuna, 'ala']} />
            </td>
            <td>
              <Input
                {disabled}
                {schema}
                compact={true}
                bind:model={energiatodistus}
                path={['lahtotiedot', 'ikkunat', ikkuna, 'U']} />
            </td>
            <td>
              <Input
                {disabled}
                {schema}
                compact={true}
                bind:model={energiatodistus}
                path={['lahtotiedot', 'ikkunat', ikkuna, 'g-ks']} />
            </td>
          </tr>
        {/each}
      </tbody>
    </table>

    <H3 text="Ilmanvaihtojärjestelmä" />

    <div class="w-full py-4 mb-4">
      <Input
        {disabled}
        {schema}
        bind:model={energiatodistus}
        path={['lahtotiedot', 'ilmanvaihto', 'kuvaus-fi']} />
    </div>

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
