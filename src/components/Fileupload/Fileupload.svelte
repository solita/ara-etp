<script>
import * as R from 'ramda';
import { fileupload } from './fileupload.js';

export let multiple = false;
export let labelText = 'Click or drop to choose a file for uploading';

let state = {
  highlight: false,
  files: []
};

const update = fn => state = fn(state);
</script>

<style type="text/postcss">
  label {
    @apply inline-block px-64 py-16 text-center bg-light text-dark rounded-lg border-dashed border-secondary border-2;
  }

  label:focus {
    @apply bg-secondary;
  }

  .highlight {
    @apply bg-primary;
  }
</style>

<label 
  use:fileupload={update}
  class:highlight={state.highlight}>
    <input 
    on:focus={() => update(R.assoc('highlight', true))} 
    on:blur={() => update(R.assoc('highlight', false))}
    class="opacity-0 w-0 h-0" type="file" bind:files={state.files} multiple={multiple} />
    {labelText}
</label>

<ul>
  {#each state.files as file}
    <li>{file.name}</li>
  {/each}
</ul>
