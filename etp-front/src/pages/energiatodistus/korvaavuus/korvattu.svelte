<script>
  import { slide } from 'svelte/transition';
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  import Checkbox from '@Component/Checkbox/Checkbox';
  import Input from '@Component/Input/Input';
  import Spinner from '@Component/Spinner/Spinner';
  import EtTable from './energiatodistus-table';
  import EnergiatodistusKorvausEhdotus from './ehdotus';

  import * as EtApi from '../energiatodistus-api';

  import * as Korvaus from './korvaus';
  import * as ET from '../energiatodistus-utils';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Parsers from '@Utility/parsers';
  import { announcementsForModule } from '@Utility/announce';

  export let energiatodistus;
  export let whoami;
  export let error = Maybe.None();
  export let postinumerot;
  export let dirty;

  const i18n = $_;
  const { announceError } = announcementsForModule('Energiatodistus');

  const enabled =
    (Kayttajat.isLaatija(whoami) &&
      R.propEq(ET.tila.draft, 'tila-id', energiatodistus)) ||
    (Kayttajat.isPaakayttaja(whoami) &&
      !ET.isTilaInTilat([ET.tila.draft, ET.tila.deleted])(energiatodistus));

  const lens = R.lensProp('korvattu-energiatodistus-id');

  let cancel = () => {};
  let searching = false;
  let updated = true;
  $: query = R.view(lens, energiatodistus);
  let korvattavaEnergiatodistus = Maybe.None();
  let checked = Maybe.isSome(R.view(lens, energiatodistus));

  const parseId = R.compose(Maybe.fromFalsy, value => parseInt(value, 10));

  const setKorvattavaEnergiatodistus = id => {
    if (!R.equals(R.view(lens, energiatodistus), id)) {
      energiatodistus = R.set(lens, id, energiatodistus);
    }
  };

  $: korvattavaETError = korvattava =>
    Korvaus.isSame(korvattava, energiatodistus)
      ? Maybe.Some('is-same')
      : !Korvaus.isValidState(korvattava, energiatodistus)
        ? Maybe.Some('invalid-tila')
        : Korvaus.hasOtherKorvaaja(korvattava, energiatodistus)
          ? Maybe.Some('already-replaced')
          : Maybe.None();

  const fetchKorvattavaEnergiatodistus = initialDelay => id => {
    cancel = R.compose(
      Future.fork(
        _ => {
          searching = false;
          updated = true;
          announceError(i18n('energiatodistus.messages.load-error'));
        },
        response => {
          searching = false;
          updated = true;
          korvattavaEnergiatodistus = Maybe.fromNull(response);
        }
      ),
      R.chain(Future.after(200)),
      R.chain(EtApi.findEnergiatodistusById),
      R.map(
        R.tap(_ => {
          searching = true;
        })
      ),
      initialDelay > 0 ? Future.after(initialDelay) : Future.resolve,
      R.tap(_ => {
        updated = false;
      }),
      R.tap(cancel)
    )(id);
  };

  $: R.forEach(
    fetchKorvattavaEnergiatodistus(0),
    R.view(lens, energiatodistus)
  );

  $: if (enabled) {
    if (checked) {
      setKorvattavaEnergiatodistus(korvattavaEnergiatodistus.map(R.prop('id')));
    } else {
      setKorvattavaEnergiatodistus(Maybe.None());
    }
  }

  $: error =
    checked && query.isSome() && updated
      ? Maybe.isNone(R.chain(parseId, query))
        ? Maybe.Some('invalid-id')
        : korvattavaEnergiatodistus.isNone()
          ? Maybe.Some('not-found')
          : R.chain(korvattavaETError, korvattavaEnergiatodistus)
      : Maybe.None();

  const searchKorvattavaEnergiatodistus = R.compose(
    Maybe.cata(_ => {
      korvattavaEnergiatodistus = Maybe.None();
    }, fetchKorvattavaEnergiatodistus(500)),
    R.chain(parseId)
  );

  const updateQuery = event => {
    query = Parsers.optionalString(event.target.value);
    searchKorvattavaEnergiatodistus(query);
  };
</script>

<style type="text/postcss">
  .error-label {
    @apply absolute top-auto;
    font-size: smaller;
  }

  .error-icon {
    @apply text-error;
  }

  h3 {
    @apply text-primary uppercase font-bold text-sm mt-6;
  }
</style>

<EnergiatodistusKorvausEhdotus
  {postinumerot}
  {whoami}
  bind:dirty
  bind:checked
  bind:energiatodistus />

{#if checked || enabled}
  {#if enabled}
    <Checkbox
      label={i18n('energiatodistus.korvaavuus.checkbox')}
      dataCy="korvaavuus-checkbox"
      bind:model={checked}
      disabled={!enabled} />
  {/if}

  {#if !enabled}
    <h3>{i18n('energiatodistus.korvaavuus.header.korvattu')}</h3>
  {/if}

  {#if checked}
    <div
      class="flex flex-col gap-x-8 mt-2"
      transition:slide={{ duration: 200 }}>
      {#if enabled}
        <div class="w-full lg:w-1/2 py-2" on:input={updateQuery}>
          <Input
            model={query}
            id={'korvattavaenergiatodistus'}
            name={'korvattavaenergiatodistus'}
            label={i18n('energiatodistus.korvaavuus.input')}
            lens={R.lens(R.identity, R.identity)}
            format={Maybe.orSome('')}
            parse={Parsers.optionalString}
            {i18n} />
        </div>
      {/if}
      {#if !searching}
        <div class="w-full">
          {#each Maybe.toArray(korvattavaEnergiatodistus) as et}
            <div class="w-full py-4" transition:slide={{ duration: 200 }}>
              <EtTable energiatodistus={et} {whoami} {postinumerot} />
            </div>
          {/each}
          {#each Maybe.toArray(error) as key}
            <div class="error-label">
              <span class="font-icon error-icon">error</span>
              {i18n('energiatodistus.korvaavuus.validation.' + key)}
            </div>
          {/each}
        </div>
      {:else}
        <Spinner />
      {/if}
    </div>
  {/if}
{/if}
