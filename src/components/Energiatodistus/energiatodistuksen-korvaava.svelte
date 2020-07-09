<script>
  import { slide } from 'svelte/transition';
  import * as R from 'ramda';

  import { push } from '@Component/Router/router';
  import { _ } from '@Language/i18n';
  import Input from '@Component/Input/Input';

  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';

  import * as EtApi from './energiatodistus-api';
  import * as empty from './empty';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';

  export let korvaavaEnergiatodistusId = Maybe.None();

  let cancel = () => {};
  let overlay = false;

  let korvaavaEnergiatodistus = Maybe.None();

  const fetchEnergiatodistus = id => {
    cancel = R.compose(
      Maybe.cata(
        R.compose(
          R.always(() => {}),
          R.tap(_ => (korvaavaEnergiatodistus = Maybe.None()))
        ),
        R.compose(
          Future.fork(
            _ => {
              overlay = false;
              korvaavaEnergiatodistus = Maybe.None();
            },
            et => {
              overlay = false;
              korvaavaEnergiatodistus = Maybe.Some(et);
            }
          ),
          R.chain(Future.after(200)),
          EtApi.getEnergiatodistusById(fetch, 'all'),
          R.tap(_ => {
            overlay = true;
          })
        )
      ),
      R.tap(cancel)
    )(id);
  };

  $: fetchEnergiatodistus(korvaavaEnergiatodistusId);
</script>

<style type="text/postcss">

</style>

<div
  class="flex flex-col -mx-4 mt-4"
  transition:slide|local={{ duration: 200 }}>
  {#each Maybe.toArray(korvaavaEnergiatodistus) as et}
    <div
      class="w-full px-4 py-4 relative"
      transition:slide|local={{ duration: 200 }}>
      <Overlay {overlay}>
        <div slot="content" class="w-full">
          <table class="etp-table">
            <thead class="etp-table--thead">
              <tr class="etp-table--tr etp-table--tr__light">
                <th class="etp-table--th">
                  {$_('energiatodistus.korvaavuus.tunnus')}
                </th>
                <th class="etp-table--th">
                  {$_('energiatodistus.korvaavuus.etl')}
                </th>
                <th class="etp-table--th">
                  {$_('energiatodistus.korvaavuus.ktl')}
                </th>
                <th class="etp-table--th">
                  {$_('energiatodistus.korvaavuus.rakennustunnus')}
                </th>
                <th class="etp-table--th">
                  {$_('energiatodistus.korvaavuus.nettoala')}
                </th>
                <th class="etp-table--th">
                  {$_('energiatodistus.korvaavuus.nimi')}
                </th>
                <th class="etp-table--th">
                  {$_('energiatodistus.korvaavuus.osoite')}
                </th>
                <th class="etp-table--th">
                  {$_('energiatodistus.korvaavuus.laatija')}
                </th>
              </tr>
            </thead>
            <tbody class="etp-table--tbody">
              <tr
                class="etp-table-tr etp-table--tr__link"
                on:click={() => push('#/energiatodistus/' + et.versio + '/' + et.id)}>
                <td class="etp-table--td">
                  {R.defaultTo(Maybe.getOrElse(' ', korvaavaEnergiatodistusId), et.id)}
                </td>
                <td class="etp-table--td">{''}</td>
                <td class="etp-table--td">
                  {Maybe.orSome('', et.perustiedot.kayttotarkoitus)}
                </td>
                <td class="etp-table--td">
                  {Maybe.orSome('', et.perustiedot.rakennustunnus)}
                </td>
                <td class="etp-table--td">
                  {Maybe.orSome('', et.lahtotiedot['lammitetty-nettoala'])}
                </td>
                <td class="etp-table--td">
                  {Maybe.orSome('', et.perustiedot.nimi)}
                </td>
                <td class="etp-table--td">
                  {Maybe.orSome('', et.perustiedot['katuosoite-fi'])}
                </td>
                <td class="etp-table--td">
                  {Maybe.orSome('', et['laatija-fullname'])}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div slot="overlay-content">
          <Spinner />
        </div>
      </Overlay>
    </div>
  {/each}
</div>
