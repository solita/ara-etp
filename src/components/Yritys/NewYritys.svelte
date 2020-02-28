<script>
  import { replace } from 'svelte-spa-router';
  import * as R from 'ramda';

  import { _ } from '../../i18n';

  import * as Maybe from '../../utils/maybe-utils';
  import * as Either from '../../utils/either-utils';
  import * as Fetch from '../../utils/fetch-utils';
  import * as Future from '../../utils/future-utils';

  import NavigationTabBar from '../NavigationTabBar/NavigationTabBar.svelte';
  import YritysForm from './YritysForm.svelte';
  import * as YritysUtils from './yritys-utils';

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
