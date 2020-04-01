<script>
  import * as R from 'ramda';
  import * as Future from '@Utility/future-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Error from '@Utility/error-utils';
  import { currentUserStore, flashMessageStore, errorStore } from '@/stores';
  import * as KayttajaUtils from './kayttaja-utils';

  Future.fork(
    R.compose(
      errorStore.set,
      Error.httpError({})
    ),
    R.compose(
      currentUserStore.set,
      Maybe.of
    ),
    KayttajaUtils.currentKayttajaFuture(fetch)
  );
</script>
