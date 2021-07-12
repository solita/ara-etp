<script>
  import { tick } from 'svelte';
  import { querystring, location, push } from 'svelte-spa-router';
  import qs from 'qs';
  import * as R from 'ramda';

  import { flashMessageStore } from '@/stores';

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

  import { _ } from '@Language/i18n';

  export let where;
  export let keyword;
  export let id;
  export let luokittelut;
  export let whoami;

  let overlay = true;
  let schema = Kayttajat.isPaakayttajaOrLaskuttaja(whoami)
    ? EtHakuSchema.paakayttajaSchema
    : EtHakuSchema.laatijaSchema;

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
      params => qs.stringify(params, { encode: false }),
      R.dissoc('offset'),
      R.when(R.has('id'), R.dissoc('where')),
      R.when(R.propEq('keyword', ''), R.dissoc('keyword')),
      R.when(R.propEq('id', ''), R.dissoc('id')),
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

<!-- purgecss: bg-beige -->
{#if Maybe.isSome(laatijat)}
  <div class="flex w-full">
    <div class="w-7/12 flex flex-col justify-end">
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
        class="conjunction w-full flex justify-center"
        class:bg-beige={conjunction === 'or'}>
        <div class="flex justify-between w-1/6 my-5">
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
      <div class="flex justify-start items-center">
        <QueryBlock
          {operator}
          {key}
          {values}
          {index}
          {luokittelut}
          {schema}
          laatijat={Maybe.get(laatijat)} />
        <span
          class="text-secondary font-icon text-2xl cursor-pointer ml-4
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
              flashMessageStore.add(
                'energiatodistus',
                'warn',
                'Hakukriteerin poistamisessa tapahtui virhe.'
              );
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
          const newItems = R.compose(
            R.map(R.append(EtHakuUtils.defaultQueryItem())),
            R.map(EtHakuUtils.searchItems)
          )(Either.fromTry(() => EtHakuUtils.parseHakuForm(form)));
          if (Either.isRight(newItems)) {
            queryItems = Either.right(newItems);
            await tick();
            form.dispatchEvent(new Event('change'));
            R.last([...form.querySelectorAll('input:not(.sr-only)')]).focus();
          } else {
            flashMessageStore.add(
              'energiatodistus',
              'warn',
              'Hakukriteerin lisäyksessä tapahtui virhe.'
            );
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
