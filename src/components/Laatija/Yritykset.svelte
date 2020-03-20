<script>
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import H1 from '@Component/H1/H1';
  import Input from '@Component/Input/Input';
  import Table from '@Component/Table/Table';
  import Button from '@Component/Button/Button';
  import Link from '@Component/Link/Link';
  import Autocomplete from '@Component/Autocomplete/Autocomplete';
  import * as api from './laatija-api';
  import * as yritys from './yritys';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Future from '@Utility/future-utils';

  import { flashMessageStore } from '@/stores';

  export let params;

  let newLaatijaYritys = Either.Right(Maybe.None());
  let laatijaYritykset = [];
  let allYritykset = [];
  let overlay = true;
  let disabled = false;

  const actions = [{type: 'remove', update: R.tap(txt => console.log('Poista ', txt))}]
  $: fields = [
    { id: 'nimi', title: $_('yritys.nimi') },
    { id: 'ytunnus', title: $_('yritys.y-tunnus') },
    { title: 'Toiminnot', type: 'action', actions: actions }
  ];

  const toggleOverlay = value => () => (overlay = value);
  const toggleDisabled = value => () => (disabled = value);

  const parseYritys = name => R.isEmpty(R.trim(name)) ?
    Either.Right(Maybe.None()) :
    yritys.findYritys(allYritykset, name)
      .toEither(R.applyTo('laatija.yritykset.yritys-not-found'))
      .map(Maybe.of);

  $: yritysKeywords = R.map(R.prop('nimi'), allYritykset);

  const load = R.compose(
    Future.fork(
      R.compose(
        flashMessageStore.add('Laatija', 'error'),
        R.always($_('errors.load-error')),
        R.tap(toggleDisabled(true)),
        R.tap(toggleOverlay(false)),
      ),
      R.compose(
        result => {
          allYritykset = result[0];
          laatijaYritykset = R.map(id => R.find(R.propEq('id', id), allYritykset), result[1]);
        },
        R.tap(toggleOverlay(false))
      )
    ),
    Future.both(api.getAllYritykset(fetch)),
    api.getYritykset(fetch),
  );

  $: load(params.id);

  function attach() {
    newLaatijaYritys.flatMap(Maybe.toEither(R.applyTo('laatija.yritykset.error.select-yritys')))
      .leftMap(R.applyTo($_)).cata(
        flashMessageStore.add('Laatija', 'error'),
        yritys => {
          Future.fork(
            response => {
              flashMessageStore.add('Laatija', 'error', $_('laatija.yritykset.error.attach-failed'))
            },
            response => {
              load(params.id);
              flashMessageStore.add('Laatija', 'success', $_('laatija.yritykset.success.attach'));
            },
            api.putLaatijaYritys(fetch, params.id, yritys.id));
        });
  }
</script>

<style type="text/postcss">
  h2 {
    @apply font-bold uppercase text-lg pb-4;
  }
</style>

<form>
  <div class="w-full mt-3">
    <H1 text="Yritykset" />

    <p class="mb-5">
      Sinut on liitetty laatijaksi seuraaviin yrityksiin:
    </p>

    <div class="mb-10">
      <Table {fields} tablecontents={laatijaYritykset} />
    </div>

    <h2>Liity yrityksen laatijaksi</h2>

    <p>
      Liitettyjen yritysten laskutusosoitteet ovat käytössäsi
      energiatodistuksen allekirjoitusvaiheessa.
    </p>

    <form class="mb-5" on:submit|preventDefault={attach}>
      <div class="flex lg:flex-row flex-col py-4 -mx-4">
        <div class="lg:w-1/2 lg:py-0 w-full px-4 py-4">
          <Autocomplete items={yritysKeywords}>
            <Input
              id={'yritys'}
              name={'yritys'}
              label={$_('laatija.yritykset.find-yritys')}
              required={false}
              {disabled}
              bind:model={newLaatijaYritys}
              format={R.compose(Maybe.orSome(''), R.map(R.prop('nimi')))}
              parse={parseYritys}
              lens={R.lens(R.identity, R.identity)}
              i18n={$_}/>
          </Autocomplete>
        </div>
        <div class="self-end">
          <Button type={'submit'} text={$_('laatija.yritykset.attach-to-yritys')} />
        </div>
      </div>

    </form>

    <h2>Luo uusi yritys</h2>

    <p class="mb-5">
      Jos yritystä ei löydy energiatodistuspalvelusta, voit lisätä sen itse.
    </p>

    <Link icon={'add_circle_outline'} text={'Lisää uusi yritys'} href='#/yritys/new'/>
  </div>
</form>