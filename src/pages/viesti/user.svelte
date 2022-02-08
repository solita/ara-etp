<script>
  import * as R from 'ramda';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as Viestit from '@Pages/viesti/viesti-util';

  import { _ } from '@Language/i18n';

  export let user;
  export let whoami;
  export let icons = false;

  const i18n = $_;
  const formatUser = Viestit.formatUser(i18n);
</script>

<style>
</style>

{#if icons}
  {#if R.propSatisfies(Kayttajat.isSystemRole, 'rooli-id', user)}
    <span class="font-icon-outlined">cloud</span>
  {:else}
    <span class="font-icon">person</span>
  {/if}
{/if}
{#if R.eqProps('id', user, whoami)}
  <span class="text-primary font-bold">
    {$_('viesti.ketju.existing.self')}
  </span>
{:else}
  <span>
    {formatUser(user)}
  </span>
{/if}
