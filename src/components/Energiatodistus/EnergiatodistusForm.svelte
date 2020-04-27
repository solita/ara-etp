<script>
  import * as R from 'ramda';
  import ET2018Form from './ET2018Form';
  import ET2013Form from './ET2013Form';
  import ToolBar from '@Component/ToolBar/ToolBar';

  export let version;
  export let energiatodistus;
  export let submit;
  export let disabled = false;
  export let title = '';
</script>

<div class="w-full relative flex">
  <div class="w-5/6">
    {#if R.equals(version, '2018')}
      <ET2018Form {title} {energiatodistus} {submit} {disabled}/>
    {:else if R.equals(version, '2013')}
      <ET2013Form {title} {energiatodistus} {submit} {disabled}/>
    {:else}
      <p>Energiatodistusversiota {version} ei ole olemassa.</p>
    {/if}
  </div>
  <div class="sticky top-3em w-1/6 self-start flex justify-end">
    <ToolBar />
  </div>
</div>