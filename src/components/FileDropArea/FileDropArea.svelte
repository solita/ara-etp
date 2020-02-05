<script>
  import * as R from 'ramda';
  import { fileupload } from './fileupload-drag-and-drop';

  export let multiple = false;
  export let labelText = 'Click or drop to choose a file for uploading';
  
  export let state = {
    files: []
  };

  export let uiState = {
    highlight: false
  }

  export let update = fn => state = fn(state);
  const updateUi = fn => uiState = fn(uiState);
</script>

<style type="text/postcss">
  label {
    @apply inline-block px-64 py-40 text-center bg-light text-dark rounded-lg border-dashed border-secondary border-2;
  }

  input {
    @apply opacity-0 w-0 h-0;
  }

  .highlight {
    @apply bg-primary;
  }
</style>

<label 
  use:fileupload={{update, updateUi}}
  class:highlight={uiState.highlight}>
    <input 
    on:focus={_ => updateUi(R.assoc('highlight', true))} 
    on:blur={_ => updateUi(R.assoc('highlight', false))}
    on:change={
      event => {
        update(R.assoc('files', event.target.files)); 
        updateUi(R.assoc('highlight', false));
      }}
    type="file" files={state.files} multiple={multiple} />
    {labelText}
</label>
