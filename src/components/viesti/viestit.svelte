<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Formats from '@Utility/formats';

  import * as api from './viesti-api';
  import * as Viestit from './viesti-util';

  import { flashMessageStore } from '@/stores';
  import { _ } from '@Language/i18n';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import H1 from '@Component/H/H1.svelte';
  import Link from '../Link/Link.svelte';
  import Viestiketju from './viestiketju';

  let ketjut = [];
  let overlay = true;

  Future.fork(
    response => {
      const msg = $_(
        Maybe.orSome(
          'viesti.all.messages.load-error',
          Response.localizationKey(response)
        )
      );

      flashMessageStore.add('viesti', 'error', msg);
      overlay = false;
    },
    response => {
      ketjut = response;
      overlay = false;
    },
    api.getKetjut(fetch)
  );

  $: formatSender = Viestit.formatSender($_);

  const sentTime = R.compose(R.prop('senttime'), R.last, R.prop('viestit'));
</script>

<style>
  .header > * {
    @apply block font-bold text-primary uppercase text-left tracking-wider bg-light text-sm;
  }
</style>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    <H1 text={$_('viesti.all.title')} />
    <div class="flex flex-col">
      <div class="flex header">
        <span class="w-1/6">{$_('viesti.all.senttime')}</span>
        <span class="w-1/3">{$_('viesti.all.osallistujat')}</span>
        <span class="w-1/2">{$_('viesti.all.content')}</span>
      </div>
    </div>
    {#each ketjut as ketju}
      <Viestiketju {ketju} />
    {/each}
    <!--<table class="etp-table">
      <thead class="etp-table--thead">
        <tr class="etp-table--tr">
          <th class="etp-table--th">{$_('viesti.all.senttime')}</th>
          <th class="etp-table--th">{$_('viesti.all.osallistujat')}</th>
          <th class="etp-table--th">{$_('viesti.all.content')}</th>
        </tr>
      </thead>
      <tbody class="etp-table--tbody">
        {#each ketjut as ketju}
          <tr
            class="etp-table--tr etp-table--tr__link"
            on:click={() => push('#/viesti/' + ketju.id)}>
          <td class="etp-table--td">
            <p>{sentTime(ketju)}</p>
          </td>
          <td class="etp-table--td">
            {formatSender(R.last(ketju.viestit)['from'])}
            {#if R.length(ketju.viestit) > 1}
              <p>{R.length(ketju.viestit)} viestiä</p>
            {/if}
          </td>
          <td class="etp-table--td">
            {ketju.subject} - {R.last(ketju.viestit).body}
          </td>
        </tr>
      {/each}
      </tbody>
    </table>-->

    <div class="flex mt-5">
      <Link text={'Lisää uusi ketju'} href="#/viesti/new" />
    </div>
  </div>
</Overlay>
