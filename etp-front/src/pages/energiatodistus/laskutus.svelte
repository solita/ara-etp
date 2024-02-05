<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';
  import * as et from './energiatodistus-utils';

  import H2 from '@Component/H/H2';
  import Select from '@Component/Select/Select';
  import * as Kayttajat from '@Utility/kayttajat';
  import Input from '@Component/Input/Input';
  import EtInput from './Input';
  import HR from '@Component/HR/HR.svelte';

  export let energiatodistus;
  export let whoami;
  export let schema;
  export let laskutusosoitteet;
  export let verkkolaskuoperaattorit;

  let resources = Maybe.None();
  let error = Maybe.None();

  const i18n = $_;
  const i18nRoot = 'energiatodistus.laskutus';

  $: disabled =
    energiatodistus.laskutusaika.isSome() || Kayttajat.isLaskuttaja(whoami);

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
    _ => i18n(i18nRoot + '.laatijalaskutus'),
    yritysLabel
  );

  const postiosoite = entity =>
    entity.jakeluosoite +
    ', ' +
    entity.postinumero +
    ' ' +
    entity.postitoimipaikka;

  $: findLaskutusosoite = laskutusosoitteet =>
    R.chain(
      Maybe.findById(R.__, laskutusosoitteet),
      energiatodistus['laskutusosoite-id']
    );

  const verkkolaskuoperaattori = verkkolaskuoperaattorit =>
    R.compose(
      Maybe.orSome(''),
      R.map(et.selectFormat(R.prop('nimi'), verkkolaskuoperaattorit)),
      R.prop('verkkolaskuoperaattori')
    );

  const isVerkkolasku = R.compose(
    R.all(Maybe.isSome),
    R.juxt([R.prop('verkkolaskuoperaattori'), R.prop('verkkolaskuosoite')])
  );
</script>

<H2 text={$_('energiatodistus.laskutus.title')} />

<div class="flex flex-col lg:flex-row lg:space-x-8">
  <div class="lg:w-1/2 w-full py-4">
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
      validators={schema['laskutusosoite-id'].validators}
      items={R.pluck('id', R.filter(R.prop('valid'), laskutusosoitteet))} />
  </div>
</div>
<div class="flex flex-col lg:flex-row lg:space-x-8">
  {#each findLaskutusosoite(laskutusosoitteet).toArray() as osoite}
    {#if isVerkkolasku(osoite)}
      <div class="lg:w-1/2 w-full py-4">
        <Input
          id="energiatodistus.laskutus.verkkolaskuoperaattori"
          name="energiatodistus.laskutus.verkkolaskuoperaattori"
          label={$_('energiatodistus.laskutus.verkkolaskuoperaattori')}
          disabled={true}
          model={verkkolaskuoperaattori(verkkolaskuoperaattorit)(osoite)} />
      </div>
      <div class="lg:w-1/2 w-full py-4">
        <Input
          id="energiatodistus.laskutus.verkkolaskuosoite"
          name="energiatodistus.laskutus.verkkolaskuosoite"
          label={$_('energiatodistus.laskutus.verkkolaskuosoite')}
          disabled={true}
          model={Maybe.orSome('', osoite.verkkolaskuosoite)} />
      </div>
    {:else}
      <div class="lg:w-1/2 w-full py-4">
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
<div class="flex flex-col lg:flex-row lg:space-x-8">
  <div class="lg:w-1/2 w-full py-4">
    <EtInput
      {disabled}
      {schema}
      center={false}
      bind:model={energiatodistus}
      path={['laskuriviviite']} />
  </div>
</div>

<HR />
