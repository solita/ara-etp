<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';

  import Autocomplete from './Autocomplete';
  import Input from '@Component/Input/Input';

  export let createFutureFn;
  export let completedValue = '';

  export let reject = Maybe.None();

  let items = [];
  let rawValue = '';

  let cancel = () => {};
  let focused = false;

  const fetchItems = searchValue => {
    if (R.isEmpty(R.trim(searchValue))) {
      return;
    }

    cancel = R.compose(
      Future.fork(
        _ => {
          cancel = () => {};
          items = [];
        },
        fetchedItems => {
          cancel = () => {};
          items = R.compose(
            R.map(R.toString),
            R.reject(R.compose(Maybe.exists(R.__, reject), R.equals))
          )(fetchedItems);
        }
      ),
      R.chain(createFutureFn(fetch)),
      Future.after(300),
      R.tap(cancel),
      R.trim
    )(searchValue);
  };

  $: fetchItems(rawValue);
</script>

<style type="text/postcss">
  .inputwrapper {
    @apply flex relative items-center border-b-3 border-disabled text-dark;
  }

  .inputwrapper:hover {
    @apply border-hover;
  }

  .inputwrapper.search::after {
    @apply font-icon absolute text-2xl font-bold text-disabled;
    right: 0.5em;
    content: 'search';
  }

  .inputwrapper.focused {
    @apply border-primary;
  }

  .inputwrapper.focused::after {
    @apply text-primary;
  }

  input {
    @apply w-full relative font-medium py-1;
  }

  input:focus {
    @apply outline-none;
  }

  input:hover {
    @apply bg-background;
  }

  input:disabled {
    @apply bg-light;
  }
</style>

<Autocomplete bind:completedValue {items}>
  <div class="inputwrapper search" class:focused>
    <input
      on:blur={_ => (focused = false)}
      on:focus={_ => (focused = true)}
      bind:value={rawValue} />
  </div>
</Autocomplete>
