<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';
  import * as et from './energiatodistus-utils';

  import H2 from '@Component/H/H2';
  import Select from '@Component/Select/Select';
  import * as Future from '@Utility/future-utils';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as laskutusApi from '@Component/Laskutus/laskutus-api';
  import * as laatijaApi from '@Pages/laatija/laatija-api';
  import { flashMessageStore } from '@/stores';
  import Loading from '@Component/Loading/Loading.svelte';
  import Input from '@Component/Input/Input';
  import EtInput from './Input';
  import HR from '@Component/HR/HR.svelte';

  export let energiatodistus;
  export let whoami;
  export let schema;

  let resources = Maybe.None();
  let error = Maybe.None();

  const i18n = $_;

  $: disabled =
    energiatodistus.laskutusaika.isSome() || Kayttajat.isLaskuttaja(whoami);

  $: laatijaId = R.compose(
    Maybe.orSome(whoami.id),
    R.prop('laatija-id')
  )(energiatodistus);

  $: {
    resources = Maybe.None();
    Future.fork(
    () => {
      error = Maybe.Some($_('energiatodistus.laskutus.load-error'));
      flashMessageStore.add('Energiatodistus', 'error', error.some());
    },
    response => {
      resources = Maybe.Some(response);
    },
    Future.parallelObject(2, {
      verkkolaskuoperaattorit: laskutusApi.verkkolaskuoperaattorit,
      laskutusosoitteet: laatijaApi.laskutusosoitteet(laatijaId)
    }));
  }

  const yritysLabel = yritys =>
    yritys.nimi +
    R.compose(
      Maybe.orSome(''),
      R.map(R.concat(' / ')),
      R.prop('vastaanottajan-tarkenne')
    )(yritys) +
    ' | ' +
    Maybe.orSome('', yritys.ytunnus);

  const osoiteLabel = R.ifElse(
    R.compose(R.propEq('id', -1)),
    _ => i18n('energiatodistus.laskutus.laatijalaskutus'),
    yritysLabel);

  const postiosoite = entity =>
    entity.jakeluosoite +
    ', ' +
    entity.postinumero +
    ' ' +
    entity.postitoimipaikka;

  $: findLaskutusosoite = laskutusosoitteet =>
    R.chain(Maybe.findById(R.__, laskutusosoitteet),
      energiatodistus['laskutusosoite-id']);

  const verkkolaskuoperaattori = verkkolaskuoperaattorit => R.compose(
    Maybe.orSome(''),
    R.map(et.selectFormat(R.prop('nimi'), verkkolaskuoperaattorit)),
    R.prop('verkkolaskuoperaattori')
  );

  const isVerkkolasku = R.compose(
    R.all(Maybe.isSome),
    R.juxt([R.prop('verkkolaskuoperaattori'), R.prop('verkkolaskuosoite')])
  );
</script>

<H2 text={'* ' + $_('energiatodistus.laskutus.title')} />

{#each Maybe.toArray(resources) as { verkkolaskuoperaattorit, laskutusosoitteet}}
  <div class="flex flex-col lg:flex-row -mx-4">
    <div class="lg:w-1/2 w-full px-4 py-4">
      <Select
        id="laskutusosoite-id"
        name="laskutusosoite-id"
        label={$_('energiatodistus.laskutusosoite-id')}
        required={true}
        allowNone={false}
        validation={schema.$signature}
        {disabled}
        bind:model={energiatodistus}
        lens={R.lensProp('laskutusosoite-id')}
        parse={Maybe.Some}
        format={et.selectFormat(osoiteLabel, laskutusosoitteet)}
        items={R.pluck('id', laskutusosoitteet)} />
    </div>
  </div>
  <div class="flex flex-col lg:flex-row -mx-4">
    {#each findLaskutusosoite(laskutusosoitteet).toArray() as osoite}
      {#if isVerkkolasku(osoite)}
        <div class="lg:w-1/2 w-full px-4 py-4">
          <Input
            id="energiatodistus.laskutus.verkkolaskuoperaattori"
            name="energiatodistus.laskutus.verkkolaskuoperaattori"
            label={$_('energiatodistus.laskutus.verkkolaskuoperaattori')}
            disabled={true}
            model={verkkolaskuoperaattori(verkkolaskuoperaattorit)(osoite)} />
        </div>
        <div class="lg:w-1/2 w-full px-4 py-4">
          <Input
            id="energiatodistus.laskutus.verkkolaskuosoite"
            name="energiatodistus.laskutus.verkkolaskuosoite"
            label={$_('energiatodistus.laskutus.verkkolaskuosoite')}
            disabled={true}
            model={Maybe.orSome('', osoite.verkkolaskuosoite)} />
        </div>
      {:else}
        <div class="lg:w-1/2 w-full px-4 py-4">
          <Input
              id="energiatodistus.laskutus.postiosoite"
              name="energiatodistus.laskutus.postiosoite"
              label={$_('energiatodistus.laskutus.postiosoite')}
              disabled={true}
              model={postiosoite(osoite)} />
        </div>
      {/if}
    {/each}
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
{/each}

{#if Maybe.isNone(resources)}
  <Loading {error} />
{/if}

<HR />
