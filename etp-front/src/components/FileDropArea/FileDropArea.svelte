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
    @apply inline-block w-full cursor-pointer rounded-lg border-2 border-dashed border-secondary bg-light py-40 text-center text-dark;
  }

  input {
    @apply h-0 w-0 opacity-0;
  }

  label:hover .highlight-text,
  .highlight .highlight-text {
    @apply underline;
  }

  .highlight {
    @apply border-4 border-primary;
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
    {multiple} />
  <span class="material-icons align-middle text-primary">arrow_downward</span>
  <span>{labelText}</span>
  <span class="highlight-text font-bold text-primary"
    >{labelTextHighlight}</span>
</label>
