<script>
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';
  import { lock } from 'svelte-spa-router';

  import Button from '@Component/Button/Button';

  export let dirty = false;
  export let routeRequest = false;

  const confirmation = event => {
    if (dirty && !R.isNil(event.newURL) && !R.isNil(event.oldURL)) {
      routeRequest = true;

      // revert hash change to an old url
      // Note: push state does not cause a new hashchange event
      window.history.pushState(undefined, undefined, event.oldURL);

      // update location in router
      window.dispatchEvent(new Event('hashchange'));
    }
  };

  const confirmationUnload = event => {
    if (dirty) {
      event.returnValue =
        $_('navigation.confirmation.description') +
        ' ' +
        $_('navigation.confirmation.message');

      event.preventDefault();
    }
  };

  const cancel = _ => {
    routeRequest = false;
  };

  const forward = _ => {
    routeRequest = false;
    dirty = false;
    window.history.back();
  };

  $: if (dirty) {
    lock.set(true);
  } else {
    lock.set(false);
  }
</script>

<style type="text/postcss">
  dialog {
    @apply fixed left-0 top-0 z-50 flex h-screen w-screen cursor-default items-center justify-center bg-hr;
  }

  .content {
    @apply relative flex w-2/3 flex-col justify-center rounded-md bg-light px-10 py-10 shadow-lg;
  }

  h1 {
    @apply mb-4 border-b-1 border-tertiary pb-2 text-lg font-bold uppercase tracking-xl text-secondary;
  }

  p {
    @apply mt-2;
  }

  .buttons {
    @apply mt-5 flex flex-wrap items-center border-t-1 border-tertiary;
  }
</style>

<svelte:window
  on:beforeunload={confirmationUnload}
  on:hashchange={confirmation} />

{#if dirty && routeRequest}
  <dialog on:click|stopPropagation>
    <div class="content">
      <h1>{$_('navigation.confirmation.title')}</h1>

      <p>{$_('navigation.confirmation.description')}</p>
      <p>{$_('navigation.confirmation.message')}</p>
      <div class="buttons">
        <div class="mr-5 mt-5">
          <Button
            text={$_('navigation.confirmation.forward')}
            on:click={forward} />
        </div>

        <div class="mt-5">
          <Button
            text={$_('navigation.confirmation.cancel')}
            style={'secondary'}
            on:click={cancel} />
        </div>
      </div>
    </div>
  </dialog>
{/if}
