<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';
  import * as et from './energiatodistus-utils';
  import * as localstorage from './local-storage';
  import * as dfns from 'date-fns';

  import H2 from '@Component/H/H2';
  import Select from '@Component/Select/Select';
  import * as Future from '@Utility/future-utils';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as api from '@Pages/energiatodistus/energiatodistus-api';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';
  import * as laskutusApi from '@Component/Laskutus/laskutus-api';
  import { flashMessageStore } from '@/stores';
  import Loading from '@Component/Loading/Loading.svelte';
  import Input from '@Component/Input/Input';
  import EtInput from './Input';
  import HR from '@Component/HR/HR.svelte';

  export let energiatodistus;
  export let whoami;
  export let schema;

  let laatijaYritykset = [];
  let verkkolaskuoperaattorit = [];
  let loading = true;
  let laatija = Maybe.None();
  let error = Maybe.None();
  const toggleLoading = value => {
    loading = value;
  };

  $: disabled =
    energiatodistus['laskutusaika'].isSome() || Kayttajat.isLaskuttaja(whoami);

  const getLaatija = id =>
    R.equals(id, whoami.id) || Kayttajat.isPaakayttaja(whoami)
      ? R.map(Maybe.Some, kayttajaApi.getLaatijaById(fetch, id))
      : Future.resolve(Maybe.None());

  R.compose(
    Future.fork(
      () => {
        error = Maybe.Some($_('energiatodistus.laskutus.load-error'));
        flashMessageStore.add('Energiatodistus', 'error', error.some());
      },
      response => {
        laatijaYritykset = response[1][0];
        laatija = response[1][1];
        verkkolaskuoperaattorit = response[0];

        localstorage.getDefaultLaskutettavaYritysId().forEach(id => {
          if (
            R.isNil(energiatodistus.id) &&
            energiatodistus['laskutettava-yritys-id'].isNone() &&
            R.any(R.propEq('id', id), laatijaYritykset)
          ) {
            energiatodistus = R.assoc(
              'laskutettava-yritys-id',
              Maybe.Some(id),
              energiatodistus
            );
          }
        });

        toggleLoading(false);
      }
    ),
    Future.both(laskutusApi.verkkolaskuoperaattorit),
    R.converge(Future.both, [api.getLaatijaYritykset(fetch), getLaatija]),
    Maybe.orSome(whoami.id),
    R.prop('laatija-id'),
    R.tap(() => toggleLoading(true))
  )(energiatodistus);

  const yritysLabel = yritys =>
    yritys.nimi +
    ' / ' +
    R.compose(
      Maybe.orSome(''),
      R.map(R.concat(R.__, ' | ')),
      R.prop('vastaanottajan-tarkenne')
    )(yritys) +
    yritys.ytunnus;

  const postiosoite = entity =>
    entity.jakeluosoite +
    ', ' +
    entity.postinumero +
    ' ' +
    entity.postitoimipaikka;

  let laskutettavaYritys = Maybe.None();
  $: laskutettavaYritys = Maybe.chain(
    Maybe.findById(R.__, laatijaYritykset),
    energiatodistus['laskutettava-yritys-id']
  );

  $: verkkolaskuoperaattori = R.compose(
    R.map(et.selectFormat(R.prop('nimi'), verkkolaskuoperaattorit)),
    R.prop('verkkolaskuoperaattori')
  );

  $: verkkolasku = R.compose(
    Maybe.toMaybeList,
    R.juxt([verkkolaskuoperaattori, R.prop('verkkolaskuosoite')])
  );
</script>

<H2 text={'* ' + $_('energiatodistus.laskutus.title')} />

{#if !loading}
  <div class="flex flex-col lg:flex-row -mx-4">
    <div class="lg:w-1/2 w-full px-4 py-4">
      <Select
        id={'laskutettava-yritys-id'}
        name="laskutettava-yritys-id"
        label={$_('energiatodistus.laskutus.laskutettava')}
        required={true}
        validation={schema.$signature}
        allowNone={true}
        noneLabel={'energiatodistus.laskutus.laatijalaskutus'}
        {disabled}
        bind:model={energiatodistus}
        lens={R.lensPath(['laskutettava-yritys-id'])}
        parse={Maybe.Some}
        format={et.selectFormat(yritysLabel, laatijaYritykset)}
        items={R.pluck('id', laatijaYritykset)} />
    </div>
  </div>
  <div class="flex flex-col lg:flex-row -mx-4">
    {#each laskutettavaYritys.toArray() as yritys}
      {#each verkkolasku(yritys).toArray() as [operaattori, osoite]}
        <div class="lg:w-1/2 w-full px-4 py-4">
          <Input
            id="energiatodistus.laskutus.verkkolaskuoperaattori"
            name="energiatodistus.laskutus.verkkolaskuoperaattori"
            label={$_('energiatodistus.laskutus.verkkolaskuoperaattori')}
            disabled={true}
            model={operaattori} />
        </div>
        <div class="lg:w-1/2 w-full px-4 py-4">
          <Input
            id="energiatodistus.laskutus.verkkolaskuosoite"
            name="energiatodistus.laskutus.verkkolaskuosoite"
            label={$_('energiatodistus.laskutus.verkkolaskuosoite')}
            disabled={true}
            model={osoite} />
        </div>
      {/each}
      {#if verkkolasku(yritys).isNone()}
        <div class="lg:w-1/2 w-full px-4 py-4">
          <Input
            id="energiatodistus.laskutus.postiosoite"
            name="energiatodistus.laskutus.postiosoite"
            label={$_('energiatodistus.laskutus.postiosoite')}
            disabled={true}
            model={postiosoite(yritys)} />
        </div>
      {/if}
    {/each}
    {#if laskutettavaYritys.isNone()}
      {#each laatija.toArray() as laatija}
        <div class="lg:w-1/2 w-full px-4 py-4">
          <Input
            id="energiatodistus.laskutus.postiosoite"
            name="energiatodistus.laskutus.postiosoite"
            label={$_('energiatodistus.laskutus.postiosoite')}
            disabled={true}
            model={postiosoite(laatija)} />
        </div>
      {/each}
    {/if}
  </div>
  <div class="flex flex-col lg:flex-row -mx-4">
    <div class="lg:w-1/2 w-full px-4 py-4">
      <EtInput
        {disabled}
        {schema}
        center={false}
        bind:model={energiatodistus}
        path={['laskuriviviite']} />
    </div>
  </div>
{:else}
  <Loading {error} />
{/if}

<HR />
