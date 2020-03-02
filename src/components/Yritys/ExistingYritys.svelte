<script>
  import * as R from 'ramda';

  import { _ } from '../../i18n';

  import * as Maybe from '../../utils/maybe-utils';
  import * as Either from '../../utils/either-utils';
  import * as Future from '../../utils/future-utils';
  import * as Fetch from '../../utils/fetch-utils';

  import NavigationTabBar from '../NavigationTabBar/NavigationTabBar';
  import YritysForm from './YritysForm';
  import * as YritysUtils from './yritys-utils';

  export let params;
  let id = params.id;
  let yritys = Maybe.None();
  let api = Maybe.None();

  R.compose(
    Future.fork(
      console.error,
      fetchedYritys => (yritys = Maybe.Some(fetchedYritys))
    ),
    YritysUtils.getYritysByIdFuture(fetch)
  )(id);

  const submit = R.compose(
    Future.fork(console.error, console.log),
    YritysUtils.putYritysByIdFuture(fetch, id)
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

  .content > * :not(first) {
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
    <YritysForm
      {submit}
      existing={true}
      yritys={Maybe.getOrElse(null, yritys)} />
  {/if}
</section>
