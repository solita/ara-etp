<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Validation from '@Utility/validation';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as Parsers from '@Utility/parsers';
  import { _, locale } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';

  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';
  import * as api from './ohje-api';
  import * as Schema from './schema';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';
  import Input from '@Component/Input/Input.svelte';
  import TextEditor from '@Component/text-editor/text-editor.svelte';
  import Checkbox from '@Component/Checkbox/Checkbox';
  import Button from '@Component/Button/Button';

  const emptySivu = {
    published: false,
    title: '',
    body: '',
    'parent-id': Maybe.None(),
    ordinal: 0
  };

  const i18nRoot = 'ohje.editor';
  let sivu = emptySivu;

  let dirty = false;
  let overlay = false;
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
          Maybe.orSome(
            `${i18nRoot}.messages.error`,
            Response.localizationKey(response)
          )
        );
        flashMessageStore.add('ohje', 'error', msg);
        overlay = false;
      },
      _ => {
        flashMessageStore.add(
          'ohje',
          'success',
          $_(`${i18nRoot}.create.success`)
        );
        dirty = false;
        overlay = false;
      }
    ),
    R.tap(enableOverlay),
    api.postOhje(fetch)
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

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    <div class="w-full flex flex-col">
      <DirtyConfirmation {dirty} />
      <div class="w-full flex flex-col">
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
              label={$_('ohje.title')}
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
              label={$_('ohje.body')}
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
              label={$_('ohje.published')}
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
            <Button
              disabled={!dirty}
              on:click={cancel}
              text={$_(`${i18nRoot}.reset`)}
              type={'reset'}
              style={'secondary'} />
          </div>
        </form>
      </div>
    </div>
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
