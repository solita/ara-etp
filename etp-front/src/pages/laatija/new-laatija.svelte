<script>
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Router from '@Component/Router/router';
  import * as Locales from '@Language/locale-utils';
  import * as Response from '@Utility/response';
  import * as Laatija from './laatija';

  import * as GeoApi from '@Utility/api/geo-api';
  import * as KayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as LaatijaApi from '@Pages/laatija/laatija-api';
  import * as LaskutusApi from '@Utility/api/laskutus-api';
  import * as versionApi from '@Component/Version/version-api';

  import LaatijaForm from './laatija-form.svelte';

  import H1 from '@Component/H/H1';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';
  import { announcementsForModule } from '@Utility/announce';
  import { _ } from '@Language/i18n';

  const i18n = $_;
  const i18nRoot = 'laatija';
  const { announceError, announceSuccess } = announcementsForModule('Laatija');

  let overlay = true;
  let dirty = false;

  let resources = Maybe.None();

  let laatija = Laatija.emptyLaatija();
  const clean = _ => {
    laatija = Laatija.emptyLaatija();
  };

  $: Future.fork(
    response => {
      console.log('ERROR:' + response);
    },
    response => {
      overlay = false;
      resources = Maybe.Some(response);
    },
    Future.parallelObject(2, {
      whoami: KayttajaApi.whoami,
      config: versionApi.getConfig,
      luokittelut: Future.parallelObject(5, {
        countries: GeoApi.countries,
        toimintaalueet: GeoApi.toimintaalueet,
        patevyydet: LaatijaApi.patevyydet,
        laskutuskielet: LaskutusApi.laskutuskielet
      })
    })
  );

  const addLaatija = laatija => {
    overlay = true;
    Future.fork(
      response => {
        overlay = false;
        announceError(
          Locales.uniqueViolationMessage(
            i18n,
            response,
            Response.errorKey(i18nRoot, 'add', response)
          )
        );
      },
      response => {
        announceSuccess(i18n(`${i18nRoot}.messages.add-success`));
        overlay = false;
        dirty = false;
        Router.push('/kayttaja/' + response.id);
      },
      LaatijaApi.postLaatija(Laatija.fromLaatijaForm(laatija))
    );
  };
</script>

<H1 text={i18n('kayttajat.new-partner')} />
<Overlay {overlay}>
  <div slot="content">
    {#each resources.toArray() as { whoami, config, luokittelut }}
      <LaatijaForm
        {luokittelut}
        {config}
        {laatija}
        {whoami}
        {dirty}
        errorModule={'Laatija'}
        submit={addLaatija}
        cancel={clean} />
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
