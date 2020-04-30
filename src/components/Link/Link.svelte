<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';

  export let href;
  export let text = '';
  export let icon = Maybe.None();
</script>

<style type="text/postcss">
  a {
    @apply text-link flex;
    align-items: center;
  }

  a:hover {
    @apply underline;
  }
</style>

<a {href}>
  {@html R.compose( Maybe.orSome(''), R.map(i => `<span class="font-icon mr-1 text-2xl">${i}</span>`) )(icon)}
  {text}
</a>
