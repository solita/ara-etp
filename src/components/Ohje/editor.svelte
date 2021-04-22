<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Validation from '@Utility/validation';
  import { _ } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';
  import { push } from 'svelte-spa-router';

  import * as api from './ohje-api';
  import * as Schema from './schema';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';
  import Input from '@Component/Input/Input.svelte';
  import TextEditor from '@Component/text-editor/text-editor.svelte';
  import Checkbox from '@Component/Checkbox/Checkbox';
  import Button from '@Component/Button/Button';
  import Navigation from './navigation';

  const i18nRoot = 'ohje.editor';

  export let params;

  let dirty = false;
  let overlay = true;
  let enableOverlay = _ => {
    overlay = true;
  };
  let sivu;
  $: load(params.id);

  const load = id => {
    overlay = true;
    Future.fork(
      response => {
        const msg = $_(
          Maybe.orSome(
            `${i18nRoot}.load-error`,
            Response.localizationKey(response)
          )
        );

        flashMessageStore.add('ohje', 'error', msg);
        overlay = false;
      },
      response => {
        sivu = response;
        overlay = false;
      },
      api.getSivu(id)
    );
  };

  const updateOhje = R.compose(
    Future.fork(
      response => {
        const msg = $_(
          Maybe.orSome(`${i18nRoot}.error`, Response.localizationKey(response))
        );
        flashMessageStore.add('ohje', 'error', msg);
        overlay = false;
      },
      _ => {
        flashMessageStore.add('ohje', 'success', $_(`${i18nRoot}.success`));
        dirty = false;
        overlay = false;
        push(`/ohje/${params.id}`);
      }
    ),
    R.tap(enableOverlay),
    api.putSivu(fetch, params.id)
  );

  const isValidForm = Validation.isValidForm(Schema.sivu);

  const deleteOhje = () => {
    alert('delete ohje');
  };

  const submitOhje = event => {
    if (isValidForm(sivu)) {
      updateOhje(sivu);
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

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3 flex space-x-4">
    <div class="w-2/6 max-w-xs">
      <Navigation />
    </div>
    <div class="w-4/6 flex-grow">
      {#if sivu}
        <div class="w-full flex flex-col">
          <DirtyConfirmation {dirty} />
          <div class="w-full flex flex-col">
            <form
              id="ohje"
              on:submit|preventDefault={submitOhje(sivu)}
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
                  bind:model={sivu}
                  lens={R.lensProp('body')}
                  required={true}
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
                on:click={deleteOhje}
                text={$_(`${i18nRoot}.delete`)}
                style={'error'} /> -->
              </div>
            </form>
          </div>
        </div>
      {/if}
    </div>
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
