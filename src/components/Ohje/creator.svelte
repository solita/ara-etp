<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Validation from '@Utility/validation';
  import { pop } from '@Component/Router/router';
  import { _ } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';
  import { push } from '@Component/Router/router';

  import * as api from './ohje-api';
  import * as Schema from './schema';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';
  import Input from '@Component/Input/Input.svelte';
  import TextEditor from '@Component/text-editor/text-editor.svelte';
  import Checkbox from '@Component/Checkbox/Checkbox';
  import Button from '@Component/Button/Button';
  import TextButton from '@Component/Button/TextButton';
  import Navigation from './navigation';

  const emptySivu = {
    published: false,
    title: '',
    body: '',
    'parent-id': Maybe.None(),
    ordinal: 0
  };

  const i18nRoot = 'ohje.creator';
  let sivu = emptySivu;

  let dirty = false;
  let overlay = false;
  let createSuccess = false;
  let enableOverlay = _ => {
    overlay = true;
  };

  const cancel = _ => {
    sivu = emptySivu;
    dirty = false;
  };

  const addOhje = R.compose(
    Future.fork(
      response => {
        const msg = $_(
          Maybe.orSome(`${i18nRoot}.error`, Response.localizationKey(response))
        );
        flashMessageStore.add('ohje', 'error', msg);
        overlay = false;
      },
      response => {
        flashMessageStore.add('ohje', 'success', $_(`${i18nRoot}.success`));
        dirty = false;
        createSuccess = true;
        if (R.has('id', response)) {
          setTimeout(() => {
            push(`/ohje/${response.id}`);
          }, 500);
        } else {
          overlay = false;
        }
      }
    ),
    R.tap(enableOverlay),
    api.postSivu(fetch)
  );

  const isValidForm = Validation.isValidForm(Schema.sivu);

  const submitOhje = event => {
    if (isValidForm(sivu)) {
      addOhje(sivu);
    } else {
      flashMessageStore.add(
        'ohje',
        'error',
        $_(`${i18nRoot}.validation-error`)
      );
      Validation.blurForm(event.target);
    }
  };
</script>

<style>
  .success-icon {
    font-size: 54px;
  }
</style>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3 flex space-x-4">
    <!-- <div class="w-2/6 max-w-xs">
      <Navigation />
    </div>
    <div class="w-4/6 flex-grow flex flex-col"> -->
    <div class="w-full flex flex-col">
      <DirtyConfirmation {dirty} />
      <div class="w-full flex flex-col">
        <div class="mr-auto">
          <TextButton
            on:click={pop}
            icon="arrow_back"
            text={$_(`${i18nRoot}.back`)}
            style={'secondary'} />
        </div>
        <form
          id="ohje"
          on:submit|preventDefault={submitOhje}
          on:input={_ => {
            dirty = true;
          }}
          on:change={_ => {
            dirty = true;
          }}>
          <div class="w-full py-4">
            <Input
              id={'ohje.title'}
              name={'ohje.title'}
              label={$_(`${i18nRoot}.title`)}
              required={true}
              bind:model={sivu}
              lens={R.lensProp('title')}
              parse={R.trim}
              validators={Schema.sivu.title}
              i18n={$_} />
          </div>

          <div class="w-full py-4">
            <TextEditor
              id={'ohje.body'}
              name={'ohje.body'}
              label={$_(`${i18nRoot}.body`)}
              required={true}
              bind:model={sivu}
              lens={R.lensProp('body')}
              parse={R.trim}
              validators={Schema.sivu.body}
              i18n={$_} />
          </div>

          <div class="flex space-x-4 pt-8">
            <Checkbox
              id={'ohje.published'}
              name={'ohje.published'}
              label={$_(`${i18nRoot}.published`)}
              bind:model={sivu}
              lens={R.lensProp('published')}
              required={true}
              disabled={false} />
          </div>
          <div class="flex space-x-4 pt-8">
            <Button
              disabled={!dirty}
              type={'submit'}
              text={$_(`${i18nRoot}.submit`)} />
            <!-- <Button
              disabled={!dirty}
              on:click={cancel}
              text={$_(`${i18nRoot}.reset`)}
              type={'reset'}
              style={'secondary'} /> -->
          </div>
        </form>
      </div>
    </div>
  </div>
  <div slot="overlay-content">
    {#if createSuccess}
      <span class="material-icons text-primary success-icon">
        check_circle
      </span>
    {:else}
      <Spinner />
    {/if}
  </div>
</Overlay>
