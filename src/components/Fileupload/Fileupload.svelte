<script>
  import * as R from 'ramda';
  import FileDropArea from '../FileDropArea/FileDropArea';
  import Overlay from '../Overlay/Overlay';
  import Spinner from '../Spinner/Spinner';
  import Alert from '../Alert/Alert';
  import * as Either from '../../utils/either-utils';

  import * as FileuploadUtils from './fileupload-utils';

  import { _ } from '../../language/i18n.js';

  let state = { files: [] };

  export let update = fn => (state = fn(state));
  export let multiple = false;

  let overlay = false;

  let errorStateTransitions = [
    FileuploadUtils.filesError(FileuploadUtils.isValidAmountFiles(multiple), {
      component: Alert,
      type: 'error',
      text: 'file_upload_error_multiple_files'
    })
  ];

  $: error = R.compose(
    Either.fold(R.identity, R.always({ component: false })),
    FileuploadUtils.validFilesInput(errorStateTransitions),
    R.prop('files')
  )(state);
</script>

<style type="text/postcss">
  .fileupload {
    @apply flex flex-col;
  }
</style>

<div class="fileupload">
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
