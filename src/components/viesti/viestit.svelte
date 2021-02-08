<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';

  import * as api from './viesti-api';

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

    <div class="flex mt-5">
      <Link text={'Lisää uusi ketju'} href="#/viesti/new" />
    </div>
  </div>
</Overlay>
