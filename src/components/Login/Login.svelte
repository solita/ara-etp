<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import * as RedirectUtils from '../../utils/redirect-utils';
  import Spinner from '@Component/Spinner/Spinner.svelte';

  export let redirectTimeout = 0;

  /* Remove path /api/logout from redirect url */
  export const currentUrlEncoded =
    encodeURIComponent(R.replace(/api\/logout.*$/, '', document.location.href));

  export const loginUrl = () => `${document.location.protocol}//${document.location.host}/api/login?redirect=${currentUrlEncoded}`;

  RedirectUtils.redirectAfterTimeout(loginUrl(), redirectTimeout);

</script>

<style type="text/postcss">
  .content {
    @apply flex flex-col flex-grow py-8 px-10 mx-auto bg-light;
  }
</style>

<section class="content">
  <div class="bg-success flex items-center py-4 px-4 mx-4 bg-primary text-light rounded-lg">
    <Spinner white={true}/>
    <div class="mx-4">{$_('login_redirect')}</div>
  </div>
</section>
