<script>
  import { flashMessageStore } from '@/stores';
  import { _ } from '@Language/i18n';

  import * as R from 'ramda';
  import * as Parsers from '@Utility/parsers';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Kayttajat from '@Utility/kayttajat';
  import { querystring } from 'svelte-spa-router';
  import { push } from '@Component/Router/router';
  import qs from 'qs';

  import * as api from './viesti-api';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import H1 from '@Component/H/H1.svelte';
  import Link from '../Link/Link.svelte';
  import Viestiketju from './viestiketju';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import Pagination from '@Component/Pagination/Pagination';
  import Checkbox from '@Component/Checkbox/Checkbox';

  const i18n = $_;

  let resources = Maybe.None();
  let overlay = true;

  const pageSize = 50;
  let ketjutCount = 0;
  let page = Maybe.Some(1);

  let filters = {
    'kasittelija-self': true,
    'no-kasittelija': true,
    'include-kasitelty': false
  };

  $: page = R.compose(
    R.chain(Either.toMaybe),
    R.map(Parsers.parseInteger),
    Maybe.fromEmpty,
    R.prop('page'),
    qs.parse
  )($querystring);
  $: pageCount = Math.ceil(R.divide(ketjutCount, pageSize));

  const nextPageCallback = nextPage => push(`#/viesti/all?page=${nextPage}`);

  const load = (page, filters) => {
    overlay = true;
    R.compose(
      Future.fork(
        response => {
          const msg = i18n(
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
        }
      ),
      R.chain(whoami =>
        Future.parallelObject(4, {
          whoami: Future.resolve(whoami),
          ketjutCount: api.getKetjutCount(
            Kayttajat.isLaatija()
              ? {}
              : {
                  'kasittelija-id': filters['kasittelija-self']
                    ? Maybe.Some(whoami.id)
                    : Maybe.None(),
                  'has-kasittelija': filters['no-kasittelija']
                    ? Maybe.Some(false)
                    : Maybe.None(),
                  'include-kasitelty': Maybe.Some(filters['include-kasitelty'])
                }
          ),
          ketjut: api.getKetjut(
            Kayttajat.isLaatija()
              ? {}
              : {
                  'kasittelija-id': filters['kasittelija-self']
                    ? Maybe.Some(whoami.id)
                    : Maybe.None(),
                  'has-kasittelija': filters['no-kasittelija']
                    ? Maybe.Some(false)
                    : Maybe.None(),
                  'include-kasitelty': Maybe.Some(filters['include-kasitelty']),
                  offset: R.map(R.compose(R.multiply(pageSize), R.dec), page),
                  limit: Maybe.Some(pageSize)
                }
          ),
          vastaanottajaryhmat: api.vastaanottajaryhmat,
          kasittelijat: api.getKasittelijat
        })
      )
    )(kayttajaApi.whoami);
  };

  $: load(page, filters);

  const submitKasitelty = (ketjuId, kasitelty) => {
    updateKetju(ketjuId, {
      kasitelty: kasitelty
    });
  };

  const updateKetju = R.compose(
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            `viesti.all.messages.update-error`,
            Response.localizationKey(response)
          )
        );
        flashMessageStore.add('viesti', 'error', msg);
        overlay = false;
      },
      _ => {
        flashMessageStore.add(
          'viesti',
          'success',
          i18n(`viesti.all.messages.update-success`)
        );
        overlay = false;
        load(page, filters);
      }
    ),
    R.tap(() => {
      overlay = true;
    }),
    api.putKetju(fetch)
  );
</script>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    {#each Maybe.toArray(resources) as { ketjut, whoami, vastaanottajaryhmat, kasittelijat }}
      <div class="flex justify-between">
        <H1 text={i18n('viesti.all.title')} />
        <div class="font-bold">
          <Link
            icon={Maybe.Some('add_circle_outline')}
            text={i18n('viesti.all.new-viesti')}
            href="#/viesti/new" />
        </div>
      </div>
      {#if !Kayttajat.isLaatija(whoami)}
        <div class="flex justify-between mb-4">
          <div class="flex">
            <Checkbox
              id={'checkbox.show-mine'}
              name={'checkbox.show-mine'}
              label={i18n('viesti.all.show-mine')}
              lens={R.lensProp('kasittelija-self')}
              bind:model={filters}
              disabled={false} />
            <Checkbox
              id={'checkbox.no-handler'}
              name={'checkbox.no-handler'}
              label={i18n('viesti.all.no-handler')}
              lens={R.lensProp('no-kasittelija')}
              bind:model={filters}
              disabled={false} />
          </div>
          <Checkbox
            id={'checkbox.show-handled'}
            name={'checkbox.show-handled'}
            label={i18n('viesti.all.show-handled')}
            lens={R.lensProp('include-kasitelty')}
            bind:model={filters}
            disabled={false} />
        </div>
      {/if}
      {#if ketjut.length === 0}
        <span>{i18n('viesti.all.no-messages')}</span>
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
