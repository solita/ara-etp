<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import Link from '@Component/Link/Link';
  import * as Maybe from '@Utility/maybe-utils';

  import * as BreadcrumbUtils from './breadcrumb-utils';

  export let location;
  export let whoami;
  export let idTranslate;

  let breadcrumb = [];

  $: console.log(idTranslate);
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
