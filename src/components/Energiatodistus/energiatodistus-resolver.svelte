<script>
  import * as R from 'ramda';
  import * as Future from '@Utility/future-utils';
  import * as Maybe from '@Utility/maybe-utils';

  import * as EtApi from './energiatodistus-api';

  import { _ } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';
  import * as Response from '@Utility/response';

  import { replace, location } from 'svelte-spa-router';

  import Spinner from '@Component/Spinner/Spinner';

  export let params;

  let overlay = true;

  Future.fork(
    response => {
      const msg = Response.notFound(response)
        ? $_('energiatodistus.messages.not-found')
        : $_(
            Maybe.orSome(
              'energiatodistus.messages.load-error',
              Response.localizationKey(response)
            )
          );
      overlay = false;
      flashMessageStore.add('Energiatodistus', 'error', msg);
    },
    energiatodistus => {
      replace(
        `#/energiatodistus/${energiatodistus.versio}/${
          energiatodistus.id
        }${R.compose(
          R.when(R.length, R.concat('/')),
          R.join('/'),
          R.drop(3),
          R.split('/')
        )($location)}`
      );
    },
    EtApi.getEnergiatodistusById('all', params.id)
  );
</script>

{#if overlay}
  <div class="flex justify-center"><Spinner /></div>
{/if}
