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

  import Textarea from '@Component/Textarea/Textarea';
  import Datepicker from '@Component/Input/Datepicker';
  import { flashMessageStore } from '@/stores';
  import Dialog from '@Component/dialog/dialog';
  import Select from '@Component/Select/Select.svelte';
  import * as Validation from '@Utility/validation';
  import Radio from '@Component/Radio/Radio';
  import Fieldset from '@Component/Fieldset/Fieldset';

  const i18n = $_;
  const i18nRoot = 'valvonta.oikeellisuus.toimenpide';

  export let id;
  export let templatesByType;
  export let toimenpide;
  export let reload;

  let form;
  let error = Maybe.None();

  const text = R.compose(i18n, Toimenpiteet.i18nKey);

  let publishPending = false;
  let previewPending = false;

  let anomalyMessageLanguage = 'fi';

  $: templates = Toimenpiteet.templates(templatesByType)(toimenpide);
  $: formatTemplate = Locales.labelForId($locale, templates);

  $: schema = Schema.toimenpidePublish(templates, toimenpide);
  $: isValidForm = Validation.isValidForm(schema);

  $: publish = toimenpide => {
    if (isValidForm(toimenpide)) {
      publishPending = true;
      Future.fork(
        response => {
          publishPending = false;
          const msg = i18n(
            Maybe.orSome(
              `${i18nRoot}.messages.publish-error`,
              Response.localizationKey(response)
            )
          );
          error = Maybe.Some(msg);
        },
        _ => {
          publishPending = false;
          flashMessageStore.add(
            'valvonta-oikeellisuus',
            'success',
            i18n(`${i18nRoot}.messages.publish-success`)
          );
          reload();
        },
        Toimenpiteet.isAnomaly(toimenpide)
          ? ValvontaApi.postAnomaly(id, toimenpide, anomalyMessageLanguage)
          : ValvontaApi.postToimenpide(id, toimenpide)
      );
    } else {
      error = Maybe.Some($_(`${i18nRoot}.messages.validation-error`));
      Validation.blurForm(form);
    }
  };

  $: preview = toimenpide => {
    if (isValidForm(toimenpide)) {
      previewPending = true;
      Future.fork(
        response => {
          previewPending = false;
          const msg = i18n(
            Maybe.orSome(
              `${i18nRoot}.messages.preview-error`,
              Response.localizationKey(response)
            )
          );
          error = Maybe.Some(msg);
        },
        response => {
          previewPending = false;
          error = Maybe.None();
          Response.openBlob(response);
        },
        ValvontaApi.previewToimenpide(id, toimenpide)
      );
    } else {
      error = Maybe.Some($_(`${i18nRoot}.messages.validation-error`));
      Validation.blurForm(form);
    }
  };

  $: disabled = publishPending || previewPending;
</script>

<Dialog
  bind:form
  header={text(toimenpide, 'title')}
  {error}
  buttons={[
    {
      text: text(toimenpide, 'publish-button'),
      'on:click': _ => publish(toimenpide),
      disabled,
      showSpinner: publishPending
    },
    {
      text: i18n(i18nRoot + '.cancel-button'),
      style: 'secondary',
      'on:click': reload,
      disabled
    },
    ...(!R.isEmpty(templates)
      ? [
          {
            text: i18n(i18nRoot + '.preview-button'),
            style: 'secondary',
            'on:click': _ => preview(toimenpide),
            disabled,
            showSpinner: previewPending
          }
        ]
      : [])
  ]}>
  <p>{text(toimenpide, 'info')}</p>

  {#if Toimenpiteet.hasDeadline(toimenpide)}
    <div class="flex py-4">
      <Datepicker
        {disabled}
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
        {disabled}
        label="Valitse asiakirjapohja"
        bind:model={toimenpide}
        lens={R.lensProp('template-id')}
        parse={Maybe.fromNull}
        required={true}
        validation={true}
        format={formatTemplate}
        items={R.pluck('id', templates)} />
    </div>
  {:else if Toimenpiteet.isAnomaly(toimenpide)}
    <div class="w-full py-4">
      <Fieldset legendText={text(toimenpide, 'language-selection')}>
        <Radio
          bind:group={anomalyMessageLanguage}
          value="fi"
          label={text(toimenpide, 'fi')} />
        <Radio
          bind:group={anomalyMessageLanguage}
          value="sv"
          label={text(toimenpide, 'sv')} />
      </Fieldset>
      <Textarea
        {disabled}
        id={'toimenpide.description'}
        name={'toimenpide.description'}
        label={text(toimenpide, `description-${anomalyMessageLanguage}`)}
        bind:model={toimenpide}
        lens={R.lensProp('description')}
        required={false}
        format={Maybe.orSome('')}
        parse={Parsers.optionalString}
        validators={schema.description}
        {i18n} />
    </div>
  {:else}
    <div class="w-full py-4">
      <Textarea
        {disabled}
        id={'toimenpide.description'}
        name={'toimenpide.description'}
        label={text(toimenpide, 'description')}
        bind:model={toimenpide}
        lens={R.lensProp('description')}
        required={false}
        format={Maybe.orSome('')}
        parse={Parsers.optionalString}
        validators={schema.description}
        {i18n} />
    </div>
  {/if}
</Dialog>
