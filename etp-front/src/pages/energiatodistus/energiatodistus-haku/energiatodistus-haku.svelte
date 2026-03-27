<script>
  import { tick } from 'svelte';
  import { querystring, location, push } from 'svelte-spa-router';
  import qs from 'qs';
  import * as R from 'ramda';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Future from '@Utility/future-utils';
  import * as Kayttajat from '@Utility/kayttajat';

  import * as EtHakuSchema from './schema';
  import * as laatijaApi from '@Pages/laatija/laatija-api';

  import * as EtHakuUtils from './energiatodistus-haku-utils';
  import Input from '@Component/Input/Input';
  import PillInputWrapper from '@Component/Input/PillInputWrapper';
  import QueryBlock from './querybuilder/queryblock';
  import Button from '@Component/Button/Button';
  import TextButton from '@Component/Button/TextButton';
  import Radio from '@Component/Radio/Radio';
  import { isEtp2026Enabled } from '@Utility/config_utils.js';

  import { _ } from '@Language/i18n';
  import { announcementsForModule } from '@Utility/announce';

  export let config;
  export let where;
  export let keyword;
  export let kunnat;
  export let id;
  export let luokittelut;
  export let whoami;

  let overlay = true;
  let schema = R.when(
    R.always(R.not(isEtp2026Enabled(config))),
    R.omit([
      'perusparannuspassi.id',
      'perusparannuspassi.valid',
      'energiatodistus.perustiedot.havainnointikayntityyppi-id',
      'energiatodistus.perustiedot.tayttaa-aplus-vaatimukset',
      'energiatodistus.perustiedot.tayttaa-a0-vaatimukset',
      'energiatodistus.lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin',
      'energiatodistus.lahtotiedot.lammitys.lammonjako-lampotilajousto',
      'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko',
      'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkolampo',
      'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.tuulisahko',
      'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.lampopumppu',
      'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.muulampo',
      'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.muusahko',
      'energiatodistus.toteutunut-ostoenergiankulutus.tietojen-alkuperavuosi',
      'energiatodistus.toteutunut-ostoenergiankulutus.lisatietoja-fi',
      'energiatodistus.toteutunut-ostoenergiankulutus.lisatietoja-sv',
      'energiatodistus.toteutunut-ostoenergiankulutus.uusiutuvat-polttoaineet-vuosikulutus-yhteensa',
      'energiatodistus.toteutunut-ostoenergiankulutus.fossiiliset-polttoaineet-vuosikulutus-yhteensa',
      'energiatodistus.toteutunut-ostoenergiankulutus.uusiutuva-energia-vuosituotto-yhteensa',
      'energiatodistus.huomiot.lammitys.kayttoikaa-jaljella-arvio-vuosina',
      'energiatodistus.ilmastoselvitys.laatimisajankohta',
      'energiatodistus.ilmastoselvitys.laatija',
      'energiatodistus.ilmastoselvitys.yritys',
      'energiatodistus.ilmastoselvitys.yritys-osoite',
      'energiatodistus.ilmastoselvitys.yritys-postinumero',
      'energiatodistus.ilmastoselvitys.yritys-postitoimipaikka',
      'energiatodistus.ilmastoselvitys.laadintaperuste',
      'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennus.rakennustuotteiden-valmistus',
      'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennus.kuljetukset-tyomaavaihe',
      'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennus.rakennustuotteiden-vaihdot',
      'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennus.energiankaytto',
      'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennus.purkuvaihe',
      'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennuspaikka.rakennustuotteiden-valmistus',
      'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennuspaikka.kuljetukset-tyomaavaihe',
      'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennuspaikka.rakennustuotteiden-vaihdot',
      'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennuspaikka.energiankaytto',
      'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennuspaikka.purkuvaihe',
      'energiatodistus.ilmastoselvitys.hiilikadenjalki.rakennus.uudelleenkaytto',
      'energiatodistus.ilmastoselvitys.hiilikadenjalki.rakennus.kierratys',
      'energiatodistus.ilmastoselvitys.hiilikadenjalki.rakennus.ylimaarainen-uusiutuvaenergia',
      'energiatodistus.ilmastoselvitys.hiilikadenjalki.rakennus.hiilivarastovaikutus',
      'energiatodistus.ilmastoselvitys.hiilikadenjalki.rakennus.karbonatisoituminen',
      'energiatodistus.ilmastoselvitys.hiilikadenjalki.rakennuspaikka.uudelleenkaytto',
      'energiatodistus.ilmastoselvitys.hiilikadenjalki.rakennuspaikka.kierratys',
      'energiatodistus.ilmastoselvitys.hiilikadenjalki.rakennuspaikka.ylimaarainen-uusiutuvaenergia',
      'energiatodistus.ilmastoselvitys.hiilikadenjalki.rakennuspaikka.hiilivarastovaikutus',
      'energiatodistus.ilmastoselvitys.hiilikadenjalki.rakennuspaikka.karbonatisoituminen'
    ])
  )(
    Kayttajat.isPaakayttajaOrLaskuttaja(whoami)
      ? EtHakuSchema.paakayttajaSchema
      : EtHakuSchema.laatijaSchema
  );

  const { announceWarning } = announcementsForModule('Energiatodistus');

  let laatijat = Maybe.None();

  Future.fork(
    () => {
      overlay = false;
    },
    response => {
      overlay = false;
      laatijat = Maybe.Some(response);
    },
    Kayttajat.isPaakayttaja(whoami) ? laatijaApi.laatijat : Future.resolve([])
  );

  const navigate = search => {
    R.compose(
      push,
      R.concat(`${$location}?`),
      // Encode '+' as '%2B' so qs.parse won't decode it as a space (e.g. "A+" → "A ")
      s => s.replace(/\+/g, '%2B'),
      params => qs.stringify(params, { encode: false }),
      R.dissoc('offset'),
      R.when(R.has('id'), R.dissoc('where')),
      R.when(R.propEq('', 'keyword'), R.dissoc('keyword')),
      R.when(R.propEq('', 'id'), R.dissoc('id')),
      R.mergeRight(R.mergeRight(qs.parse($querystring), { keyword, id })),
      R.assoc('where', R.__, {})
    )(search);
  };

  let queryItems = [];

  let form;

  let valid = true;

  $: queryItems = R.ifElse(
    R.length,
    R.compose(
      Maybe.orSome([]),
      R.lift(EtHakuUtils.deserializeWhere)(Maybe.Some(schema)),
      Maybe.Some
    ),
    R.always([])
  )(where);
</script>

<style>
  .conjunction:first-of-type {
    display: none;
  }
</style>

{#if Maybe.isSome(laatijat)}
  <div class="flex w-full">
    <div class="flex w-7/12 flex-col justify-end">
      <Input
        bind:model={id}
        label={$_('energiatodistus.id')}
        search={true}
        inputComponentWrapper={PillInputWrapper}
        on:keypress={evt => {
          if (evt.key === 'Enter') {
            form.dispatchEvent(new Event('submit'));
          }
        }} />
    </div>
  </div>

  <form
    bind:this={form}
    on:change={_ => {
      valid = R.compose(
        Either.orSome(false),
        R.map(
          R.compose(
            R.all(EtHakuUtils.isValidBlock(schema)),
            R.map(R.prop('block')),
            EtHakuUtils.searchItems
          )
        ),
        f => Either.fromTry(() => EtHakuUtils.parseHakuForm(f))
      )(form);
    }}
    novalidate
    on:submit|preventDefault={evt => {
      R.compose(
        R.chain(navigate),
        R.map(EtHakuUtils.searchString),
        R.map(EtHakuUtils.searchItems),
        evt => Either.fromTry(() => EtHakuUtils.parseHakuForm(evt.target))
      )(evt);
    }}
    on:reset|preventDefault={_ => {
      queryItems = [];
      keyword = '';
      id = '';
      valid = true;
    }}>
    {#each queryItems as { conjunction, block: [operator, key, ...values] }, index (`${index}_${operator}_${key}_${R.join('_', values)}`)}
      <div
        class="conjunction flex w-full justify-center"
        class:bg-beige={conjunction === 'or'}>
        <div class="my-5 flex w-1/6 justify-between">
          <Radio
            group={conjunction}
            value={'and'}
            label={$_('energiatodistus.haku.and')}
            name={`${index}_conjunction`}
            on:click={_ => {
              queryItems = R.over(
                R.lensIndex(index),
                R.assoc('conjunction', 'and'),
                queryItems
              );
            }} />
          <Radio
            group={conjunction}
            value={'or'}
            label={$_('energiatodistus.haku.or')}
            name={`${index}_conjunction`}
            on:click={_ => {
              queryItems = R.over(
                R.lensIndex(index),
                R.assoc('conjunction', 'or'),
                queryItems
              );
            }} />
        </div>
      </div>
      <div class="flex items-center justify-start">
        <QueryBlock
          {config}
          {operator}
          {key}
          {values}
          {index}
          {luokittelut}
          {kunnat}
          {schema}
          laatijat={Maybe.get(laatijat)} />
        <span
          class="ml-4 cursor-pointer font-icon text-2xl text-secondary
          hover:text-error"
          on:click={async _ => {
            const newItems = R.compose(
              R.map(EtHakuUtils.removeQueryItem(index)),
              R.map(EtHakuUtils.searchItems)
            )(Either.fromTry(() => EtHakuUtils.parseHakuForm(form)));
            if (Either.isRight(newItems)) {
              queryItems = Either.right(newItems);
              await tick();
              form.dispatchEvent(new Event('change'));
            } else {
              announceWarning('Hakukriteerin poistamisessa tapahtui virhe.');
            }
          }}>
          cancel
        </span>
      </div>
    {/each}

    <div class="my-4 flex">
      <TextButton
        text={$_('energiatodistus.haku.lisaa_hakuehto')}
        icon={'add_circle_outline'}
        type={'button'}
        on:click={async _ => {
          const newItems = R.compose(
            R.map(R.append(EtHakuUtils.defaultQueryItem())),
            R.map(EtHakuUtils.searchItems)
          )(Either.fromTry(() => EtHakuUtils.parseHakuForm(form)));
          if (Either.isRight(newItems)) {
            queryItems = Either.right(newItems);
            await tick();
            form.dispatchEvent(new Event('change'));
            R.last([...form.querySelectorAll('input:not(.hidden)')]).focus();
          } else {
            announceWarning('Hakukriteerin lisäyksessä tapahtui virhe.');
          }
        }} />
    </div>

    <div class="flex space-x-8">
      <Button
        disabled={!valid}
        type="submit"
        text={$_('energiatodistus.haku.hae')} />
      <Button
        type="reset"
        style="secondary"
        text={$_('energiatodistus.haku.tyhjenna_hakuehdot')} />
    </div>
  </form>
{/if}
