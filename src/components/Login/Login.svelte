<script>
  import * as RedirectUtils from '../../utils/redirect-utils'

  export let redirectTimeout = 0;

  // TODO Real client id
  //export const appClientId = CONFIG.COGNITOCLIENTID;
  export const appClientId = "34kgobivb08mdl2po9vgb50m4v";

  // TODO this should also come from config.json similar to client id.
  export const loginPageBaseUrl = "https://kehitys-energiatodistuspalvelu-com.auth.eu-central-1.amazoncognito.com/login";

  // TODO remember to handle trailing slash
  export const currentPage = encodeURIComponent(document.location.href);

  // TODO should include state query parameter to cognito and verify it upon returning.
  export const loginPageUrl = () => `${loginPageBaseUrl}?client_id=${appClientId}&redirect_uri=${currentPage}&response_type=code`;

  // TODO should come from config.json as well.
  export const tokenUrl = code => `https://kehitys-energiatodistuspalvelu-com.auth.eu-central-1.amazoncognito.com/oauth2/token?grant_type=authorization_code&client_id=${appClientId}&redirect_uri=${currentPage}&code=${code}`

  export const urlParams = new URLSearchParams(window.location.search);
  export const oauthCode = urlParams.get('code');

  // Test code for seeing how Cognito works...
  if(!oauthCode) {
    RedirectUtils.redirectAfterTimeout(loginPageUrl(), redirectTimeout);
  }
  else {
    const response = fetch(tokenUrl(oauthCode),
        {method: 'POST',
         headers: {'Content-Type': 'application/x-www-form-urlencoded'}})
      .then(function(response) {
        console.log(response);
        document.cookie = `AWSELBAuthSessionCookie=${response.body.access_token}`
      })
      .catch(function(e) {
        console.log(e);
      });
  }

</script>

<style type="text/postcss">
</style>

<span>Odota hetki, sinut ohjataan kirjautumissivulle...</span>
