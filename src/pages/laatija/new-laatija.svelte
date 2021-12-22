<script>
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Future from '@Utility/future-utils';

  import * as GeoApi from '@Utility/api/geo-api';
  import * as KayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as LaatijaApi from '@Pages/laatija/laatija-api';
  import * as LaskutusApi from '@Utility/api/laskutus-api';

  import LaatijaForm from './laatija-form.svelte';

  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';

  let overlay = true;

  let resources = Maybe.None();
  let laatija = Maybe.Some({});

  const emptyLaatija = {
    login: Maybe.None(),
    passivoitu: false,
    rooli: Maybe.None(),
    etunimi: '',
    sukunimi: '',
    email: '',
    puhelin: '',
    henkilotunnus: Maybe.None(),
    toimintaalue: Maybe.None(),
    muuttoimintaalueet: [],
    wwwosoite: Maybe.None(),
    julkinenwwwosoite: false,
    maa: Either.Right('FI'),
    rooli: 0,
    patevyystaso: 0,
    verifytime: Maybe.None(),
    'vastaanottajan-tarkenne': Maybe.None(),
    jakeluosoite: '',
    postinumero: '',
    postitoimipaikka: '',
    laskutuskieli: 0,
    toteamispaivamaara: new Date(),
    'voimassaolo-paattymisaika': new Date(),
    'api-key': Maybe.None()
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
      errResponse => null,
      response => {
        overlay = false;
        push('/kayttaja/' + response.id);
      },
      LaatijaApi.postLaatija(laatija)
    );
  };
</script>

<Overlay {overlay}>
  <div slot="content">
    {#each resources.toArray() as { whoami, luokittelut }}
      <LaatijaForm
        {luokittelut}
        laatija={emptyLaatija}
        {whoami}
        submit={addLaatija} />
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
