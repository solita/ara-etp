<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';

  import * as Kayttajat from '@Utility/kayttajat';
  import Link from '@Component/Link/Link';

  export let energiatodistus;
  export let whoami;

  $: laatijaFullName = R.compose(
    Maybe.orSome(''),
    R.prop('laatija-fullname')
  )(energiatodistus);

  $: laatijaId = R.compose(
    Maybe.orSome(''),
    R.prop('laatija-id')
  )(energiatodistus);
</script>

<span>{$_('energiatodistus.laatija-fullname')}</span>
<div class="flex">
  {#if Kayttajat.isPaakayttaja(whoami)}
    <Link text={laatijaFullName} href={`#/kayttaja/${laatijaId}`} />
  {:else}
    {laatijaFullName}
  {/if}
</div>
