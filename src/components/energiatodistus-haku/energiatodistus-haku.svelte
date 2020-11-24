<script>
  import { tick } from 'svelte';
  import { slide } from 'svelte/transition';
  import { querystring, location, push } from 'svelte-spa-router';
  import qs from 'qs';
  import * as R from 'ramda';

  import { flashMessageStore } from '@/stores';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Future from '@Utility/future-utils';
  import * as Kayttajat from '@Utility/kayttajat';

  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';
  import * as api from '@Component/Energiatodistus/energiatodistus-api';
  import * as EtHakuSchema from '@Component/energiatodistus-haku/schema';

  import * as EtHakuUtils from './energiatodistus-haku-utils';
  import SimpleInput from '@Component/Input/SimpleInput';
  import PillInputWrapper from '@Component/Input/PillInputWrapper';
  import QueryBlock from './querybuilder/queryblock';
  import Button from '@Component/Button/Button';
  import TextButton from '@Component/Button/TextButton';
  import Radio from '@Component/Radio/Radio';

  import { _ } from '@Language/i18n';

  export let where;
  export let keyword;

  let overlay = true;
  let luokittelut = Maybe.None();
  let schema = Maybe.None();

  R.compose(
    Future.fork(
      () => {
        overlay = false;
      },
      response => {
        overlay = false;
        schema = Maybe.Some(
          Kayttajat.isPaakayttaja(response[0])
            ? EtHakuSchema.paakayttajaSchema
            : EtHakuSchema.laatijaSchema
        );

        luokittelut = Maybe.Some({
          2013: { ...response[1], ...response[2] },
          2018: { ...response[1], ...response[3] }
        });
      }
    ),
    Future.parallel(5),
    R.tap(() => (overlay = true))
  )([
    kayttajaApi.whoami,
    api.yhteisetLuokittelut,
    api.tarkoitusluokat(2013),
    api.tarkoitusluokat(2018)
  ]);

  const navigate = search => {
    R.compose(
      push,
      R.concat(`${$location}?`),
      params => qs.stringify(params, { encode: false }),
      R.when(R.propEq('keyword', ''), R.dissoc('keyword')),
      R.mergeRight(R.mergeRight(qs.parse($querystring), { keyword })),
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
      R.lift(EtHakuUtils.deserializeWhere)(schema),
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

<!-- purgecss: bg-beige -->
{#if Maybe.isSome(schema) && Maybe.isSome(luokittelut)}
  <div class="flex w-full">
    <div class="w-7/12 flex flex-col justify-end">
      <SimpleInput
        label={' '}
        wrapper={PillInputWrapper}
        rawValueAsViewValue={true}
        search={true}
        bind:rawValue={keyword} />
    </div>
  </div>

  <form
    bind:this={form}
    on:change={_ => {
      valid = R.compose(
        Either.orSome(false),
        R.map(
          R.compose(
            R.all(EtHakuUtils.isValidBlock(Maybe.get(schema))),
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
      valid = true;
    }}>
    {#each queryItems as { conjunction, block: [operator, key, ...values] }, index (`${index}_${operator}_${key}_${R.join('_', values)}`)}
      <div
        class="conjunction w-full flex justify-center"
        class:bg-beige={conjunction === 'or'}>
        <div class="flex justify-between w-1/6 my-5">
          <Radio
            group={conjunction}
            value={'and'}
            label={$_('energiatodistus.haku.and')}
            name={`${index}_conjunction`}
            on:click={_ => {
              queryItems = R.over(R.lensIndex(index), R.assoc('conjunction', 'and'), queryItems);
            }} />
          <Radio
            group={conjunction}
            value={'or'}
            label={$_('energiatodistus.haku.or')}
            name={`${index}_conjunction`}
            on:click={_ => {
              queryItems = R.over(R.lensIndex(index), R.assoc('conjunction', 'or'), queryItems);
            }} />
        </div>
      </div>
      <div class="flex justify-start items-center">
        <QueryBlock
          {operator}
          {key}
          {values}
          {index}
          schema={Maybe.get(schema)}
          luokittelut={Maybe.get(luokittelut)} />
        <span
          class="text-secondary font-icon text-2xl cursor-pointer ml-4
          hover:text-error"
          on:click={async _ => {
            const newItems = R.compose(R.map(EtHakuUtils.removeQueryItem(index)), R.map(EtHakuUtils.searchItems))(Either.fromTry(
                () => EtHakuUtils.parseHakuForm(form)
              ));
            if (Either.isRight(newItems)) {
              queryItems = Either.right(newItems);
              await tick();
              form.dispatchEvent(new Event('change'));
            } else {
              flashMessageStore.add('energiatodistus', 'warn', 'Hakukriteerin poistamisessa tapahtui virhe.');
            }
          }}>
          cancel
        </span>
      </div>
    {/each}

    <div class="flex my-4">
      <TextButton
        text={$_('energiatodistus.haku.lisaa_hakuehto')}
        icon={'add_circle_outline'}
        type={'button'}
        on:click={async _ => {
          const newItems = R.compose(R.map(R.append(EtHakuUtils.defaultQueryItem())), R.map(EtHakuUtils.searchItems))(Either.fromTry(
              () => EtHakuUtils.parseHakuForm(form)
            ));
          if (Either.isRight(newItems)) {
            queryItems = Either.right(newItems);
            await tick();
            form.dispatchEvent(new Event('change'));
          } else {
            flashMessageStore.add('energiatodistus', 'warn', 'Hakukriteerin lisäyksessä tapahtui virhe.');
          }
        }} />
    </div>

    <div class="flex">
      <div class="w-1/5">
        <Button
          disabled={!valid}
          type="submit"
          text={$_('energiatodistus.haku.hae')} />
      </div>
      <div class="w-1/3">
        <Button
          type="reset"
          style="secondary"
          text={$_('energiatodistus.haku.tyhjenna_hakuehdot')} />
      </div>
    </div>
  </form>
{/if}
