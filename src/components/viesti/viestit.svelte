<script>
  import * as R from 'ramda';
  import * as Parsers from '@Utility/parsers';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import { querystring } from 'svelte-spa-router';
  import { push } from '@Component/Router/router';
  import qs from 'qs';

  import * as api from './viesti-api';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';

  import { flashMessageStore } from '@/stores';
  import { _ } from '@Language/i18n';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import H1 from '@Component/H/H1.svelte';
  import Link from '../Link/Link.svelte';
  import Viestiketju from './viestiketju';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import Pagination from '@Component/Pagination/Pagination';

  let resources = Maybe.None();
  let overlay = true;

  const pageSize = 50;
  let ketjutCount = 0;
  let page = Maybe.Some(1);

  $: page = R.compose(
    R.chain(Either.toMaybe),
    R.map(Parsers.parseInteger),
    Maybe.fromEmpty,
    R.prop('page'),
    qs.parse
  )($querystring);
  $: pageCount = Math.ceil(R.divide(ketjutCount, pageSize));

  const nextPageCallback = nextPage => push(`#/viesti/all?page=${nextPage}`);

  const load = page => {
    overlay = true;
    Future.fork(
      response => {
        const msg = $_(
          Maybe.orSome(
            'viesti.all.messages.load-error',
            Response.localizationKey(response)
          )
        );

        flashMessageStore.add('viesti', 'error', msg);
        overlay = false;
      },
      response => {
        resources = Maybe.Some(response);
        ketjutCount = response.ketjutCount.count;
        overlay = false;
      },
      Future.parallelObject(4, {
        whoami: kayttajaApi.whoami,
        ketjutCount: api.getKetjutCount,
        ketjut: api.getKetjut({
          offset: R.map(R.compose(R.multiply(pageSize), R.dec), page),
          limit: Maybe.Some(pageSize)
        }),
        vastaanottajaryhmat: api.vastaanottajaryhmat,
        kasittelijat: api.getKasittelijat
      })
    );
  };

  $: load(page);

  const submitKasittelija = (ketjuId, kasittelijaId) => {
    updateKetju(ketjuId, {
      'kasittelija-id': kasittelijaId
    });
  };

  const submitKasitelty = (ketjuId, kasitelty) => {
    updateKetju(ketjuId, {
      kasitelty: kasitelty
    });
  };

  const updateKetju = R.compose(
    Future.fork(
      response => {
        const msg = $_(
          Maybe.orSome(
            `viesti.all.messages.update-error`,
            Response.localizationKey(response)
          )
        );
        flashMessageStore.add('viesti', 'update-error', msg);
        overlay = false;
      },
      _ => {
        flashMessageStore.add(
          'viesti',
          'success',
          $_(`viesti.all.messages.update-success`)
        );
        overlay = false;
        load(page);
      }
    ),
    R.tap(() => {
      overlay = true;
    }),
    api.putKetju(fetch)
  );
</script>

<style>
  .header > * {
    @apply block font-bold text-primary uppercase text-left tracking-wider bg-light text-sm;
  }
</style>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    {#each Maybe.toArray(resources) as { ketjut, whoami, vastaanottajaryhmat, kasittelijat }}
      <div class="flex justify-between">
        <H1 text={$_('viesti.all.title')} />
        <div class="font-bold">
          <Link
            icon={Maybe.Some('add_circle_outline')}
            text={$_('viesti.all.new-viesti')}
            href="#/viesti/new" />
        </div>
      </div>
      {#if ketjut.length === 0}
        <span>{$_('viesti.all.no-messages')}</span>
      {/if}
      <div class="my-6">
        {#each ketjut as ketju}
          <Viestiketju
            {ketju}
            {whoami}
            {vastaanottajaryhmat}
            {kasittelijat}
            {submitKasitelty} />
        {/each}
      </div>
    {/each}
    {#if R.gt(pageCount, 1)}
      <div class="pagination">
        <Pagination
          {pageCount}
          pageNum={page.orSome(1)}
          {nextPageCallback}
          itemsPerPage={pageSize}
          itemsCount={ketjutCount} />
      </div>
    {/if}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
