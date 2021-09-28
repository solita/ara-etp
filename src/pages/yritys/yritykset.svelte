<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as api from '@Pages/yritys/yritys-api';
  import * as qs from 'qs';
  import { replace, location, querystring } from 'svelte-spa-router';

  import { flashMessageStore } from '@/stores';
  import { _ } from '@Language/i18n';
  import { push } from '@Component/Router/router';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import H1 from '@Component/H/H1.svelte';
  import H2 from '@Component/H/H2.svelte';
  import Address from '@Component/address/address';
  import Input from '@Component/Input/Input';
  import PillInputWrapper from '@Component/Input/PillInputWrapper';

  const i18n = $_;

  let yritykset = [];
  let overlay = true;
  let cancel = () => {};

  Future.fork(
    _ => {
      flashMessageStore.add(
        'Yritys',
        'error',
        i18n(Response.errorKey(i18nRoot, 'load', response))
      );

      overlay = false;
    },
    response => {
      yritykset = R.sort(R.ascend(R.prop('nimi')), response);
      overlay = false;
    },
    api.getAllYritykset
  );

  const matchSearch = R.curry((search, yritys) =>
    R.compose(
      R.any(R.includes(R.compose(R.toLower, R.trim)(search))),
      R.map(R.toLower),
      R.flatten,
      R.values,
      R.pick([
        'nimi',
        'ytunnus',
        'jakeluosoite',
        'postinumero',
        'postitoimipaikka'
      ])
    )(yritys)
  );

  $: searchKeyword = Maybe.fromEmpty(qs.parse($querystring).search);
  $: results = Maybe.fold(
    yritykset,
    search => R.filter(matchSearch(search), yritykset),
    searchKeyword
  );

  $: R.compose(
    querystring => replace(`${$location}?${querystring}`),
    qs.stringify,
    R.objOf('search')
  )(Maybe.orSome('', searchKeyword));
</script>

<style>
</style>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    <H1 text={i18n('yritykset.title')} />

    <div class="my-4">
      <div class="flex flex-col lg:w-2/3 w-full">
        <H2 text={i18n('yritykset.search')} />
        <Input
          model={Maybe.orSome('', searchKeyword)}
          inputComponentWrapper={PillInputWrapper}
          search={true}
          on:input={evt => {
            cancel = R.compose(
              Future.value(val => {
                searchKeyword = Maybe.fromEmpty(R.trim(val));
              }),
              Future.after(200),
              R.tap(cancel)
            )(evt.target.value);
          }} />
      </div>
    </div>

    <div class="mt-10">
      <H1 text={`${i18n('yritykset.results')} ${R.length(results)}`} />
    </div>

    {#if R.not(R.isEmpty(results))}
      <div class="overflow-x-auto">
        <table class="etp-table">
          <thead class="etp-table--thead">
            <tr class="etp-table--tr">
              <th class="etp-table--th">{i18n('yritys.nimi')}</th>
              <th class="etp-table--th">{i18n('yritys.y-tunnus')}</th>
              <th class="etp-table--th">{i18n('yritys.laskutusosoite')}</th>
            </tr>
          </thead>
          <tbody class="etp-table--tbody">
            {#each results as yritys}
              <tr
                class="etp-table--tr etp-table--tr__link"
                on:click={() => push('#/yritys/' + yritys.id)}>
                <td class="etp-table--td">
                  {yritys.nimi}
                  {Maybe.fold(
                    '',
                    R.concat('/ ', R.__),
                    yritys['vastaanottajan-tarkenne']
                  )}
                </td>
                <td class="etp-table--td">{yritys.ytunnus}</td>
                <td class="etp-table--td"><Address address={yritys} /></td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    {/if}
  </div>
</Overlay>
