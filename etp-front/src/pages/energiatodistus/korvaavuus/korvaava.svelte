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
    @apply text-primary uppercase font-bold text-sm mt-6;
  }
</style>

{#if korvaavaEnergiatodistusId.isSome()}
  <h3>
    {Maybe.exists(ET.isDraft, korvaavaEnergiatodistus)
      ? i18n('energiatodistus.korvaavuus.header.korvaava-draft')
      : i18n('energiatodistus.korvaavuus.header.korvaava')}
  </h3>
  <div class="flex flex-col gap-x-8 mt-2">
    {#if !overlay}
      {#each Maybe.toArray(korvaavaEnergiatodistus) as et}
        <div class="w-full py-4 relative" transition:slide={{ duration: 200 }}>
          <EtTable energiatodistus={et} {whoami} {postinumerot} />
          {#if Maybe.exists(ET.isDraft, korvaavaEnergiatodistus)}
            <p class="text-sm flex mt-2">
              <span class="font-icon mr-1">info</span>
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
