<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Parsers from '@Utility/parsers';
  import * as Formats from '@Utility/formats';
  import * as Validation from '@Utility/validation';
  import * as EM from '@Utility/either-maybe';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';

  import Datepicker from '@Component/Input/Datepicker';
  import Button from '@Component/Button/Button';
  import Spinner from '@Component/Spinner/Spinner';

  import { _ } from '@Language/i18n';
  import { announcementsForModule } from '@Utility/announce';

  const i18n = $_;
  const { announceSuccess } = announcementsForModule('valvonta-oikeellisuus');

  export let i18nRoot;
  export let putToimenpide;
  export let toimenpide;

  export let cancel;
  export let reload;

  export let schema = {
    'deadline-date': [Validation.isSome]
  };

  let form;
  let error = Maybe.None();
  let pending = false;

  $: isValidForm = Validation.isValidForm(schema);

  $: updateDeadline = toimenpide => {
    if (isValidForm(toimenpide)) {
      pending = true;
      Future.fork(
        response => {
          pending = false;
          const msg = i18n(
            Maybe.orSome(
              `${i18nRoot}.messages.save-error`,
              Response.localizationKey(response)
            )
          );
          error = Maybe.Some(msg);
        },
        _ => {
          pending = false;
          announceSuccess(i18n(`${i18nRoot}.messages.save-success`));
          cancel();
          reload();
        },
        putToimenpide(toimenpide.id, toimenpide)
      );
    } else {
      error = Maybe.Some(i18n(`${i18nRoot}.messages.validation-error`));
      Validation.blurForm(form);
    }
  };
</script>

<dialog
  class="fixed top-0 left-0 h-screen w-screen z-50 bg-hr cursor-default flex justify-center items-center"
  on:click|stopPropagation>
  <form
    class="relative bg-light w-2/3 py-10 px-10 rounded-md shadow-lg flex flex-col justify-center"
    bind:this={form}>
    <h1
      class="text-secondary font-bold uppercase text-lg mb-4 pb-2 border-b-1 border-tertiary tracking-xl">
      {i18n(i18nRoot + '.deadline-date-dialog.title')}
    </h1>
    <div class="flex">
      <Datepicker
        label={i18n(i18nRoot + '.deadline-date')}
        bind:model={toimenpide}
        required={true}
        lens={R.lensProp('deadline-date')}
        format={Maybe.fold('', Formats.formatDateInstant)}
        parse={Parsers.optionalParser(Parsers.parseDate)}
        transform={EM.fromNull}
        validators={schema['deadline-date']}
        {i18n} />
    </div>
    <div
      class="flex flex-wrap items-center mt-6 pt-6 border-t-1 border-tertiary space-x-5">
      {#if !pending}
        <Button
          text={i18n(i18nRoot + '.deadline-date-dialog.save')}
          disabled={!isValidForm(toimenpide)}
          on:click={_ => updateDeadline(toimenpide)} />
        <Button
          text={i18n(i18nRoot + '.deadline-date-dialog.cancel')}
          style={'secondary'}
          on:click={cancel} />
      {:else}
        <Spinner />
      {/if}
    </div>
  </form>
</dialog>
