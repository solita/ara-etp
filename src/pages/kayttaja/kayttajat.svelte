<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Formats from '@Utility/formats';
  import * as Response from '@Utility/response';
  import * as Locales from '@Language/locale-utils';
  import * as KayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as LaatijaApi from '@Pages/laatija/laatija-api';

  import { flashMessageStore } from '@/stores';
  import { _, locale } from '@Language/i18n';
  import { push } from '@Component/Router/router';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import H1 from '@Component/H/H1.svelte';
  import H2 from '@Component/H/H2.svelte';
  import Link from '../../components/Link/Link.svelte';

  const i18n = $_;
  const i18nRoot = 'kayttajat';

  let resources = Maybe.None();
  let overlay = true;

  Future.fork(
    response => {
      flashMessageStore.add(
        'kayttaja',
        'error',
        i18n(Response.errorKey(i18nRoot, 'load', response))
      );
      overlay = false;
    },
    response => {
      resources = Maybe.Some(response);
      overlay = false;
    },
    Future.parallelObject(4, {
      kayttajat: KayttajaApi.kayttajat,
      kumppanit: R.map(
        R.filter(R.prop('partner')),
        LaatijaApi.laatijat
      ),
      roolit: KayttajaApi.roolit
    })
  );
</script>

<style>
</style>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    {#each Maybe.toArray(resources) as { roolit, kumppanit, kayttajat }}
      <div class="flex justify-between">
        <H1 text={i18n(i18nRoot + '.title')} />
        <div class="font-bold">
          <Link
            icon={Maybe.Some('add_circle_outline')}
            text={i18n(i18nRoot + '.new-kayttaja')}
            href="#/kayttaja/new" />
        </div>
      </div>

      {#if R.not(R.isEmpty(kayttajat))}
        <div class="overflow-x-auto">
          <table class="etp-table">
            <thead class="etp-table--thead">
              <tr class="etp-table--tr">
                <th class="etp-table--th">{i18n(i18nRoot + '.nimi')}</th>
                <th class="etp-table--th">{i18n(i18nRoot + '.rooli')}</th>
                <th class="etp-table--th">{i18n(i18nRoot + '.email')}</th>
                <th class="etp-table--th">{i18n(i18nRoot + '.puhelin')}</th>
                <th class="etp-table--th">{i18n(i18nRoot + '.login')}</th>
                <th class="etp-table--th"
                  >{i18n(i18nRoot + '.state.header')}</th>
              </tr>
            </thead>
            <tbody class="etp-table--tbody">
              {#each kayttajat as kayttaja}
                <tr
                  class="etp-table--tr etp-table--tr__link"
                  on:click={() => push('#/kayttaja/' + kayttaja.id)}>
                  <td class="etp-table--td">
                    {kayttaja.etunimi}
                    {kayttaja.sukunimi}
                  </td>
                  <td class="etp-table--td">
                    {Locales.labelForId($locale, roolit)(kayttaja.rooli)}
                  </td>
                  <td class="etp-table--td">{kayttaja.email}</td>
                  <td class="etp-table--td">{kayttaja.puhelin}</td>
                  <td class="etp-table--td">
                    {Maybe.fold(
                      i18n('kayttaja.no-login'),
                      Formats.formatTimeInstantMinutes,
                      kayttaja.login
                    )}
                  </td>
                  <td class="etp-table--td">
                    {kayttaja.passivoitu
                      ? i18n(i18nRoot + '.state.passivoitu')
                      : i18n(i18nRoot + '.state.active')}
                  </td>
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
      {/if}

      <div class="flex justify-between mt-8">
        <H2 text={i18n(i18nRoot + '.kumppanit')} />
        <div class="font-bold">
          <Link
            icon={Maybe.Some('add_circle_outline')}
            text={i18n(i18nRoot + '.new-kumppani')}
            href="#/laatija/new" />
        </div>
      </div>

      {#if R.not(R.isEmpty(kumppanit))}
        <div class="overflow-x-auto">
          <table class="etp-table">
            <thead class="etp-table--thead">
              <tr class="etp-table--tr">
                <th class="etp-table--th">{i18n(i18nRoot + '.nimi')}</th>
                <th class="etp-table--th">{i18n(i18nRoot + '.email')}</th>
                <th class="etp-table--th">{i18n(i18nRoot + '.puhelin')}</th>
                <th class="etp-table--th">{i18n(i18nRoot + '.login')}</th>
                <th class="etp-table--th"
                  >{i18n(i18nRoot + '.state.header')}</th>
              </tr>
            </thead>
            <tbody class="etp-table--tbody">
              {#each kumppanit as kumppani}
                <tr
                  class="etp-table--tr etp-table--tr__link"
                  on:click={() => push('#/kayttaja/' + kumppani.id)}>
                  <td class="etp-table--td">
                    {kumppani.etunimi}
                    {kumppani.sukunimi}
                  </td>
                  <td class="etp-table--td">{kumppani.email}</td>
                  <td class="etp-table--td">{kumppani.puhelin}</td>
                  <td class="etp-table--td">
                    {Maybe.fold(
                      i18n('kayttaja.no-login'),
                      Formats.formatTimeInstantMinutes,
                      kumppani.login
                    )}
                  </td>
                  <td class="etp-table--td">
                    {kumppani.passivoitu
                      ? i18n(i18nRoot + '.state.passivoitu')
                      : i18n(i18nRoot + '.state.active')}
                  </td>
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
      {/if}

      <p class="mt-8">
        <span class="font-icon">info</span>
        {i18n(i18nRoot + '.info')}
        <Link href="#/laatija/all" text={i18n(i18nRoot + '.laatija-link')} />
      </p>
    {/each}
  </div>
</Overlay>
