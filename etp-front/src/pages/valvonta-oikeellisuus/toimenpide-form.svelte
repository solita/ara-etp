<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Validation from '@Utility/validation';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as Router from '@Component/Router/router';
  import { tick } from 'svelte';

  import * as Toimenpiteet from './toimenpiteet';
  import * as Schema from './schema';

  import { _ } from '@Language/i18n';

  import H1 from '@Component/H/H1';
  import MuistioForm from './muistio-form.svelte';
  import ResponseForm from './response-form.svelte';
  import Button from '@Component/Button/Button.svelte';
  import TextButton from '@Component/Button/TextButton.svelte';
  import Confirm from '@Component/Confirm/Confirm';
  import { announcementsForModule } from '@Utility/announce';

  export let whoami;
  export let toimenpide;
  export let templatesByType;
  export let severities;
  export let virhetyypit;
  export let dirty;
  export let submit;
  export let cancel;
  export let publish = Maybe.None();
  export let preview = Maybe.None();
  export let deleteDraft = Maybe.None();
  export let liiteApi;
  export let liitteet;

  const i18nRoot = 'valvonta.oikeellisuus.toimenpide';
  const i18n = $_;
  const text = R.compose($_, Toimenpiteet.i18nKey);
  const { announceError } = announcementsForModule('valvonta-oikeellisuus');

  const forms = {
    'audit-report': MuistioForm,
    'rfc-reply': ResponseForm,
    'rfi-reply': ResponseForm,
    'audit-reply': ResponseForm
  };
  const Content = forms[Toimenpiteet.typeKey(toimenpide['type-id'])];

  const setDirty = _ => {
    dirty = true;
  };

  $: templates = Toimenpiteet.templates(templatesByType)(toimenpide);
  let form;
  let schema = Schema.toimenpideSave;

  const submitToimenpide = submit => {
    if (Validation.isValidForm(schema)(toimenpide)) {
      submit(toimenpide);
    } else {
      announceError(i18n(`${i18nRoot}.messages.validation-error`));
      Validation.blurFormExcludeNested(form);
    }
  };

  const previewToimenpide = _ => R.forEach(submitToimenpide, preview);

  const publishToimenpide = _ =>
    R.forEach(fn => {
      schema = Schema.toimenpidePublish(templates, toimenpide);
      tick().then(_ => submitToimenpide(fn));
    }, publish);

  const saveToimenpide = _ => submitToimenpide(submit);
  const deleteToimenpide = _ =>
    R.forEach(deleteDraft => deleteDraft(), deleteDraft);
</script>

<H1 text={text(toimenpide, 'title')} />

<form
  bind:this={form}
  on:submit|preventDefault={saveToimenpide}
  on:input={setDirty}
  on:change={setDirty}
  on:text-change={setDirty}>
  <Content
    bind:toimenpide
    bind:dirty
    {schema}
    {templates}
    {severities}
    {virhetyypit}
    {liiteApi}
    {liitteet}
    disabled={!Toimenpiteet.isDraft(toimenpide)} />

  <div class="flex space-x-4 pt-8">
    <Button
      disabled={Maybe.isNone(publish) || !Toimenpiteet.isDraft(toimenpide)}
      on:click={publishToimenpide}
      text={text(toimenpide, 'publish-button')} />

    <Button
      disabled={!Toimenpiteet.isDraft(toimenpide) ||
        (!dirty && !R.isNil(toimenpide.id))}
      type={'submit'}
      text={text(toimenpide, 'save-button')} />

    <Button
      disabled={!Toimenpiteet.isDraft(toimenpide) || !dirty}
      on:click={cancel}
      text={text(toimenpide, 'reset-button')}
      style={'secondary'} />

    {#if Kayttajat.isPaakayttaja(whoami) && Toimenpiteet.isDraft(toimenpide) && Maybe.isSome(deleteDraft)}
      <Confirm
        let:confirm
        confirmButtonLabel={i18n('confirm.button.delete')}
        confirmMessage={i18n('confirm.you-want-to-delete')}>
        <Button
          on:click={confirm(deleteToimenpide)}
          text={text(toimenpide, 'delete-button')}
          style={'secondary'} />
      </Confirm>
    {/if}

    {#if !R.isEmpty(templates) && Maybe.isSome(preview)}
      <Button
        disabled={!Toimenpiteet.hasTemplate(toimenpide)}
        on:click={previewToimenpide}
        text={!Toimenpiteet.isDraft(toimenpide)
          ? i18n(i18nRoot + '.download-button')
          : i18n(i18nRoot + '.preview-button')} />
    {/if}
  </div>
</form>

<div class="mt-5">
  <TextButton
    text={i18n(i18nRoot + '.back')}
    icon="arrow_back"
    on:click={_ => Router.pop()} />

  {#if Toimenpiteet.isDraft(toimenpide) && !R.isNil(toimenpide.id)}
    <p class="mt-2">{text(toimenpide, 'draft-info')}</p>
  {/if}
</div>
