<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import { replace } from 'svelte-spa-router';
  import { currentUserStore } from '@/stores';
  import { _ } from '@Language/i18n';
  import * as Navigation from '@Utility/navigation';

  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';
  import * as versionApi from '@Component/Version/version-api';

  let whoami = Maybe.None();
  let flags = Maybe.None();
  let failure = Maybe.None();

  Future.fork(
    response => {
      failure = Maybe.Some(response);
    },
    response => {
      whoami = Maybe.Some(response[0]);
      flags = Maybe.Some(response[1].flags);
    },
    Future.parallel(2, [kayttajaApi.whoami, versionApi.getVersion])
  );

  $: R.forEach(
    R.compose(
      replace,
      R.prop('href'),
      R.head,
      Navigation.parseRoot(Maybe.orSome({ viestit: false }, flags), $_)
    ),
    whoami
  );
</script>
