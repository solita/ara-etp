<script>
  import * as R from 'ramda';
  import ET2018Form from './ET2018Form';
  import ET2013Form from './ET2013Form';

  export let version;
  export let energiatodistus;
  export let submit;
  export let disabled = false;
  export let title = '';
</script>

{#if R.equals(version, '2018')}
  <ET2018Form {title} {energiatodistus} {submit} {disabled}/>
{:else if R.equals(version, '2013')}
  <ET2013Form {title} {energiatodistus} {submit} {disabled}/>
{:else}
  <p>Energiatodistusversiota {version} ei ole olemassa.</p>
{/if}