<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';

  import * as api from './energiatodistus-api';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';

  import { flashMessageStore } from '@/stores';
  import { _ } from '@Language/i18n';

  const i18n = $_;
  export let params;

  let resources = Maybe.None();
  let overlay = true;

  const load = id => {
    overlay = true;
    Future.fork(
      response => {
        const msg = i18n(
          Response.notFound(response)
            ? 'energiatodistus.muutoshistoria.not-found'
            : Maybe.orSome(
                'energiatodistus.muutoshistoria.load-error',
                Response.localizationKey(response)
              )
        );

        flashMessageStore.add('Energiatodistus', 'error', msg);
        overlay = false;
      },
      response => {
        resources = Maybe.Some(response);
        overlay = false;
      },
      Future.parallelObject(2, {
        whoami: kayttajaApi.whoami,
        history: api.getEnergiatodistusHistoryById(id)
      })
    );
  };

  load(params.id);
</script>

<style>
</style>

<div class="w-full mt-3">TODO</div>
