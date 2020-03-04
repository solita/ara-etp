<script>
  import { replace } from 'svelte-spa-router';
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Fetch from '@Utility/fetch-utils';
  import * as Future from '@Utility/future-utils';

  import NavigationTabBar from '@Component/NavigationTabBar/NavigationTabBar';
  import YritysForm from '@Component/Yritys/YritysForm';
  import * as YritysUtils from '@Component/Yritys/yritys-utils';

  let yritys = YritysUtils.emptyYritys();

  const submit = R.compose(
    Future.fork(console.error, ({ id }) => replace(`/yritys/${id}`)),
    YritysUtils.postYritysFuture(fetch)
  );

  $: links = [
    {
      text: $_('yritys.uusi_yritys')
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
  <div class="w-full">
    <NavigationTabBar {links} />
  </div>
  <YritysForm {yritys} {submit} />
</section>
