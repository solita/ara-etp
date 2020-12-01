<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Formats from '@Utility/formats';

  import * as api from './yritys-api';
  import * as laatijaApi from '@Component/Laatija/laatija-api';
  import * as Tila from './laatija-yritys-tila';

  import { _ } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';

  import H1 from '@Component/H/H1.svelte';
  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import Confirm from '@Component/Confirm/Confirm.svelte';
  import Link from '@Component/Link/Link.svelte';

  export let params;

  let overlay = false;
  let laatijat = [];

  const load = R.compose(
    Future.fork(
      response => {
        overlay = false;
        flashMessageStore.add('Yritys', 'error',
          $_(Maybe.orSome('yritys.messages.load-error',
            Response.localizationKey(response))));
      },
      response => {
        laatijat = R.filter(R.complement(Tila.isDeleted), response);
        overlay = false;
      }
    ),
    api.getLaatijatById(fetch)
  );

  load(params.id);

  const detach = id => {
    Future.fork(
      _ =>
        flashMessageStore.add(
          'Yritys',
          'error',
          $_('laatija.yritykset.error.detach-failed')
        ),
      _ => {
        load(params.id);
        flashMessageStore.add(
          'Yritys',
          'success',
          $_('laatija.yritykset.success.detach')
        );
      },
      laatijaApi.deleteLaatijaYritys(fetch, id, params.id)
    );
  };

  const accept = id =>
    Future.fork(
      _ =>
        flashMessageStore.add(
          'Yritys',
          'error',
          $_('yritys.laatijat.accept.error')
        ),
      _ => {
        load(params.id);
        flashMessageStore.add(
          'Yritys',
          'success',
          $_('yritys.laatijat.accept.success')
        );
      },
      api.putAcceptedLaatijaYritys(fetch, id, params.id)
    );
</script>

<style>

</style>

<Overlay {overlay}>
  <div slot="content">
    <H1 text="Yrityksen laatijat" />

    <table class="etp-table">
      <thead class="etp-table--thead">
      <tr class="etp-table--tr etp-table--tr__light">
        <th class="etp-table--th">
          {$_('yritys.laatijat.table.laatija')}
        </th>
        <th class="etp-table--th">
          {$_('yritys.laatijat.table.liitetty')}
        </th>
        <th class="etp-table--th">
          {$_('yritys.laatijat.table.hyvaksyja')}
        </th>
        <th class="etp-table--th">
          {$_('yritys.laatijat.table.delete')}
        </th>
      </tr>
      </thead>
      <tbody class="etp-table--tbody">
      {#each laatijat as laatija}
      <tr class="etp-table-tr">
        <td class="etp-table--td">
          {laatija.etunimi} {laatija.sukunimi}
        </td>
        <td class="etp-table--td">
          {#if Tila.isProposal(laatija)}
            <Confirm let:confirm
                confirmButtonLabel={$_('yritys.laatijat.accept.confirm-button')}
                confirmMessage={$_('yritys.laatijat.accept.confirm-message')}>

              <div class="flex"
                  on:click|stopPropagation={_ => confirm(accept, laatija.id)}>
                <Link icon={Maybe.Some('person_add')}
                      text={$_('yritys.laatijat.accept.link')}/>
              </div>
            </Confirm>
          {:else}
            {Formats.formatTimeInstant(laatija.modifytime)}
          {/if}
        </td>
        <td class="etp-table--td">
          {#if Tila.isAccepted(laatija)}
            {laatija['modifiedby-name']}
          {/if}
        </td>
        <td class="etp-table--td">
          <Confirm
              let:confirm
              confirmButtonLabel={$_('yritys.laatijat.delete.confirm-button')}
              confirmMessage={$_('yritys.laatijat.delete.confirm-message')}>
              <span
                  class="material-icons cursor-pointer"
                  on:click|stopPropagation={_ => confirm(detach, laatija.id)}>
                delete
              </span>
          </Confirm>
        </td>
      </tr>
      {/each}
      </tbody>
    </table>
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
