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
  import { breadcrumbStore } from '@/stores';

  let overlay = false;

  let yritys = YritysUtils.emptyYritys();

  const submit = R.compose(
    Future.fork(
      R.compose(
        console.error,
        R.tap(() => (overlay = false))
      ),
      R.compose(
        ({ id }) => replace(`/yritys/${id}`),
        R.tap(() => (overlay = false))
      )
    ),
    Future.both(Future.after(500, true)),
    YritysUtils.postYritysFuture(fetch),
    R.tap(() => (overlay = true))
  );

  $: links = [
    {
      text: $_('yritys.uusi_yritys')
    },
    { text: $_('yritys.laatijat') }
  ];

  breadcrumbStore.set([{
    label: $_('yritys.yritykset'),
    url: '/#/yritykset'
  },{
    label: $_('yritys.uusi_yritys'),
    url: window.location.href
  }]);

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
