<script>
  import * as R from 'ramda';

  import { _ } from '../../i18n';

  import * as Yritys from './yritys';

  import * as Maybe from '../../utils/maybe-utils';
  import * as Either from '../../utils/either-utils';

  import NavigationTabBar from '../NavigationTabBar/NavigationTabBar.svelte';
  import YritysForm from './YritysForm.svelte';
  import * as YritysUtils from './yritys-utils';

  let { yritys, api, method } = YritysUtils.newYritysAction();

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

  .content > * {
    @apply py-8;
  }
</style>

<section class="content">
  <div class="w-full">
    <NavigationTabBar {links} />
  </div>
  <YritysForm
    on:submit={event => {
      console.log(event);
      event.preventDefault();
    }}
    {yritys} />
</section>
