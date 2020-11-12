<script>
  import { _ } from '@Language/i18n';
  import * as RedirectUtils from '../../utils/redirect-utils'

  export let redirectTimeout = 0;

  export const currentUrlEncoded = encodeURIComponent(document.location.href);

  export const loginUrl = () => `${document.location.protocol}//${document.location.host}/api/login?redirect=${currentUrlEncoded}`;

  RedirectUtils.redirectAfterTimeout(loginUrl(), redirectTimeout);

</script>

<style type="text/postcss">
    .content {
        @apply flex flex-col flex-grow py-8 px-10 mx-auto bg-light;
    }
</style>

<section class="content">
  <p class="bg-success flex justify-around py-4 px-4 bg-primary text-light rounded-lg">
    <span class="font-icon pr-4">check_circle_outline</span>
    {$_('login_redirect')}
  </p>
</section>
