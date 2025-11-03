<script>
  import { slide } from 'svelte/transition';
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import Spinner from '@Component/Spinner/Spinner';
  import EtTable from './energiatodistus-table';

  import * as EtApi from '../energiatodistus-api';
  import * as ET from '../energiatodistus-utils';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import { announcementsForModule } from '@Utility/announce';

  export let korvaavaEnergiatodistusId = Maybe.None();
  export let whoami;
  export let postinumerot;

  const i18n = $_;
  const { announceError } = announcementsForModule('Energiatodistus');

  let overlay = false;

  let korvaavaEnergiatodistus = Maybe.None();

  const fetchEnergiatodistus = Maybe.cata(
    _ => {
      korvaavaEnergiatodistus = Maybe.None();
    },
    R.compose(
      Future.fork(
        _ => {
          overlay = false;
          korvaavaEnergiatodistus = Maybe.None();
          announceError(i18n('energiatodistus.messages.load-error'));
        },
        response => {
          overlay = false;
          korvaavaEnergiatodistus = Maybe.fromNull(response);
        }
      ),
      Future.delay(200),
      EtApi.findEnergiatodistusById,
      R.tap(_ => {
        overlay = true;
      })
    )
  );

  $: fetchEnergiatodistus(korvaavaEnergiatodistusId);
</script>

<style type="text/postcss">
  h3 {
    @apply mt-6 text-sm font-bold uppercase text-primary;
  }
</style>

{#if korvaavaEnergiatodistusId.isSome()}
  <h3>
    {Maybe.exists(ET.isDraft, korvaavaEnergiatodistus)
      ? i18n('energiatodistus.korvaavuus.header.korvaava-draft')
      : i18n('energiatodistus.korvaavuus.header.korvaava')}
  </h3>
  <div class="mt-2 flex flex-col gap-x-8">
    {#if !overlay}
      {#each Maybe.toArray(korvaavaEnergiatodistus) as et}
        <div class="relative w-full py-4" transition:slide={{ duration: 200 }}>
          <EtTable energiatodistus={et} {whoami} {postinumerot} />
          {#if Maybe.exists(ET.isDraft, korvaavaEnergiatodistus)}
            <p class="mt-2 flex text-sm">
              <span class="mr-1 font-icon">info</span>
              {i18n('energiatodistus.korvaavuus.korvaava-draft-info')}
            </p>
          {/if}
        </div>
      {/each}
    {:else}
      <Spinner />
    {/if}
  </div>
{/if}
