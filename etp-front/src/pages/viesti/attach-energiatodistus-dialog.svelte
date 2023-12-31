<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Parsers from '@Utility/parsers';
  import * as Validation from '@Utility/validation';

  import Input from '@Component/Input/Input';
  import { _ } from '@Language/i18n';
  import * as api from './viesti-api';
  import Dialog from '@Component/dialog/dialog';

  export let close;
  export let ketjuId;
  export let energiatodistusId;
  $: inputEtId = Either.Right(energiatodistusId);

  const i18n = $_;
  const i18nRoot = 'viesti.ketju.existing.attach-to-et';

  let form;
  let showAttachSpinner = false;
  let showDetachSpinner = false;
  let error = Maybe.None();

  const updateKetju = R.compose(
    Future.fork(
      response => {
        const invalidEtId =
          response.body.constraint === 'viestiketju-energiatodistus-id-fkey' &&
          response.body.type === 'foreign-key-violation';

        const msg = i18n(
          invalidEtId
            ? `${i18nRoot}.messages.update-error-missing-et`
            : Maybe.orSome(
                `${i18nRoot}.messages.update-error`,
                Response.localizationKey(response)
              )
        );
        buttonsDisabled = false;
        showAttachSpinner = false;
        showDetachSpinner = false;
        error = Maybe.Some(msg);
      },
      _ => {
        buttonsDisabled = false;
        showAttachSpinner = false;
        showDetachSpinner = false;
        close(true);
      }
    ),
    R.tap(() => {
      buttonsDisabled = true;
    }),
    api.putKetju(fetch, ketjuId)
  );

  const attach = () => {
    if (Maybe.isNone(EM.toMaybe(inputEtId))) {
      error = Maybe.Some(i18n(`${i18nRoot}.messages.validation-error`));
    } else {
      error = Maybe.None();
      showAttachSpinner = true;

      updateKetju({
        'energiatodistus-id': EM.orSome(null, inputEtId)
      });
    }
  };
  const detach = () => {
    error = Maybe.None();
    showDetachSpinner = true;
    updateKetju({
      'energiatodistus-id': null
    });
  };

  let buttonsDisabled = false;
</script>

<Dialog
  bind:form
  header={i18n(i18nRoot + '.title')}
  {error}
  buttons={[
    {
      disabled: buttonsDisabled,
      'on:click': attach,
      style: 'primary',
      text: i18n(i18nRoot + '.button-attach'),
      showSpinner: showAttachSpinner
    },
    {
      disabled: Maybe.isNone(energiatodistusId) || buttonsDisabled,
      'on:click': detach,
      style: 'secondary',
      text: i18n(i18nRoot + '.button-detach'),
      showSpinner: showDetachSpinner
    },
    {
      disabled: buttonsDisabled,
      'on:click': () => {
        close(false);
      },
      style: 'secondary',
      text: i18n('peruuta')
    }
  ]}>
  <div class="lg:mr-64 my-4 w-32">
    <Input
      id={'dialog.attach-et.input'}
      name={'dialog.attach-et.input'}
      label={i18n(i18nRoot + '.input-label')}
      compact={false}
      required={true}
      bind:model={inputEtId}
      format={Maybe.orSome('')}
      parse={Parsers.optionalParser(Parsers.parseInteger)}
      validators={[Validation.isSome]}
      {i18n} />
  </div>
</Dialog>
