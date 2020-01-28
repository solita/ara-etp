<script>
  import FileDropArea from '../FileDropArea/FileDropArea.svelte';
  import Overlay from '../Overlay/Overlay.svelte';
  import Spinner from '../Spinner/Spinner.svelte';

  import { _ } from '../../i18n.js';

  let state = { files: [] };

  export let update = fn => (state = fn(state));
  export let multiple = false;

  let overlay = false;
</script>

<Overlay {overlay}>
  <div slot="content">
    <FileDropArea {update} {state} {multiple} />
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
