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
  import Select from '@Component/Select/Select';
  import OsapuoletTable from './toimenpide-osapuolet-table.svelte';

  import * as Validation from '@Utility/validation';

  import DecisionOrderActualDecisionSubView from './new-decision-order-actual-decision-toimenpide';
  import PenaltyDecisionActualDecisionSubView from './new-penalty-decision-actual-decision-toimenpide';
  import { announcementsForModule } from '@Utility/announce';
  import { announceAssertively } from '@Utility/aria-live';

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.toimenpide';
  const { announceSuccess } = announcementsForModule('valvonta-kaytto');

  export let id;
  export let templatesByType;
  export let toimenpide;
  export let reload;

  // Depending on the toimenpidetype of the toimenpide we are creating here, these can
  // contain either all the osapuolet or only the owners. Filtering is done outside of this view
  export let henkilot;
  export let yritykset;

  export let roolit;
  export let toimitustavat;
  export let hallintoOikeudet;

  export let karajaoikeudet;

  export let manuallyDeliverableToimenpide = false;
  export let commentingAllowed = false;

  let form;
  let error = Maybe.None();

  // setError is used instead of the more common announceError from
  // @Utility/announce, because the visual error needs a mechanism
  // different from the common route through FlashMessage
  const setError = er => {
    error = er;
    R.forEach(announceAssertively, R.map(R.prop('message'), error));
  };

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
          setError(
            Maybe.Some({
              mayBypassAsha:
                Toimenpiteet.isCloseCase(toimenpide) &&
                ValvontaApi.isAshaFailure(response),
              message: i18n(Response.errorKey(i18nRoot, 'publish', response))
            })
          );
          publishPending = false;
        },
        _ => {
          announceSuccess(i18n(`${i18nRoot}.messages.publish-success`));
          publishPending = false;
          reload();
        },
        ValvontaApi.postToimenpide(id, toimenpide)
      );
    } else {
      setError(
        Maybe.Some({
          mayBypassAsha: false,
          message: $_(`${i18nRoot}.messages.validation-error`)
        })
      );
      Validation.blurForm(form);
    }
  };

  $: preview = (api, previewToimenpide = toimenpide) => {
    // If validating only partial data, we need to create
    // the schema and validation function based on that data
    const previewSchema = Schema.toimenpidePublish(
      templates,
      previewToimenpide
    );
    const isValidPreview = Validation.isValidForm(previewSchema);
    if (isValidPreview(previewToimenpide)) {
      previewPending = true;
      Future.fork(
        response => {
          setError(
            Maybe.Some({
              message: i18n(Response.errorKey(i18nRoot, 'preview', response)),
              mayBypassAsha: false
            })
          );
          previewPending = false;
        },
        response => {
          previewPending = false;
          setError(Maybe.None());
          Response.openBlob(response);
        },
        api
      );
    } else {
      setError(
        Maybe.Some({
          message: $_(`${i18nRoot}.messages.validation-error`),
          mayBypassAsha: false
        })
      );
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
      prefix: 'submit',
      showSpinner: publishPending,
      disabled,
      'on:click': _ => publish({ 'bypass-asha': false, ...toimenpide })
    },
    {
      text: i18n(i18nRoot + '.cancel-button'),
      prefix: 'cancel',
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
        name="document-selector"
        bind:model={toimenpide}
        lens={R.lensProp('template-id')}
        parse={Maybe.fromNull}
        required={true}
        validation={true}
        format={formatTemplate}
        items={R.pluck('id', filterValid(templates))} />
    </div>
  {/if}

  {#if commentingAllowed}
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
  {#if Toimenpiteet.isDecisionOrderActualDecision(toimenpide)}
    <DecisionOrderActualDecisionSubView
      bind:toimenpide
      {id}
      {preview}
      {previewPending}
      {disabled}
      error={R.map(R.prop('message'), error)}
      {i18n}
      {text}
      {schema}
      {hallintoOikeudet}
      henkiloOmistajat={henkilot}
      yritysOmistajat={yritykset} />
  {:else if Toimenpiteet.isPenaltyDecisionActualDecision(toimenpide)}
    <PenaltyDecisionActualDecisionSubView
      bind:toimenpide
      {id}
      {preview}
      {previewPending}
      {disabled}
      error={R.map(R.prop('message'), error)}
      {i18n}
      {text}
      {schema}
      {hallintoOikeudet}
      henkiloOmistajat={henkilot}
      yritysOmistajat={yritykset} />
  {/if}

  {#if !R.isEmpty(templates) && !Toimenpiteet.isDecisionOrderActualDecision(toimenpide) && !Toimenpiteet.isPenaltyDecisionActualDecision(toimenpide)}
    <div class="mt-2">
      <OsapuoletTable
        {id}
        bind:toimenpide
        {henkilot}
        {yritykset}
        {preview}
        {previewPending}
        {disabled}
        {roolit}
        {toimitustavat}
        {template}
        {schema}
        {karajaoikeudet}
        {hallintoOikeudet}
        {manuallyDeliverableToimenpide}
        showDeliveryMethod={!Toimenpiteet.isNoticeBailiff(toimenpide)}
        showHallintoOikeudetSelection={Toimenpiteet.hasCourtAttachment(
          toimenpide
        )}
        showKarajaOikeudetSelection={Toimenpiteet.isNoticeBailiff(toimenpide)}
        showCreateDocument={Toimenpiteet.hasOptionalDocument(toimenpide)}
        allowPreviewAlways={!Toimenpiteet.hasOptionalDocument(toimenpide)} />
    </div>
  {/if}
</Dialog>
