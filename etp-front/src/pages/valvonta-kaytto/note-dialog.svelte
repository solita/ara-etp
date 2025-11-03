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
  import * as Validation from '@Utility/validation';
  import { announcementsForModule } from '@Utility/announce';

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.note';
  const { announceSuccess } = announcementsForModule('valvonta-kaytto');

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
          announceSuccess(i18n(`${i18nRoot}.messages.add-success`));
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
    @apply fixed left-0 top-0 z-50 flex h-screen w-screen cursor-default items-center justify-center bg-hr;
  }

  .content {
    @apply relative flex w-2/3 flex-col justify-center rounded-md bg-light px-10 py-10 shadow-lg;
  }

  h1 {
    @apply mb-4 border-b-1 border-tertiary pb-2 text-lg font-bold uppercase tracking-xl text-secondary;
  }

  .buttons {
    @apply mt-5 flex flex-wrap items-center border-t-1 border-tertiary;
  }

  .error {
    @apply flex bg-error px-2 py-2 text-light;
  }
</style>

<dialog on:click|stopPropagation>
  <form class="content" bind:this={form}>
    <h1>{i18n(i18nRoot + '.title')}</h1>

    {#each error.toArray() as txt}
      <div class="error my-2">
        <span class="mr-2 font-icon">error_outline</span>
        <div role="alert">{txt}</div>
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
        <Button text={i18n(i18nRoot + '.add-button')} on:click={add(note)} />
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
