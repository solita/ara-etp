<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import {
    flashMessageStore,
    breadcrumbStore,
    currentUserStore
  } from '@/stores';
  import * as LaatijaUtils from './laatija-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';

  import * as KayttajaUtils from '@Component/Kayttaja/kayttaja-utils';

  import LaatijaForm from './LaatijaForm';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';

  export let params;

  let laatija = Maybe.None();
  let overlay = true;
  let disabled = false;

  const toggleOverlay = value => () => (overlay = value);
  const toggleDisabled = value => () => (disabled = value);

  const resetView = () => {
    overlay = true;
    yritys = Maybe.None();
    disabled = false;
  };

  $: submit = R.compose(
    Future.forkBothDiscardFirst(
      R.compose(
        R.tap(toggleOverlay(false)),
        flashMessageStore.add('Laatija', 'error'),
        R.always($_('laatija.messages.save-error'))
      ),
      R.compose(
        R.tap(toggleOverlay(false)),
        flashMessageStore.add('Laatija', 'success'),
        R.always($_('laatija.messages.save-success'))
      )
    ),
    Future.delay(500),
    LaatijaUtils.putLaatijaByIdFuture(
      R.compose(
        Maybe.orSome(0),
        R.map(R.prop('rooli'))
      )($currentUserStore),
      fetch,
      params.id
    ),
    R.tap(toggleOverlay(true))
  );

  $: R.compose(
    Future.fork(
      R.compose(
        flashMessageStore.add('Laatija', 'error'),
        R.always($_('laatija.messages.load-error')),
        R.tap(toggleOverlay(false)),
        R.tap(toggleDisabled(false))
      ),
      R.compose(
        fetchedLaatija => (laatija = Maybe.Some(fetchedLaatija)),
        R.tap(toggleOverlay(false))
      )
    ),
    Future.delay(500),
    LaatijaUtils.getLaatijaByIdFuture(fetch),
    R.prop('id')
  )(params);

  $: breadcrumbStore.set([
    {
      label: $_('laatija.omattiedot'),
      url: location.href
    }
  ]);
</script>

<Overlay {overlay}>
  <div slot="content">
    {#if Maybe.isSome(laatija)}
      <LaatijaForm
        {submit}
        {disabled}
        existing={true}
        laatija={Maybe.getOrElse(LaatijaUtils.emptyLaatija(), laatija)} />
    {/if}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
