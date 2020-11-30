<script>
  import { fly } from 'svelte/transition';

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

<style type="text/postcss">
  div {
    @apply sticky mt-2;
    bottom: 4em;
  }
</style>

{#if !R.isEmpty(messages)}
  <div class="w-full min-h-3em">
    {#each messages as message}
      <div transition:fly|local={{ y: 50 }}>
        <Alert
          type={message.type}
          text={message.text}
          close={() => close(message)} />
      </div>
    {/each}
  </div>
{/if}
