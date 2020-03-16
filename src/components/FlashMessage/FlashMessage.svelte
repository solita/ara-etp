<script>
  import * as R from 'ramda';
  import { flashMessageStore } from '@/stores';

  import Alert from '@Component/Alert/Alert';

  export let module;

  const close = message => flashMessageStore.remove(message);

  $: messages = R.filter(
    R.compose(
      R.equals(module),
      R.prop('module')
    ),
    $flashMessageStore
  );
</script>

<style>

</style>

{#each messages as message}
  <Alert type={message.type} text={message.text} close={() => close(message)} />
{/each}
