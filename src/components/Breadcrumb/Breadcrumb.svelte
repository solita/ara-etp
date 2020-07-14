<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as BreadcrumbUtils from './breadcrumb-utils';

  import Link from '@Component/Link/Link';

  export let user;
  export let location;
  export let i18n;
  export let idTranslate;

  $: breadcrumbs = BreadcrumbUtils.breadcrumbParse(
    idTranslate,
    location,
    i18n,
    user
  );
</script>

<style type="text/postcss">
  div {
    @apply px-20 flex h-16 items-center text-primary;
  }
  span {
    @apply ml-1 mr-2;
  }
</style>

<div class="xl:w-xl lg:w-lg md:w-md sm:w-sm">
  <Link href="/" icon={Maybe.Some('home')} />
  {#each breadcrumbs as { label, url }}
    <span>/</span>
    <Link href={url} text={label} />
  {/each}
</div>
