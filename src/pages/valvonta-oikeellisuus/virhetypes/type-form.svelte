<script>
  import * as R from 'ramda';
  import * as Validation from '@Utility/validation';
  import * as Either from '@Utility/either-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Parsers from '@Utility/parsers';
  import { Virhetype as schema } from './schema';

  import { _ } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';

  import TextEditor from '@Component/text-editor/text-editor';
  import Input from '@Component/Input/Input.svelte';
  import Button from '@Component/Button/Button.svelte';
  import Checkbox from '@Component/Checkbox/Checkbox.svelte';

  const i18n = $_;
  const i18nRoot = 'valvonta.oikeellisuus.virhetypes';

  export let virhetype;
  export let api;
  export let dirty;

  const toolbar = [
    [{ script: 'sub' }, { script: 'super' }],
    ['bold', 'italic', 'underline'],
    ['clean']
  ];

  const setDirty = _ => {
    dirty = true;
  };

  let form;
  const save = virhetype => {
    if (Validation.isValidForm(schema)(virhetype)) {
      api.save(virhetype);
    } else {
      flashMessageStore.add(
        'valvonta-oikeellisuus',
        'error',
        i18n(`${i18nRoot}.messages.validation-error`)
      );
      Validation.blurForm(form);
    }
  };

  const canonizeWhitespace = R.replace(/\s+/g, ' ');
  const parseLabel = R.compose(canonizeWhitespace, R.trim);
</script>

<style>
</style>

<form
  id="virhetype-form"
  bind:this={form}
  on:submit|preventDefault={_ => save(virhetype)}
  on:input={setDirty}
  on:change={setDirty}
  on:text-change={setDirty}>
  <div class="flex flex-wrap bg-light text-base m-2 p-2">
    <div class="w-1/6 py-4 px-2">
      <Input
        id={'id'}
        name={'id'}
        label={i18n(i18nRoot + '.id')}
        bind:model={virhetype}
        lens={R.lensProp('id')}
        required={false}
        disabled={true}
        format={Maybe.orSome(i18n(i18nRoot + '.new-virhetype'))}
        {i18n} />
    </div>
    <div class="w-1/6 py-4 px-2">
      <Input
        id={'ordinal'}
        name={'ordinal'}
        label={i18n(i18nRoot + '.ordinal')}
        bind:model={virhetype}
        lens={R.lensProp('ordinal')}
        required={true}
        parse={Parsers.parseInteger}
        {i18n} />
    </div>
    <div class="w-4/6 py-4 px-4 flex justify-end flex-wrap content-center">
      <Checkbox
        disabled={false}
        label={i18n(`${i18nRoot}.valid`)}
        bind:model={virhetype}
        lens={R.lensProp('valid')} />
    </div>
    <div class="lg:w-1/2 w-full py-4 px-2">
      <TextEditor
        id={'label-fi'}
        name={'label-fi'}
        label={i18n(i18nRoot + '.label-fi')}
        {toolbar}
        bind:model={virhetype}
        lens={R.lensProp('label-fi')}
        required={true}
        parse={parseLabel}
        validators={schema['label-fi']}
        {i18n} />
    </div>
    <div class="lg:w-1/2 w-full py-4 px-2">
      <TextEditor
        id={'label-sv'}
        name={'label-sv'}
        label={i18n(i18nRoot + '.label-sv')}
        {toolbar}
        bind:model={virhetype}
        lens={R.lensProp('label-sv')}
        required={true}
        parse={parseLabel}
        validators={schema['label-sv']}
        {i18n} />
    </div>
    <div class="lg:w-1/2 w-full py-4 px-2">
      <TextEditor
        id={'description-fi'}
        name={'description-fi'}
        label={i18n(i18nRoot + '.description-fi')}
        {toolbar}
        bind:model={virhetype}
        lens={R.lensProp('description-fi')}
        required={true}
        parse={R.trim}
        validators={schema['description-fi']}
        {i18n} />
    </div>
    <div class="lg:w-1/2 w-full py-4 px-2">
      <TextEditor
        id={'description-sv'}
        name={'description-sv'}
        label={i18n(i18nRoot + '.description-sv')}
        {toolbar}
        bind:model={virhetype}
        lens={R.lensProp('description-sv')}
        required={true}
        parse={R.trim}
        validators={schema['description-sv']}
        {i18n} />
    </div>
    <div class="flex space-x-4 b-t-1 pt-4">
      <Button
        disabled={!dirty}
        type={'submit'}
        text={i18n(i18nRoot + '.save-button')} />

      <Button
        disabled={!dirty}
        on:click={api.cancel}
        text={i18n(i18nRoot + '.cancel-button')}
        style={'secondary'} />

      <Button
        disabled={dirty}
        on:click={api.close}
        text={i18n(i18nRoot + '.close-button')}
        style={'secondary'} />
    </div>
  </div>
</form>
