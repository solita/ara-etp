<script>
  import { slide } from 'svelte/transition';
  import * as R from 'ramda';

  import { push } from '@Component/Router/router';
  import { _ } from '@Language/i18n';

  import Spinner from '@Component/Spinner/Spinner';
  import EtTable from './energiatodistus-table';

  import * as EtApi from '../energiatodistus-api';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';

  export let korvaavaEnergiatodistusId = Maybe.None();
  export let postinumerot;

  let overlay = false;

  let korvaavaEnergiatodistus = Maybe.None();

  const fetchEnergiatodistus = Maybe.cata(
    _ => {
      korvaavaEnergiatodistus = Maybe.None()
    },
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
    ));

  $: fetchEnergiatodistus(korvaavaEnergiatodistusId);
</script>

<style type="text/postcss">
h3 {
    @apply text-primary uppercase font-bold text-sm mt-6;
}
</style>

{#if korvaavaEnergiatodistusId.isSome()}
  <h3> {$_('energiatodistus.korvaavuus.header.korvaava')} </h3>
  <div class="flex flex-col -mx-4 mt-2">
    {#if !overlay}
      {#each Maybe.toArray(korvaavaEnergiatodistus) as et}
        <div class="w-full px-4 py-4 relative"
            transition:slide|local={{ duration: 200 }}>
          <EtTable energiatodistus={et} {postinumerot}/>
        </div>
      {/each}
    {:else}
      <Spinner/>
    {/if}
  </div>
{/if}