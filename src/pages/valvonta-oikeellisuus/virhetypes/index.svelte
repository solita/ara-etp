<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as qs from 'qs';
  import { tick } from 'svelte';

  import * as VirhetyyppiApi from './api';
  import * as KayttajaApi from '@Pages/kayttaja/kayttaja-api';

  import { replace, location, querystring } from 'svelte-spa-router';
  import { flashMessageStore } from '@/stores';
  import { _ } from '@Language/i18n';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import H1 from '@Component/H/H1.svelte';
  import Input from '@Component/Input/Input';
  import PillInputWrapper from '@Component/Input/PillInputWrapper';
  import Table from './table.svelte';
  import * as Response from '@Utility/response';

  const i18n = $_;
  const i18nRoot = 'valvonta.oikeellisuus.virhetypes'

  let resources = Maybe.None();
  let overlay = true;
  let dirty = false;
  let cancel = () => {};

  const load = _ => {
    overlay = true;
    Future.fork(
      response => {
        flashMessageStore.add(
          'valvonta-oikeellisuus',
          'error',
          i18n(Response.errorKey(i18nRoot, 'load', response))
        );
        overlay = false;
      },
      response => {
        resources = Maybe.Some(response);
        overlay = false;
        dirty = false;
      },
      Future.parallelObject(1, {
        virhetypes: VirhetyyppiApi.virhetyypit
      })
    );
  }
  load();

  const matchSearch = R.curry((search, virhetyyppi) =>
    R.compose(
      R.any(R.includes(R.compose(R.toLower, R.trim)(search))),
      R.map(R.toLower),
      R.values,
      R.pick([
        'label-fi',
        'label-sv'
      ])
    )(virhetyyppi)
  );

  $: searchKeyword = Maybe.fromEmpty(qs.parse($querystring).search);

  $: search = tyypit => Maybe.fold(
    tyypit,
    search => R.filter(matchSearch(search), tyypit),
    searchKeyword);

  $: R.compose(
    querystring => replace(`${$location}?${querystring}`),
    qs.stringify,
    R.objOf('search')
  )(Maybe.orSome('', searchKeyword));

  const fork = (key, successCallback, future) => {
    overlay = true;
    Future.fork(
      response => {
        flashMessageStore.add('valvonta-oikeellisuus', 'error',
          Response.errorKey(i18nRoot, key, response));
        overlay = false;
      },
      response => {
        flashMessageStore.add(
          'valvonta-oikeellisuus',
          'success',
          i18n(`${i18nRoot}.messages.${key}-success`)
        );
        overlay = false;
        dirty = false;
        successCallback(response);
      },
      future
    );
  };

  const api = {
    reload: load,
    save: (virhetype, successCallback) =>
      fork('save', successCallback, VirhetyyppiApi.putVirhetype(virhetype.id, virhetype))
  }
</script>

<style>
</style>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    <H1 text={i18n(i18nRoot + '.title')} />

    {#each Maybe.toArray(resources) as { virhetypes }}
      <div class="flex my-4">
        <div class="lg:w-1/2 w-full">
          <Input
            model={Maybe.orSome('', searchKeyword)}
            inputComponentWrapper={PillInputWrapper}
            placeholder={i18n(i18nRoot + '.search')}
            label={i18n(i18nRoot + '.search')}
            compact={true}
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
      <div class="mt-4">
        <Table virhetypes={search(virhetypes)} {api} bind:dirty />
      </div>
    {/each}
  </div>
</Overlay>
