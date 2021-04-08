<script>
  import H1 from '@Component/H/H1';
  let editMode = false;

  export let sivu;
  export let whoami;
</script>

<style>
</style>

<div class="w-full flex flex-col">
  {#if editMode}
    <input type="text" value={sivu.title} />
    <textarea value={sivu.body} />
  {:else}
    <div class="flex justify-between">
      <H1 text={sivu.title} />
      <button
        on:click={() => {
          editMode = true;
        }}>Edit</button>
    </div>
    <p>{sivu.body}</p>
  {/if}
</div>
