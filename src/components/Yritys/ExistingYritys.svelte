<script>
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Future from '@Utility/future-utils';
  import * as Fetch from '@Utility/fetch-utils';

  import NavigationTabBar from '@Component/NavigationTabBar/NavigationTabBar';
  import YritysForm from '@Component/Yritys/YritysForm';
  import * as YritysUtils from '@Component/Yritys/yritys-utils';
  import * as Breadcrumb from '@Component/Breadcrumb/breadcrumb-utils';

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

  $: Breadcrumb.set([[$_('yritys.yritykset'), '/#/yritykset'],
                     [Maybe.fold('...', R.prop('nimi'), yritys), window.location.href]]);

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
