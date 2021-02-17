<script>
  import * as R from 'ramda';
  import * as Future from '@Utility/future-utils';
  import FileDropArea from '@Component/FileDropArea/FileDropArea';
  import * as LaatijaUploadUtils from './laatija-upload-utils';
  import { _ } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';

  export let laatijat;

  let files = [];

  $: R.compose(
    Future.fork(
      _ =>
        flashMessageStore.add('Laatija', 'error', $_('errors.file-read-error')),
      result => (laatijat = result)
    ),
    R.map(LaatijaUploadUtils.readRows),
    R.sequence(Future.resolve),
    R.map(LaatijaUploadUtils.futurizeFileRead)
  )(files);
</script>

<FileDropArea bind:files />
