<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Schema from './schema';

  import * as ValvontaApi from './valvonta-api';

  import { _ } from '@Language/i18n';

  import Button from '@Component/Button/Button';
  import Textarea from '@Component/Textarea/Textarea';
  import { flashMessageStore } from '@/stores';
  import * as Validation from '@Utility/validation';

  const i18n = $_;
  const i18nRoot = 'valvonta.oikeellisuus.note';

  export let id;
  export let note;
  export let reload;

  let form;
  let error = Maybe.None();

  const schema = Schema.note;
  $: isValidForm = Validation.isValidForm(schema);

  $: add = toimenpide => {
    if (isValidForm(note)) {
      Future.fork(
        response => {
          const msg = i18n(
            Maybe.orSome(
              `${i18nRoot}.messages.add-error`,
              Response.localizationKey(response)
            )
          );
          error = Maybe.Some(msg);
        },
        _ => {
          flashMessageStore.add(
            'valvonta-oikeellisuus',
            'success',
            i18n(`${i18nRoot}.messages.add-success`)
          );
          reload();
        },
        ValvontaApi.postNote(id, note)
      );
    } else {
      error = Maybe.Some($_(`${i18nRoot}.messages.validation-error`));
      Validation.blurForm(form);
    }
  };

</script>

<style type="text/postcss">
  dialog {
    @apply fixed top-0 w-screen left-0 z-50 h-screen bg-hr cursor-default flex justify-center items-center;
  }

  .content {
    @apply relative bg-light w-2/3 py-10 px-10 rounded-md shadow-lg flex flex-col justify-center;
  }

  h1 {
    @apply text-secondary font-bold uppercase text-lg mb-4 pb-2 border-b-1 border-tertiary tracking-xl;
  }

  p {
    @apply mt-2;
  }

  .buttons {
    @apply flex flex-wrap items-center mt-5 border-t-1 border-tertiary;
  }

  .error {
    @apply flex py-2 px-2 bg-error text-light;
  }
</style>

<dialog on:click|stopPropagation>
  <form class="content" bind:this={form}>
    <h1>{i18n(i18nRoot + '.title')}</h1>

    {#each error.toArray() as txt}
      <div class="my-2 error">
        <span class="font-icon mr-2">error_outline</span>
        <div>{txt}</div>
      </div>
    {/each}

    <div class="w-full py-4">
      <Textarea
        id={'note.description'}
        name={'note.description'}
        label={i18n(i18nRoot + '.description')}
        bind:model={note}
        lens={R.lensProp('description')}
        required={false}
        validators={schema.description}
        {i18n} />
    </div>

    <div class="buttons">
      <div class="mr-5 mt-5">
        <Button
          text={i18n(i18nRoot + '.add-button')}
          on:click={add(note)} />
      </div>

      <div class="mt-5">
        <Button
          text={i18n(i18nRoot + '.cancel-button')}
          style={'secondary'}
          on:click={reload} />
      </div>
    </div>
  </form>
</dialog>
