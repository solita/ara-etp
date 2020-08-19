<script>
  import * as R from 'ramda';
  import {_} from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';
  import * as et from './energiatodistus-utils';
  import * as localstorage from './local-storage';

  import H2 from '@Component/H/H2';
  import Select from '@Component/Select/Select';
  import * as Future from "@Utility/future-utils";
  import * as api from '@Component/Energiatodistus/energiatodistus-api';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';
  import * as laskutusApi from '@Component/Laskutus/laskutus-api';
  import {flashMessageStore} from "@/stores";
  import Overlay from "../Overlay/Overlay.svelte";
  import Spinner from "../Spinner/Spinner.svelte";
  import SimpleInput from '@Component/Input/SimpleInput';
  import Input from './Input';

  export let energiatodistus;
  export let disabled;
  export let schema;

  let laatijaYritykset = [];
  let verkkolaskuoperaattorit = [];
  let overlay = true;
  const toggleOverlay = value => { overlay = value };

  R.compose(
    Future.fork(
      () => {
        toggleOverlay(false);
        flashMessageStore.add('Energiatodistus', 'error',
          $_('energiatodistus.messages.load-error'));
      },
      response => {
        laatijaYritykset = response[1];
        verkkolaskuoperaattorit = response[0];

        localstorage.getDefaultLaskutettavaYritysId().forEach(
          id => {
            if (R.isNil(energiatodistus.id) &&
              energiatodistus['laskutettava-yritys-id'].isNone() &&
              R.any(R.propEq('id', id), laatijaYritykset)) {

              energiatodistus = R.assoc('laskutettava-yritys-id',
                Maybe.Some(id),
                energiatodistus);
            }
          }
        );

        toggleOverlay(false);
      },
    ),
    Future.both(laskutusApi.verkkolaskuoperaattorit),
    Maybe.orSome(Future.chain(
      whoami => api.getLaatijaYritykset(fetch, whoami.id),
      kayttajaApi.whoami)),
    R.map(api.getLaatijaYritykset(fetch)),
    R.tap(() => toggleOverlay(true)),
    R.prop('laatija-id')
  ) (energiatodistus)

  const yritysLabel = yritys => yritys.nimi + ' | ' + yritys.ytunnus +
    R.compose(
      Maybe.orSome(''),
      R.map(R.concat(' | ')),
      R.prop('vastaanottajan-tarkenne')
    )(yritys)

  const verkkolaskuoperaattori = R.compose(
    R.map(et.selectFormat(R.prop('nimi'), verkkolaskuoperaattorit)),
    R.prop('verkkolaskuoperaattori')
  );

  const verkkolaskuosoite = R.compose(
    R.map(R.join(' | ')),
    Maybe.toMaybeList,
    R.juxt([verkkolaskuoperaattori, R.prop('verkkolaskuosoite')])
  )

  const postiosoite = yritys =>
    yritys.jakeluosoite + ', ' +
    yritys.postinumero + ' ' + yritys.postitoimipaikka

  const osoite = yritys => Maybe.orSome(
    postiosoite(yritys),
    verkkolaskuosoite(yritys));

</script>
<H2 text={'* ' + $_('energiatodistus.laskutus.title')} />

<Overlay {overlay}>
  <div slot="content">
    <div class="flex flex-col lg:flex-row -mx-4">
      <div class="lg:w-1/2 w-full px-4 py-4">
        <Select
            label={$_('energiatodistus.laskutus.laskutettava')}
            required={true}
            allowNone={true}
            noneLabel={'energiatodistus.laskutus.laatijalaskutus'}
            {disabled}
            bind:model={energiatodistus}
            lens={R.lensPath(['laskutettava-yritys-id'])}
            parse={Maybe.Some}
            format={et.selectFormat(yritysLabel, laatijaYritykset)}
            items={R.pluck('id', laatijaYritykset)} />
      </div>
      {#each energiatodistus['laskutettava-yritys-id'].toArray() as id }
      <div class="lg:w-1/2 w-full px-4 py-4">
        <SimpleInput
          id="energiatodistus.laskutus.osoite"
          name="energiatodistus.laskutus.osoite"
          label={$_('energiatodistus.laskutus.osoite')}
          disabled={true}
          viewValue={et.selectFormat(osoite, laatijaYritykset)(id)}/>
      </div>
      {/each}
    </div>
    <div class="flex flex-col lg:flex-row -mx-4 my-4">
      <div class="lg:w-1/2 w-full px-4 py-4">
        <Input
            {disabled}
            {schema}
            center={false}
            bind:model={energiatodistus}
            path={['laskuriviviite']} />
      </div>
    </div>
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>