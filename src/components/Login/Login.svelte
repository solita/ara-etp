<script>
  import * as RedirectUtils from '../../utils/redirect-utils'

  export let redirectTimeout = 0;

  export const currentUrlEncoded = encodeURIComponent(document.location.href);

  export const loginUrl = () => `${document.location.protocol}//${document.location.host}/api/login?redirect=${currentUrlEncoded}`;

  RedirectUtils.redirectAfterTimeout(loginUrl(), redirectTimeout);

</script>

<style type="text/postcss">
</style>

<span>Odota hetki, sinut ohjataan kirjautumissivulle...</span>
