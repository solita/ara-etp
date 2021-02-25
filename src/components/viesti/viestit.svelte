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

  const pageSize = 3;
  let ketjutCount = 0;
  $: pageCount = Math.ceil(R.divide(parseInt(ketjutCount), pageSize));

  $: page = R.compose(
    R.chain(Either.toMaybe),
    R.map(Parsers.parseInteger),
    Maybe.fromEmpty,
    R.prop('page'),
    qs.parse
  )($querystring);

  const enableOverlay = _ => (overlay = true);

  const nextPageCallback = nextPage =>
    push(`#/viesti/all?page=${parseInt(nextPage) - 1}`);

  $: if (page) {
    R.compose(
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
          resources = Maybe.Some({
            whoami: response[0],
            ketjut: response[1]
          });
          ketjutCount = parseInt(response[2].count);
          overlay = false;
        }
      ),
      Future.parallel(3),
      R.tap(enableOverlay),
      R.append(api.getKetjutCount),
      R.pair(kayttajaApi.whoami),
      api.getKetjut,
      api.toQueryString
    )({ offset: page, limit: Maybe.Some(pageSize) });
  }
</script>

<style>
  .header > * {
    @apply block font-bold text-primary uppercase text-left tracking-wider bg-light text-sm;
  }
</style>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    {#each Maybe.toArray(resources) as { ketjut, whoami }}
      <H1 text={$_('viesti.all.title')} />
      <!--<div class="flex flex-col">
      <div class="flex header">
        <span class="w-1/6">{$_('viesti.all.senttime')}</span>
        <span class="w-1/3">{$_('viesti.all.osallistujat')}</span>
        <span class="w-1/2">{$_('viesti.all.content')}</span>
      </div>
    </div>-->
      {#if ketjut.length === 0}
        <span>{$_('viesti.all.no-messages')}</span>
      {/if}
      {#each ketjut as ketju}
        <Viestiketju {ketju} {whoami} />
      {/each}

      <div class="flex mt-5">
        <Link text={'Lisää uusi ketju'} href="#/viesti/new" />
      </div>
    {/each}
    {#if R.gt(pageCount, 1)}
      <div class="pagination">
        <Pagination
          {pageCount}
          pageNum={page.orSome(0) + 1}
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
