<script>
  import * as R from 'ramda';
  import * as Future from '@Utility/future-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as api from './energiatodistus-api';

  import { _ } from '@Language/i18n';
  import { flashMessageStore, breadcrumbStore } from '@/stores';
  import { push } from '@Component/Router/router';

  import H1 from '@Component/H/H1';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';
  import Link from '@Component/Link/Link';
  import Select from '@Component/Select/Select';
  import Confirm from '@Component/Confirm/Confirm';

  let overlay = true;
  let failure = false;
  let energiatodistukset = [];

  const toggleOverlay = value => () => (overlay = value);
  const orEmpty = Maybe.orSome('');

  let query = {
    tila: Maybe.None()
  };

  const formatTila = R.compose(
    Maybe.orSome($_('validation.no-selection')),
    Maybe.map(tila => $_(`energiatodistukset.tilat.` + tila)),
    R.when(R.complement(Maybe.isMaybe), Maybe.of)
  );

  const toETView = (versio, id) => {
    push('#/energiatodistus/' + versio + '/' + id);
  };

  const removeEnergiatodistusFromList = R.curry((versio, id) =>
    R.filter(
      R.compose(
        R.not,
        R.allPass([R.propEq('versio', versio), R.propEq('id', id)])
      )
    )
  );

  const deleteEnergiatodistus = (versio, id) => {
    Future.fork(
      _ =>
        flashMessageStore.add(
          'Energiatodistus',
          'error',
          $_('energiatodistukset.messages.delete-error')
        ),
      _ => {
        energiatodistukset = removeEnergiatodistusFromList(versio, id)(
          energiatodistukset
        );

        flashMessageStore.add(
          'Energiatodistus',
          'success',
          $_('energiatodistukset.messages.delete-success')
        );
      },
      api.deleteEnergiatodistus(fetch, versio, id)
    );
  };

  $: R.compose(
    Future.fork(
      R.compose(
        R.tap(toggleOverlay(false)),
        R.tap(flashMessageStore.add('Energiatodistus', 'error')),
        R.always($_('energiatodistus.messages.load-error'))
      ),
      R.compose(
        response => {
          energiatodistukset = response[0];
        },
        R.tap(toggleOverlay(false))
      )
    ),
    Future.parallel(5),
    R.tap(toggleOverlay(true))
  )([api.getEnergiatodistukset(query), api.laatimisvaiheet]);
</script>

<style>

</style>

<div class="w-full mt-3">
  <H1 text={$_('energiatodistukset.title')} />
  <Overlay {overlay}>
    <div slot="content">
      <div class="lg:w-1/3 w-full mb-6">
        <Select
          label={'Tila'}
          disabled={false}
          bind:model={query}
          lens={R.lensProp('tila')}
          format={formatTila}
          parse={R.when(R.complement(Maybe.isMaybe), Maybe.of)}
          items={[Maybe.None(), 0, 1]} />
      </div>

      {#if R.isEmpty(energiatodistukset)}
        <p class="mb-10">
          Energiatodistuksia ei löydy annetuilla hakuehdoilla tai sinulle ei ole
          yhtään energiatodistusta.
        </p>
      {:else}
        <div class="mb-10">
          <table class="etp-table">
            <thead class="etp-table--thead">
              <tr class="etp-table--tr etp-table--tr__light">
                <th class="etp-table--th">Tila</th>
                <th class="etp-table--th">Tunnus</th>
                <th class="etp-table--th">ETL</th>
                <th class="etp-table--th">Versio</th>
                <th class="etp-table--th">Voimassa</th>
                <th class="etp-table--th">Rakennuksen nimi</th>
                <th class="etp-table--th">Osoite</th>
                <th class="etp-table--th">Laatija</th>
                <th class="etp-table--th">Toiminnot</th>
              </tr>
            </thead>
            <tbody class="etp-table--tbody">
              {#each energiatodistukset as energiatodistus}
                <tr
                  class="etp-table--tr etp-table--tr__link"
                  on:click={toETView(energiatodistus.versio, energiatodistus.id)}>
                  <td class="etp-table--td">Luonnos</td>
                  <td class="etp-table--td">{energiatodistus.id}</td>
                  <td class="etp-table--td">C</td>
                  <td class="etp-table--td">{energiatodistus.versio}</td>
                  <td class="etp-table--td">13.3.2030</td>
                  <td class="etp-table--td">
                    {orEmpty(energiatodistus.perustiedot.nimi)}
                  </td>
                  <td class="etp-table--td">
                    {orEmpty(energiatodistus.perustiedot['katuosoite-fi'])}
                  </td>
                  <td class="etp-table--td">
                    {orEmpty(energiatodistus['laatija-fullname'])}
                  </td>
                  <td class="etp-table--td etp-table--td__center">
                    <Confirm
                      let:confirm
                      confirmButtonLabel={$_('confirm.button.delete')}
                      confirmMessage={$_('confirm.you-want-to-delete')}>
                      <span
                        class="material-icons"
                        on:click|stopPropagation={_ => confirm(deleteEnergiatodistus, energiatodistus.versio, energiatodistus.id)}>
                        delete
                      </span>
                    </Confirm>
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
  <p class="mb-4">Uuden energiatodistuksen voit lisätä täältä:</p>
  <div class="mb-4 flex lg:flex-row flex-col">
    <div class="flex flex-row mb-4 mr-4">
      <span class="material-icons">add</span>
      &nbsp;
      <Link
        text={'Luo uusi 2018 energiatodistus'}
        href="#/energiatodistus/2018/new" />
    </div>
    <div class="flex flex-row mb-4 mr-4">
      <span class="material-icons">add</span>
      &nbsp;
      <Link
        text={'Luo uusi 2013 energiatodistus'}
        href="#/energiatodistus/2013/new" />
    </div>
  </div>
  <div class="flex flew-row mb-4 mr-4">
    <span class="material-icons">attachment</span>
    &nbsp;
    <Link
      text={'Lataa energiatodistukset XLSX-tiedostona'}
      href="/api/private/energiatodistukset/xlsx/energiatodistukset.xlsx" />
  </div>
</div>
