<script>
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';
  import { lock, location } from 'svelte-spa-router';

  import Button from '@Component/Button/Button';

  export let dirty = false;
  export let routeRequest = false;

  let formLocation = $location;

  const removeQueryString = R.compose(
    R.nth(0),
    R.split('?'))

  const confirmation = event => {
    if (dirty &&
      !R.isNil(event.newURL) &&
      !R.endsWith(formLocation, removeQueryString(event.newURL))) {

      window.history.back();
      routeRequest = true;
    }
  }

  const confirmationUnload = event => {
    if (dirty) {
      event.returnValue = $_('navigation.confirmation.description')
        + ' ' + $_('navigation.confirmation.message');

      event.preventDefault();
    }
  }

  const cancel = _ => {
    routeRequest = false;
  }

  const forward = _ => {
    routeRequest = false;
    dirty = false;
    window.history.forward();
  }

  $: if (dirty) {
    lock.set(true);
  } else {
    formLocation = $location;
    lock.set(false);
  }
</script>

<style type="text/postcss">
    dialog {
        @apply fixed top-0 w-screen left-0 z-50 h-screen bg-hr cursor-default flex justify-center items-center;
    }

    .content {
        @apply relative bg-light w-2/3 py-10 px-10 rounded-md shadow-lg flex flex-col justify-center;
    }

    h1 {
        @apply text-secondary font-bold uppercase text-lg mb-4 pb-2 border-b-1 border-tertiary tracking-xl;
    }

    p {
        @apply mt-2;
    }

    .buttons {
        @apply flex flex-wrap items-center mt-5 border-t-1 border-tertiary;
    }
</style>

<svelte:window on:beforeunload={confirmationUnload} on:hashchange={confirmation}/>

{#if dirty && routeRequest}
  <dialog on:click|stopPropagation>
    <div class="content">
      <h1>{$_('navigation.confirmation.title')}</h1>

      <p>{$_('navigation.confirmation.description')}</p>
      <p>{$_('navigation.confirmation.message')}</p>
      <div class="buttons">
        <div class="mr-5 mt-5">
          <Button text={$_('navigation.confirmation.forward')} on:click={forward}/>
        </div>

        <div class="mt-5">
          <Button text={$_('navigation.confirmation.cancel')} style={'secondary'} on:click={cancel}/>
        </div>
      </div>
    </div>
  </dialog>
{/if}