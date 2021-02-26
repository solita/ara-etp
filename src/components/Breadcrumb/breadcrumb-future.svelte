<script>
  import * as R from 'ramda';
  import * as Future from '@Utility/future-utils';
  import { _, locale } from '@Language/i18n';
  import Link from '@Component/Link/Link';
  import * as Maybe from '@Utility/maybe-utils';

  import * as BreadcrumbFutureUtils from './breadcrumb-future-utils';

  export let location;

  let crumbCache = {};

  let breadcrumb = [];

  let cancel = () => {};

  $: {
    const lens = R.lensPath([$locale, location]);

    cancel = Future.fork(
      console.error,
      result => (breadcrumb = result),
      R.compose(
        R.view(lens),
        R.tap(updatedCache => (crumbCache = updatedCache)),
        R.unless(
          R.view(lens),
          R.set(
            lens,
            Future.cache(BreadcrumbFutureUtils.parseLocation($_, location))
          )
        ),
        R.tap(cancel)
      )(crumbCache)
    );
  }
</script>

<style>
</style>

<svelte:head>
  <title
    >{`${$_('document.title')} | ${R.join(
      ' / ',
      R.pluck('label', breadcrumb)
    )}`}</title>
</svelte:head>

<div class="px-10 flex h-16 items-center text-primary">
  {#if R.head(breadcrumb)}
    <Link
      href={R.head(breadcrumb).url}
      text={R.head(breadcrumb).label}
      icon={Maybe.Some('home')} />
  {:else}
    <Link href="/" icon={Maybe.Some('home')} />
  {/if}

  {#each R.tail(breadcrumb) as { label, url }}
    <span class="ml-1 mr-2">/</span>
    <Link href={url} text={label} />
  {/each}
</div>
