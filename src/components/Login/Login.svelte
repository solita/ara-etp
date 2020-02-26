<script>
  import * as RedirectUtils from '../../utils/redirect-utils'

  export let redirectTimeout = 0;

  RedirectUtils.redirectAfterTimeout(RedirectUtils.loginPageUrl(), redirectTimeout);
</script>

<style type="text/postcss">
</style>

<span>Odota hetki, sinut ohjataan kirjautumissivulle...</span>
