<script>
  import { replace } from '@Component/Router/router';
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import * as Future from '@Utility/future-utils';

  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';
  import YritysForm from '@Component/Yritys/YritysForm';
  import { breadcrumbStore } from '@/stores';
  import * as YritysUtils from './yritys-utils';

  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore } from '@/stores';

  let overlay = false;

  const toggleOverlay = value => () => (overlay = value);

  let yritys = YritysUtils.emptyYritys();

  const submit = R.compose(
    Future.fork(
      R.compose(
        R.tap(toggleOverlay(false)),
        flashMessageStore.add('Yritys', 'error'),
        R.ifElse(R.equals(409),
            _ => $_('yritys.messages.save-conflict'),
            _ => $_('yritys.messages.save-error'))),
      R.compose(
        R.tap(_ =>
          flashMessageStore.addPersist(
            'Yritys',
            'success',
            $_('yritys.messages.save-success')
          )),
        ({ id }) => replace(`/yritys/${id}`))),
    YritysUtils.postYritysFuture(fetch),
    R.tap(toggleOverlay(true)));

  breadcrumbStore.set([
    {
      label: $_('yritys.yritykset'),
      url: '/#/yritykset'
    },
    {
      label: $_('yritys.uusi_yritys'),
      url: window.location.href
    }
  ]);
</script>

<Overlay {overlay}>
  <div slot="content">
    <YritysForm {yritys} {submit} />
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
