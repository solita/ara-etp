<script>
  import { replace } from '@Component/Router/router';
  import { querystring } from 'svelte-spa-router';
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import qs from 'qs';

  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';
  import EnergiatodistusForm from './EnergiatodistusForm';
  import * as empty from './empty';
  import * as ET from './energiatodistus-utils';
  import * as api from './energiatodistus-api';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';

  import { flashMessageStore } from '@/stores';
  import * as Response from '@Utility/response';

  export let params;

  let overlay = false;

  const toggleOverlay = value => {
    overlay = value;
  };

  const emptyEnergiatodistus = versio =>
    R.equals(versio, '2018')
      ? empty.energiatodistus2018()
      : empty.energiatodistus2013();

  const cleanEnergiatodistusCopy = R.compose(
    R.assoc('korvattu-energiatodistus-id', Maybe.None()),
    R.assoc('laskutettava-yritys-id', Maybe.None()),
    R.assoc('laskuriviviite', Maybe.None()),
    R.assoc('laatija-id', Maybe.None()),
    R.dissoc('laatija-fullname'),
    R.assoc('kommentti', Maybe.None()),
    R.assoc('tila-id', ET.tila.draft),
    R.dissoc('id')
  );

  $: copyFromId = R.compose(
    Maybe.fromNull,
    R.prop('copy-from-id'),
    qs.parse
  )($querystring);

  let resources = Maybe.None();

  const submit = (energiatodistus, onSuccessfulSave) =>
    R.compose(
      Future.fork(
        () => {
          toggleOverlay(false);
          flashMessageStore.add(
            'Energiatodistus',
            'error',
            $_('energiatodistus.messages.save-error')
          );
        },
        ({ id }) => {
          toggleOverlay(false);
          flashMessageStore.addPersist(
            'Energiatodistus',
            'success',
            $_('energiatodistus.messages.save-success')
          );
          onSuccessfulSave();
          replace(`/energiatodistus/${params.version}/${id}`);
        }
      ),
      Future.delay(500),
      api.postEnergiatodistus(fetch, params.version),
      R.tap(() => toggleOverlay(true))
    )(energiatodistus);

  $: title =
    $_('energiatodistus.title') +
    ' ' +
    params.version +
    ' - ' +
    Maybe.fold(
      $_('energiatodistus.new.draft'),
      id => $_('energiatodistus.new.copy-from') + ' ' + id,
      copyFromId
    );

  // Load all page resources
  $: R.compose(
    Future.fork(
      response => {
        toggleOverlay(false);
        const msg = $_(
          Maybe.orSome(
            'energiatodistus.messages.load-error',
            Response.localizationKey(response)
          )
        );

        flashMessageStore.add('Energiatodistus', 'error', msg);
      },
      response => {
        resources = Maybe.Some({
          energiatodistus: R.compose(
            Maybe.orSome(emptyEnergiatodistus(params.version)),
            R.map(cleanEnergiatodistusCopy),
            Maybe.fromNull
          )(response[0]),
          whoami: response[1],
          luokittelut: response[2],
          validation: response[3]
        });
        toggleOverlay(false);
      }
    ),
    Future.parallel(5),
    R.prepend(
      Maybe.fold(
        Future.resolve(null),
        api.getEnergiatodistusById(params.version),
        copyFromId
      )
    ),
    R.prepend(kayttajaApi.whoami),
    R.juxt([api.luokittelutForVersion, api.validation])
  )(params.version);
</script>

<Overlay {overlay}>
  <div slot="content">
    {#each resources.toArray() as { energiatodistus, luokittelut, validation, whoami }}
      <EnergiatodistusForm
        version={params.version}
        {title}
        {energiatodistus}
        {whoami}
        {luokittelut}
        {validation}
        {submit} />
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
