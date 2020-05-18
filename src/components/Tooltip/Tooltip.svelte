<script>
  import * as R from 'ramda';

  export let style = 'primary';

  export let vertical = 'top';
  export let horizontal = 'left';

  export let text = '';

  $: anchorClass = R.compose(
    R.unless(
      R.includes(R.__, ['topright', 'topleft', 'bottomright', 'bottomleft']),
      R.always('none')
    ),
    R.concat
  )(vertical, horizontal);
</script>

<style type="text/postcss">
  .container {
    @apply text-light font-medium rounded-lg max-w-md px-4 py-2 my-3 mx-2 text-center absolute;
  }

  .anchor {
    @apply rounded-none absolute h-4 w-4;
    z-index: -1;
    transform: rotate(45deg);
  }

  .anchor__topleft {
    top: -0.2rem;
    left: 1rem;
  }

  .anchor__topright {
    top: -0.2rem;
    right: 1rem;
  }

  .anchor__bottomleft {
    bottom: -0.2rem;
    left: 1rem;
  }

  .anchor__bottomright {
    bottom: -0.2rem;
    right: 1rem;
  }

  .anchor__none {
    @apply hidden;
  }

  .primary {
    @apply bg-primary;
  }

  .secondary {
    @apply bg-secondary;
  }
</style>

<div class={`container ${style}`}>
  {text}
  <div class={`anchor anchor__${anchorClass} ${style}`} />
</div>
