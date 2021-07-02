<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';

  import * as api from '@Component/Yritys/yritys-api';

  import { flashMessageStore } from '@/stores';
  import { _ } from '@Language/i18n';
  import { push } from '@Component/Router/router';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import H1 from '@Component/H/H1.svelte';
  import Address from '@Component/Yritys/address';

  const i18n = $_;

  let yritykset = [];
  let overlay = true;

  Future.fork(
    _ => {
      flashMessageStore.add(
        'Yritys',
        'error',
        i18n('laatija.yritykset.error.detach-failed')
      );
      overlay = false;
    },
    response => {
      yritykset = response;
      overlay = false;
    },
    api.getAllYritykset
  );
</script>

<style>
</style>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    <H1 text={i18n('yritykset.title')} />
    <div class="overflow-x-auto">
      <table class="etp-table">
        <thead class="etp-table--thead">
          <tr class="etp-table--tr">
            <th class="etp-table--th">{i18n('yritys.nimi')}</th>
            <th class="etp-table--th">{i18n('yritys.y-tunnus')}</th>
            <th class="etp-table--th">{i18n('yritys.laskutusosoite')}</th>
          </tr>
        </thead>
        <tbody class="etp-table--tbody">
          {#each yritykset as yritys}
            <tr
              class="etp-table--tr etp-table--tr__link"
              on:click={() => push('#/yritys/' + yritys.id)}>
              <td class="etp-table--td">
                {yritys.nimi}
                {Maybe.fold(
                  '',
                  R.concat('/ ', R.__),
                  yritys['vastaanottajan-tarkenne']
                )}
              </td>
              <td class="etp-table--td">{yritys.ytunnus}</td>
              <td class="etp-table--td"><Address address={yritys} /></td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>
  </div>
</Overlay>
