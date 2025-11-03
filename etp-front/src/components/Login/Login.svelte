<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import * as RedirectUtils from '../../utils/redirect-utils';
  import Spinner from '@Component/Spinner/Spinner.svelte';

  export let redirectTimeout = 0;
  export let disableAnimation = false;

  /* Remove path /api/logout from redirect url */
  export const currentUrlEncoded = encodeURIComponent(
    R.replace(/api\/logout.*$/, '', document.location.href)
  );

  export const loginUrl = () =>
    `${document.location.protocol}//${document.location.host}/api/login?redirect=${currentUrlEncoded}`;

  RedirectUtils.redirectAfterTimeout(loginUrl(), redirectTimeout);
</script>

<style type="text/postcss">
  .content {
    @apply mx-auto flex flex-grow flex-col bg-light px-10 py-8;
  }
</style>

<section class="content">
  <div
    class="mx-4 flex items-center rounded-lg bg-primary bg-success px-4 py-4 text-light">
    <Spinner white={true} {disableAnimation} />
    <div class="mx-4">{$_('login_redirect')}</div>
  </div>
</section>
