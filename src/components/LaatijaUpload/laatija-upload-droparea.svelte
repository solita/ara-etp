<script>
  import * as R from 'ramda';
  import * as Future from '@Utility/future-utils';
  import FileDropArea from '@Component/FileDropArea/FileDropArea';
  import * as LaatijaUploadUtils from './laatija-upload-utils';

  export let laatijat;
  let files = [];

  $: R.compose(
    Future.fork(console.error, result => (laatijat = result)),
    R.map(LaatijaUploadUtils.readRows),
    R.sequence(Future.resolve),
    R.map(LaatijaUploadUtils.futurizeFileRead)
  )(files);
</script>

<FileDropArea bind:files />
