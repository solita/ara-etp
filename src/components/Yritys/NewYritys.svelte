<script>
  import { replace } from 'svelte-spa-router';
  import * as R from 'ramda';

  import { _ } from '../../i18n';

  import * as Yritys from './yritys';

  import * as Maybe from '../../utils/maybe-utils';
  import * as Either from '../../utils/either-utils';
  import * as Fetch from '../../utils/fetch-utils';
  import * as Future from '../../utils/future-utils';

  import NavigationTabBar from '../NavigationTabBar/NavigationTabBar.svelte';
  import YritysForm from './YritysForm.svelte';
  import * as YritysUtils from './yritys-utils';

  let { yritys, api, method } = YritysUtils.newYritysAction();

  const submit = R.compose(
    Future.fork(
      console.error,
      R.compose(
        replace,
        R.concat('/yritys/'),
        R.toString,
        R.prop('id')
      )
    ),
    Fetch.fetchFromUrl(R.__, api),
    Fetch.putFetch(fetch, method),
    YritysUtils.yritysSerialize
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
