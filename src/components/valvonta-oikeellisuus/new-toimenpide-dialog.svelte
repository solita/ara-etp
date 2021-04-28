<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Parsers from '@Utility/parsers';
  import * as Formats from '@Utility/formats';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Locales from '@Language/locale-utils';


  import * as Toimenpiteet from './toimenpiteet';
  import * as Schema from './schema';

  import * as ValvontaApi from './valvonta-api';

  import { _, locale } from '@Language/i18n';

  import Button from '@Component/Button/Button';
  import Textarea from '@Component/Textarea/Textarea';
  import Datepicker from '@Component/Input/Datepicker';
  import { flashMessageStore } from '@/stores';
  import Select from '../Select/Select.svelte';
  import * as Validation from '@Utility/validation';

  const i18n = $_;
  const i18nRoot = 'valvonta.oikeellisuus.toimenpide';

  export let id;
  export let templatesByType;
  export let toimenpide;
  export let reload;

  let form;
  let error = Maybe.None();

  const text = R.compose(i18n, Toimenpiteet.i18nKey);

  $: templates = Toimenpiteet.templates(templatesByType)(toimenpide);
  $: formatTemplate  = Locales.labelForId($locale, templates);

  $: schema = Schema.toimenpidePublish(templates, toimenpide);
  $: isValidForm = Validation.isValidForm(schema);

  $: publish = toimenpide => {
    if (isValidForm(toimenpide)) {
      Future.fork(
        response => {
          const msg = i18n(
            Maybe.orSome(
              `${i18nRoot}.messages.publish-error`,
              Response.localizationKey(response)
            )
          );
          error = Maybe.Some(msg);
        },
        _ => {
          flashMessageStore.add(
            'valvonta-oikeellisuus',
            'success',
            i18n(`${i18nRoot}.messages.publish-success`)
          );
          reload();
        },
        ValvontaApi.postToimenpide(id, toimenpide)
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
    <h1>{text(toimenpide, 'title')}</h1>

    {#each error.toArray() as txt}
      <div class="my-2 error">
        <span class="font-icon mr-2">error_outline</span>
        <div>{txt}</div>
      </div>
    {/each}

    <p>{text(toimenpide, 'description')}</p>

    {#if Toimenpiteet.hasDeadline(toimenpide)}
      <div class="flex py-4">
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
      </div>
    {/if}

    {#if !R.isEmpty(templates)}
      <div class="w-1/2 py-4">
        <Select
          label="Valitse asiakirjapohja"
          bind:model={toimenpide}
          lens={R.lensProp('template-id')}
          parse={Maybe.fromNull}
          required={true}
          validation={true}
          format={formatTemplate}
          items={R.pluck('id', templates)} />
      </div>
    {:else}
      <div class="w-full py-4">
        <Textarea
          id={'toimenpide.document'}
          name={'toimenpide.document'}
          label={text(toimenpide, 'document')}
          bind:model={toimenpide}
          lens={R.lensProp('document')}
          required={false}
          format={Maybe.orSome('')}
          parse={Parsers.optionalString}
          validators={schema.document}
          {i18n} />
      </div>
    {/if}

    <div class="buttons">
      <div class="mr-5 mt-5">
        <Button
          text={text(toimenpide, 'publish-button')}
          on:click={publish(toimenpide)} />
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
