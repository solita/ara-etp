<script>
  import { replace } from '@Component/Router/router';
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';

  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';
  import YritysForm from '@Pages/yritys/yritys-form';
  import * as YritysUtils from '@Pages/yritys/yritys-utils';

  import * as api from '@Pages/yritys/yritys-api';
  import * as Locales from '@Language/locale-utils';
  import * as kayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import { announcementsForModule } from '@Utility/announce';

  let overlay = false;
  let dirty = false;

  let yritys = YritysUtils.emptyYritys;
  let resources = Maybe.None();

  const clean = _ => {
    yritys = YritysUtils.emptyYritys;
  };

  const { announceError, announceSuccess } = announcementsForModule('yritys');

  const submit = R.compose(
    Future.fork(
      response => {
        overlay = false;
        announceError(
          Locales.uniqueViolationMessage(
            $_,
            response,
            'yritys.messages.save-error'
          )
        );
      },
      ({ id }) => {
        announceSuccess($_('yritys.messages.save-success'));
        dirty = false;
        replace(`/yritys/${id}`);
      }
    ),
    api.postYritys(fetch),
    R.tap(() => {
      overlay = true;
    })
  );

  // Load classifications
  Future.fork(
    () => {
      overlay = false;
      announceError($_('yritys.messages.load-error'));
    },
    response => {
      resources = Maybe.Some(response);
      overlay = false;
    },
    Future.parallelObject(4, {
      luokittelut: api.luokittelut,
      whoami: kayttajaApi.whoami
    })
  );
</script>

<Overlay {overlay}>
  <div slot="content">
    {#each Maybe.toArray(resources) as { luokittelut, whoami }}
      <YritysForm
        bind:yritys
        bind:dirty
        {luokittelut}
        {whoami}
        {submit}
        cancel={clean} />
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
