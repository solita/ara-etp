<script>
  import * as R from 'ramda';
  import * as Future from '@Utility/future-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as api from './energiatodistus-api';

  import { _ } from '@Language/i18n';
  import { flashMessageStore, breadcrumbStore } from '@/stores';

  import H1 from '@Component/H/H1';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';
  import Link from '@Component/Link/Link';

  let overlay = true;
  let failure = false;
  let energiatodistukset = [];

  const toggleOverlay = value => () => (overlay = value);
  const orEmpty = Maybe.orSome('');

  const changeLocationToET = (versio, id) => {
    window.location.assign('#/energiatodistus/' + versio + '/' + id);
  }
  $: R.compose(
      Future.fork(
          R.compose(
              R.tap(toggleOverlay(false)),
              R.tap(flashMessageStore.add('EnergiaTodistus', 'error')),
              R.always($_('energiatodistus.messages.load-error'))),
          R.compose(
              response => {
                energiatodistukset = response[0];
              },
              R.tap(toggleOverlay(false))
          )),
      Future.parallel(5),
      R.tap(toggleOverlay(true))
  )([api.getEnergiatodistukset,
     api.laatimisvaiheet]);
</script>

<style>
  table {
    @apply w-full;
  }

  th {
    @apply px-4 py-2 text-center;
  }

  tr {
    @apply px-4 py-2;
  }

  td {
    @apply text-center;
  }

  tr:nth-child(even) {
    @apply bg-background;
  }
</style>

<div class="w-full mt-3">
  <H1 text={$_('energiatodistukset.title')} />

  <Overlay {overlay}>
    <div slot="content">
      {#if R.isEmpty(energiatodistukset)}
        <p class="mb-10">
          Energiatodistuksia ei löydy annetuilla hakuehdoilla
          tai sinulle ei ole yhtään energiatodistusta.
        </p>
      {:else}
        <div class="mb-10">
          <table>
            <thead>
            <tr>
              <th>Tila</th><th>Tunnus</th><th>ETL</th>
              <th>Versio</th><th>Voimassa</th>
              <th>Rakennuksen nimi</th><th>Osoite</th>
              <th>Laatija</th>
              <th>Toiminnot</th>
            </tr>
            </thead>
            <tbody>
              {#each energiatodistukset as energiatodistus}
                <tr class="cursor-pointer"
                    on:click={changeLocationToET(energiatodistus.versio, energiatodistus.id)}>
                  <td>Luonnos</td>
                  <td>{energiatodistus.id}</td>
                  <td>C</td>
                  <td>{energiatodistus.versio}</td>
                  <td>13.3.2030</td>
                  <td>{orEmpty(energiatodistus.perustiedot.nimi)}</td>
                  <td>{orEmpty(energiatodistus.perustiedot['katuosoite-fi'])}</td>
                  <td>{orEmpty(energiatodistus['laatija-fullname'])}</td>
                  <td>
                    <span class="material-icons">delete</span>
                  </td>
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
      {/if}
    </div>
    <div slot="overlay-content">
      <Spinner />
    </div>
  </Overlay>
  <p class="mb-4">Uuden energiatodistuksen voit lisätä täältä: </p>
  <div class="mb-4 flex lg:flex-row flex-col">
    <div class="flex flex-row mb-4  mr-4">
      <span class="material-icons">
        add
      </span> &nbsp;
      <Link text={'Luo uusi 2018 energiatodistus'}
            href='#/energiatodistus/2018/new'/>
    </div>
    <div class="flex flex-row">
      <span class="material-icons">
        add
      </span> &nbsp;
      <Link text={'Luo uusi 2013 energiatodistus'}
            href='#/energiatodistus/2013/new'/>
    </div>
  </div>
</div>