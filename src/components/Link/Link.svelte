<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';

  export let href;
  export let text = '';
  export let icon = Maybe.None();
</script>

<style type="text/postcss">
  div {
    @apply flex justify-start items-center;
  }

  a {
    @apply text-link border-b-1 border-transparent;
  }

  a:hover {
    @apply cursor-pointer border-primary;
  }
</style>

<!-- purgecss: font-icon mr-1 text-2xl -->
<a {href}>
  {@html R.compose( Maybe.orSome(''), R.map(i => `<span class="font-icon mr-1 text-2xl">${i}</span>`) )(icon)}
  {text}
</a>
