<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Parsers from '@Utility/parsers';
  import * as Formats from '@Utility/formats';
  import { filterValid } from '@Utility/classification';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Locales from '@Language/locale-utils';

  import * as Toimenpiteet from './toimenpiteet';
  import * as Schema from './schema';

  import * as ValvontaApi from './valvonta-api';

  import { _, locale } from '@Language/i18n';

  import Dialog from '@Component/dialog/dialog';
  import Textarea from '@Component/Textarea/Textarea';
  import Datepicker from '@Component/Input/Datepicker';
  import Input from '@Component/Input/Input';
  import { flashMessageStore } from '@/stores';
  import Select from '@Component/Select/Select';
  import OsapuoletTable from './toimenpide-osapuolet-table.svelte';
  import * as Validation from '@Utility/validation';

  import ActualDecisionSubView from './new-actual-decision-toimenpide';

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.toimenpide';

  export let id;
  export let templatesByType;
  export let toimenpide;
  export let reload;
  export let henkilot;
  export let yritykset;
  export let roolit;
  export let toimitustavat;
  export let hallintoOikeudet;

  export let manuallyDeliverableToimenpide = false;
  export let commentingAllowed = false;

  let form;
  let error = Maybe.None();
  let osapuolet = R.concat(henkilot, yritykset);
  let publishPending = false;
  let previewPending = false;

  $: disabled = publishPending || previewPending;

  const text = R.compose(i18n, Toimenpiteet.i18nKey);

  let templates;
  $: templates = Toimenpiteet.templates(templatesByType)(toimenpide);
  $: formatTemplate = Locales.labelForId($locale, templates);

  $: schema = Schema.toimenpidePublish(templates, toimenpide);
  $: isValidForm = Validation.isValidForm(schema);

  $: template = R.compose(
    R.chain(Maybe.findById(R.__, templates)),
    R.prop('template-id')
  )(toimenpide);

  $: publish = toimenpide => {
    if (isValidForm(toimenpide)) {
      publishPending = true;
      Future.fork(
        response => {
          error = Maybe.Some({
            mayBypassAsha:
              Toimenpiteet.isCloseCase(toimenpide) &&
              ValvontaApi.isAshaFailure(response),
            message: i18n(Response.errorKey(i18nRoot, 'publish', response))
          });
          publishPending = false;
        },
        _ => {
          flashMessageStore.add(
            'valvonta-kaytto',
            'success',
            i18n(`${i18nRoot}.messages.publish-success`)
          );
          publishPending = false;
          reload();
        },
        ValvontaApi.postToimenpide(id, toimenpide)
      );
    } else {
      error = Maybe.Some({
        mayBypassAsha: false,
        message: $_(`${i18nRoot}.messages.validation-error`)
      });
      Validation.blurForm(form);
    }
  };

  $: preview = api => {
    if (isValidForm(toimenpide)) {
      previewPending = true;
      Future.fork(
        response => {
          error = Maybe.Some({
            message: i18n(Response.errorKey(i18nRoot, 'preview', response)),
            mayBypassAsha: false
          });
          previewPending = false;
        },
        response => {
          previewPending = false;
          error = Maybe.None();
          Response.openBlob(response);
        },
        api
      );
    } else {
      error = Maybe.Some({
        message: $_(`${i18nRoot}.messages.validation-error`),
        mayBypassAsha: false
      });
      Validation.blurForm(form);
    }
  };
</script>

<Dialog
  bind:form
  header={text(toimenpide, 'title')}
  error={R.map(R.prop('message'), error)}
  buttons={[
    {
      text: text(toimenpide, 'publish-button'),
      showSpinner: publishPending,
      disabled,
      'on:click': _ => publish({ 'bypass-asha': false, ...toimenpide })
    },
    {
      text: i18n(i18nRoot + '.cancel-button'),
      disabled,
      style: 'secondary',
      'on:click': reload
    },
    ...(Maybe.exists(e => e.mayBypassAsha, error)
      ? [
          {
            text: text(toimenpide, 'force-button'),
            showSpinner: publishPending,
            disabled,
            'on:click': _ => publish({ 'bypass-asha': true, ...toimenpide })
          }
        ]
      : [])
  ]}>
  <p>{text(toimenpide, 'info')}</p>

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
        items={R.pluck('id', filterValid(templates))} />
    </div>
  {/if}

  {#if commentingAllowed || R.isEmpty(templates)}
    <div class="w-full py-4">
      <Textarea
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

  {#if Toimenpiteet.hasFine(toimenpide)}
    <div class="w-full py-4">
      <Input
        id="toimenpide.fine"
        name="toimenpide.fine"
        label={text(toimenpide, 'fine')}
        bind:model={toimenpide}
        lens={R.lensPath(['type-specific-data', 'fine'])}
        required={true}
        type="number"
        format={Maybe.orSome('')}
        parse={R.compose(Either.toMaybe, Parsers.parseNumber)}
        validators={R.path(['type-specific-data', 'fine'], schema)}
        {i18n} />
    </div>
  {/if}

  {#if Toimenpiteet.isActualDecision(toimenpide)}
    <ActualDecisionSubView
      bind:toimenpide
      {i18n}
      {text}
      {schema}
      {hallintoOikeudet} />
  {/if}

  {#if !R.isEmpty(templates)}
    <div class="mt-2">
      <OsapuoletTable
        {id}
        {toimenpide}
        {henkilot}
        {yritykset}
        {preview}
        {previewPending}
        {disabled}
        {roolit}
        {toimitustavat}
        {template}
        {manuallyDeliverableToimenpide} />
    </div>
  {/if}
</Dialog>
