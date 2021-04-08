<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Kayttajat from '@Utility/kayttajat';
  import { _ } from '@Language/i18n';
  import TextButton from '@Component/Button/TextButton';
  import Link from '@Component/Link/Link';

  export let sivut = [];
  export let activeSivuId;
  export let whoami;
</script>

<style>
  .root {
    @apply bg-secondary text-light border-light;
  }
  .child {
    @apply bg-light text-dark border-secondary;
  }
  .active .title {
    @apply font-bold underline;
  }
</style>

<nav class="w-full flex flex-col max-w-xs">
  {#each sivut as sivu}
    <div
      class="px-1 py-2 border-b"
      class:active={sivu.id == activeSivuId}
      class:root={sivu['parent-id'].isNone()}
      class:child={sivu['parent-id'].isSome()}>
      <span class="material-icons">add</span>
      <a href={`/#/ohje/${sivu.id}`}>
        <span class="title"> {sivu.title} </span>
      </a>
    </div>
  {/each}
  {#if Kayttajat.isPaakayttaja(whoami)}
    <div class="flex flex-col space-y-2">
      <TextButton
        on:click={() => {
          alert('Order Links Button');
        }}
        icon="low_priority"
        text={'Order Links'} />

      <Link
        href="/#/ohje/new"
        icon={Maybe.Some('add_circle_outline')}
        text={'new sivu'} />
    </div>{/if}
</nav>
