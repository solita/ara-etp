<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Resources from '@Utility/resources';
  import * as Future from '@Utility/future-utils';
  import * as BreadcrumbUtils from './breadcrumb-utils';

  import Link from '@Component/Link/Link';

  export let user;
  export let location;
  export let i18n;

  let resource = Maybe.None();

  $: R.compose(
    Future.value(r => (resource = r)),
    Future.coalesce(Maybe.None, Maybe.Some),
    Maybe.orSome(Future.reject()),
    Resources.parseResource(fetch)
  )(location);

  $: breadcrumbs = R.compose(
    R.flatten,
    Array.of,
    BreadcrumbUtils.breadcrumbParse
  )(location, i18n, user, resource);
</script>

<style type="text/postcss">
  div {
    @apply bg-background px-20 flex h-16 items-center text-primary;
  }
  span {
    @apply ml-1 mr-2;
  }
</style>

<div>
  <Link href="/" icon={Maybe.Some('home')} />
  {#each breadcrumbs as { label, url }}
    <span>/</span>
    <Link href={url} text={label} />
  {/each}
</div>
