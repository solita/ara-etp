<script>
  import * as R from 'ramda';
  import FileDropArea from '../FileDropArea/FileDropArea.svelte';
  import Overlay from '../Overlay/Overlay.svelte';
  import Spinner from '../Spinner/Spinner.svelte';
  import Alert from '../Alert/Alert.svelte';
  import * as Either from '../../utils/either-utils';

  import * as FileuploadUtils from './fileupload-utils';

  import { _ } from '../../i18n.js';

  let state = { files: [] };

  export let update = fn => (state = fn(state));
  export let multiple = false;

  let overlay = false;

  let errorStateTransitions = [
    FileuploadUtils.filesError(FileuploadUtils.isValidAmountFiles(multiple), {
      component: Alert,
      type: 'error',
      text: $_('file_upload_error_multiple_files')
    })
  ];

  $: error = R.compose(
    Either.fold(R.identity, R.always({ component: false })),
    FileuploadUtils.validFilesInput(errorStateTransitions),
    R.prop('files')
  )(state);
</script>

<div class="flex flex-col">
  <Overlay {overlay}>
    <div slot="content">
      <FileDropArea {update} {state} {multiple} />
    </div>
    <div slot="overlay-content">
      <Spinner />
    </div>
  </Overlay>

  <svelte:component
    this={error.component}
    type={error.type}
    text={error.text}
    close={() => (error = { component: false })} />
</div>
