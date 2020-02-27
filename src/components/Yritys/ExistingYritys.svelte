<script>
  import * as R from 'ramda';

  import { _ } from '../../i18n';

  import * as Yritys from './yritys';

  import * as Maybe from '../../utils/maybe-utils';
  import * as Either from '../../utils/either-utils';
  import * as Future from '../../utils/future-utils';
  import * as Fetch from '../../utils/fetch-utils';

  import NavigationTabBar from '../NavigationTabBar/NavigationTabBar.svelte';
  import YritysForm from './YritysForm.svelte';
  import * as YritysUtils from './yritys-utils';

  export let params;
  let id = Maybe.fromNull(params.id);
  let yritys = Maybe.None();
  let api = Maybe.None();

  R.compose(
    Future.fork(
      console.error,
      fetchedYritys => (yritys = Maybe.Some(fetchedYritys))
    ),
    Maybe.getOrElse(Future.reject(404)),
    R.lift(YritysUtils.getYritysByIdFuture)
  )(id);

  const submit = R.compose(
    Future.fork(console.error, console.log),
    Maybe.getOrElse(Future.reject(404)),
    R.lift(YritysUtils.putYritysByIdFuture)(id),
    Maybe.Some
  );

  $: links = [
    {
      text: Maybe.fold('...', R.prop('nimi'), yritys)
    },
    { text: $_('yritys.laatijat') }
  ];
</script>

<style type="text/postcss">
  .content {
    @apply flex flex-col -my-4 pb-8;
  }

  .content > * {
    @apply py-8;
  }
</style>

<section class="content">
  {#if yritys.isNone()}
    Loading...
  {:else}
    <div class="w-full">
      <NavigationTabBar {links} />
    </div>
    <YritysForm {submit} yritys={Maybe.getOrElse(null, yritys)} />
  {/if}
</section>
