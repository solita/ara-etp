<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Schema from './schema';

  import * as ValvontaApi from './valvonta-api';

  import { _ } from '@Language/i18n';

  import Textarea from '@Component/Textarea/Textarea';
  import { flashMessageStore } from '@/stores';
  import * as Validation from '@Utility/validation';
  import Dialog from '@Component/dialog/dialog';

  const i18n = $_;
  const i18nRoot = 'valvonta.oikeellisuus.note';

  export let id;
  export let note;
  export let reload;

  let form;
  let error = Maybe.None();

  const schema = Schema.note;

  let addPending = false;

  $: isValidForm = Validation.isValidForm(schema);

  $: add = toimenpide => {
    if (isValidForm(note)) {
      addPending = true;
      Future.fork(
        response => {
          addPending = false;
          const msg = i18n(
            Maybe.orSome(
              `${i18nRoot}.messages.add-error`,
              Response.localizationKey(response)
            )
          );
          error = Maybe.Some(msg);
        },
        _ => {
          addPending = false;
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

<Dialog
  bind:form
  header={i18n(i18nRoot + '.title')}
  {error}
  buttons={[
    {
      text: i18n(i18nRoot + '.add-button'),
      disabled: addPending,
      showSpinner: addPending,
      'on:click': _ => add(note)
    },
    {
      text: i18n(i18nRoot + '.cancel-button'),
      style: 'secondary',
      disabled: addPending,
      'on:click': reload
    }
  ]}>
  <div class="w-full py-4">
    <Textarea
      disabled={addPending}
      id={'note.description'}
      name={'note.description'}
      label={i18n(i18nRoot + '.description')}
      bind:model={note}
      lens={R.lensProp('description')}
      required={false}
      validators={schema.description}
      {i18n} />
  </div>
</Dialog>
