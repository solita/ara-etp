<script context="module">
  import router from 'page';
  import { writable } from 'svelte/store';

  export const activePath = writable(location.pathname);

  export const navigate = path => router(path);

  export const backReferred = defaultPath =>
    document.referrer.split('/')[2] === window.location.host
      ? window.history.back()
      : navigate(defaultPath);
</script>

<script>
  import { parse } from 'qs';

  import Home from '@Page/home';
  import EnergiatodistusHaku from '@Page/ethaku';
  import Energiatodistus from '@Page/energiatodistus';
  import LaatijaHaku from '@Page/laatijahaku';
  import Laatija from '@Page/laatija';
  import Saavutettavuusseloste from '@Page/saavutettavuusseloste';
  import TietoaSivustosta from '@Page/tietoa-sivustosta';
  import LaatijanKirjautuminen from '@Page/laatijankirjautuminen';
  import Rekisteroitymisohjeet from '@Page/rekisteroitymisohjeet';
  import AineistopalveluLiittyminen from '@Page/aineistopalvelu-liittyminen';
  import Tilastot from '@Page/tilastot';
  import NotFound from '@Page/not-found';

  let page;
  let params;

  router('*', ({ pathname, querystring }, next) => {
    activePath.set(pathname);
    params = parse(querystring);
    next();
  });
  router('/ethaku', () => {
    page = EnergiatodistusHaku;
  });
  router('/energiatodistus', () => {
    page = Energiatodistus;
  });
  router('/laatijahaku', () => {
    page = LaatijaHaku;
  });
  router('/laatija', () => {
    page = Laatija;
  });
  router('/tietoa-sivustosta', () => {
    page = TietoaSivustosta;
  });
  router('/saavutettavuusseloste', () => {
    page = Saavutettavuusseloste;
  });
  router('/laatijankirjautuminen', () => {
    page = LaatijanKirjautuminen;
  });
  router('/rekisteroitymisohjeet', () => {
    page = Rekisteroitymisohjeet;
  });
  router('/aineistopalvelu-liittyminen', () => {
    page = AineistopalveluLiittyminen;
  });
  router('/tilastot', () => {
    page = Tilastot;
  });
  router('/', () => (page = Home));

  router('*', () => (page = NotFound));

  router.start();
</script>

<svelte:component this={page} {...params} />
