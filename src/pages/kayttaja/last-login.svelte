<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import * as Formats from '@Utility/formats';
  import * as Maybe from '@Utility/maybe-utils';

  export let params;

  const i18n = $_;
  const i18nRoot = 'kayttaja';

  export let kayttaja;
</script>

<style type="text/postcss">
    .last-login {
        @apply text-secondary mb-4;
    }
</style>

<span class="last-login">
  {R.compose(
    Maybe.orSome(i18n('kayttaja.no-login')),
    R.map(R.concat(i18n('kayttaja.last-login') + ' ')),
    R.map(Formats.formatTimeInstant)
  )(kayttaja.login)}
</span>
