<script>
  import * as R from 'ramda';
  import * as Future from '@Utility/future-utils';
  import FileDropArea from '@Component/FileDropArea/FileDropArea';
  import * as LaatijaUploadUtils from './laatija-upload-utils';
  import { _ } from '@Language/i18n';
  import { announcementsForModule } from '@Utility/announce';

  export let laatijat;
  export let files = [];

  const i18n = $_;
  const { announceError } = announcementsForModule('Laatija');

  $: R.compose(
    Future.fork(
      _ => announceError(i18n('errors.file-read-error')),
      result => (laatijat = result)
    ),
    R.map(LaatijaUploadUtils.deserialize),
    R.sequence(Future.resolve),
    R.map(LaatijaUploadUtils.futurizeFileRead)
  )(files);
</script>

<FileDropArea bind:files />
