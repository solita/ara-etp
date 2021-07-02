<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import { replace } from 'svelte-spa-router';
  import { _ } from '@Language/i18n';
  import * as Navigation from '@Utility/navigation';

  import * as kayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as versionApi from '@Component/Version/version-api';

  let failure = Maybe.None();

  let resources = Maybe.None();

  Future.fork(
    response => {
      failure = Maybe.Some(response);
    },
    response => {
      resources = Maybe.Some(response);
    },
    Future.parallelObject(2, {
      whoami: kayttajaApi.whoami,
      isDev: R.map(R.prop('isDev'), versionApi.getConfig)
    })
  );

  $: R.forEach(
    ({ whoami, isDev }) =>
      R.compose(
        replace,
        R.prop('href'),
        R.head,
        Navigation.parseRoot(false, $_)
      )(whoami),
    resources
  );
</script>
