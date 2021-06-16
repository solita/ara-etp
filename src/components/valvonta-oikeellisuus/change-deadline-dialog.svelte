<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Parsers from '@Utility/parsers';
  import * as Formats from '@Utility/formats';
  import * as Validation from '@Utility/validation';
  import * as EM from '@Utility/either-maybe';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';

  import * as ValvontaApi from './valvonta-api';

  import Datepicker from '@Component/Input/Datepicker';
  import Button from '@Component/Button/Button';
  import Spinner from '@Component/Spinner/Spinner';

  import { flashMessageStore } from '@/stores';

  const i18nRoot = 'valvonta.oikeellisuus.toimenpide';

  export let energiatodistus;
  export let toimenpide;
  export let i18n;

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
          flashMessageStore.add(
            'valvonta-oikeellisuus',
            'success',
            i18n(`${i18nRoot}.messages.save-success`)
          );
          cancel();
          reload();
        },
        ValvontaApi.putToimenpide(energiatodistus.id, toimenpide.id, toimenpide)
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
      Muuta määräaikaa
    </h1>

    <Datepicker
      label="Määräpäivä"
      bind:model={toimenpide}
      required={true}
      lens={R.lensProp('deadline-date')}
      format={Maybe.fold('', Formats.formatDateInstant)}
      parse={Parsers.optionalParser(Parsers.parseDate)}
      transform={EM.fromNull}
      validators={schema['deadline-date']}
      {i18n} />

    <div
      class="flex flex-wrap items-center mt-8 border-t-1 border-tertiary space-x-5">
      {#if !pending}
        <Button
          text={'Päivitä määräaika'}
          disabled={!isValidForm(toimenpide)}
          on:click={_ => updateDeadline(toimenpide)} />
        <Button text={'Peruuta'} style={'secondary'} on:click={cancel} />
      {:else}
        <Spinner />
      {/if}
    </div>
  </form>
</dialog>
