<script>
  import * as R from 'ramda';
  import { fileupload } from './fileupload-drag-and-drop';
  import { _ } from '@Language/i18n';

  export let multiple = false;
  export let labelText = $_('file-drop-or');
  export let labelTextHighlight = $_('file-browse');

  export let files;

  let highlight = false;
  const toggleHighlight = value => (highlight = value);
  const setFiles = f => (files = f);
</script>

<style type="text/postcss">
  label {
    @apply inline-block w-full py-40 text-center bg-light text-dark rounded-lg border-dashed border-secondary border-2 cursor-pointer;
  }

  input {
    @apply opacity-0 w-0 h-0;
  }

  label:hover .highlight-text,
  .highlight .highlight-text {
    @apply underline;
  }

  .highlight {
    @apply border-primary border-4;
  }
</style>

<label
  data-cy="droparea"
  use:fileupload={{ setFiles, toggleHighlight }}
  class:highlight>
  <input
    on:click={e => (e.target.value = '')}
    on:focus={_ => toggleHighlight(true)}
    on:blur={_ => toggleHighlight(false)}
    on:change={event => {
      setFiles(event.target.files);
      toggleHighlight(false);
    }}
    type="file"
    bind:files
    {multiple} />
  <span class="material-icons text-primary align-middle">arrow_downward</span>
  <span>{labelText}</span>
  <span class="font-bold text-primary highlight-text"
    >{labelTextHighlight}</span>
</label>
