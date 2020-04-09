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
  }

  a:hover {
    @apply underline;
  }

  span {
    @apply mr-1 text-2xl;
  }
</style>

<a {href}>
  {@html R.compose( Maybe.orSome(''), R.map(i => `<span class="font-icon">${i}</span>`) )(icon)}
  {text}
</a>
