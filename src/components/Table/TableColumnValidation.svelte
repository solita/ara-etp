<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';

  export let value;
  export let validation = R.always(true);
  export let component;
  export let format = R.identity;

  $: error = R.compose(R.not, validation)(value);
  $: hasValue = R.allPass([R.complement(R.isNil), R.complement(R.isEmpty)])(
    value
  );
</script>

<style type="text/postcss">
  .error {
    @apply text-error font-bold;
  }
</style>

<div class:error>
  {#if component}
    <svelte:component this={component} {value} />
  {:else if hasValue}{format(value)}{:else}-{/if}
</div>
