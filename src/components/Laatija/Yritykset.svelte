<script>
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import H1 from '@Component/H1/H1';
  import Input from '@Component/Input/Input';
  import Table from '@Component/Table/Table';
  import * as api from './laatija-api';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Future from '@Utility/future-utils';

  import { flashMessageStore } from '@/stores';

  export let params;

  let yritykset = [];
  let allYritykset = [];
  let overlay = true;
  let disabled = false;

  $: fields = [
    { id: 'nimi', title: $_('yritys.nimi') },
    { id: 'ytunnus', title: $_('yritys.y-tunnus') }
  ];

  const toggleOverlay = value => () => (overlay = value);
  const toggleDisabled = value => () => (disabled = value);

  const load = R.compose(
    Future.fork(
      R.compose(
        flashMessageStore.add('Yritys', 'error'),
        R.always($_('yritys.messages.load-error')),
        R.tap(toggleDisabled(true)),
        R.tap(toggleOverlay(false)),
      ),
      R.compose(
        result => {
          allYritykset = result[0];
          yritykset = R.map(id => R.find(R.propEq('id', id), allYritykset), result[1]);
        },
        R.tap(toggleOverlay(false))
      )
    ),
    Future.both(api.getAllYritykset(fetch)),
    api.getYritykset(fetch),
  );

  $: load(params.id);

</script>

<style type="text/postcss">
  h2 {
    @apply font-bold uppercase text-lg pb-4;
  }
</style>

<form>
  <div class="w-full mt-3">
    <H1 text="Yritykset" />

    <div class="mb-10">
      <Table {fields} tablecontents={yritykset} />
    </div>

    <h2>Liity yrityksen laatijaksi</h2>

    <p>
      Liitettyjen yritysten laskutusosoitteet ovat käytössäsi
      energiatodistuksen allekirjoitusvaiheessa.
    </p>

    <div class="flex lg:flex-row flex-col py-4 -mx-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <!--Input
            id={'henkilotunnus'}
            name={'henkilotunnus'}
            label={$_('laatija.henkilotunnus')}
            required={true}
            disabled={true} /-->
      </div>
    </div>

    <h2>Luo uusi yritys</h2>


  </div>
</form>