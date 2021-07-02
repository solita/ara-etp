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

  import * as api from '@Component/ohje/ohje-api';
  import * as Schema from '@Component/ohje/schema';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';
  import Input from '@Component/Input/Input.svelte';
  import TextEditor from '@Component/text-editor/text-editor.svelte';
  import Checkbox from '@Component/Checkbox/Checkbox';
  import Button from '@Component/Button/Button';
  import TextButton from '@Component/Button/TextButton';

  const emptySivu = {
    published: false,
    title: '',
    body: '',
    'parent-id': Maybe.None(),
    ordinal: null
  };

  const i18n = $_;
  let sivu = emptySivu;

  let dirty = false;
  let overlay = false;
  let enableOverlay = _ => {
    overlay = true;
  };

  const addOhje = R.compose(
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(`ohje.creator.error`, Response.localizationKey(response))
        );
        flashMessageStore.add('ohje', 'error', msg);
        overlay = false;
      },
      response => {
        dirty = false;
        overlay = false;
        push(`/ohje/${response.id}`);
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
        i18n(`ohje.creator.validation-error`)
      );
      Validation.blurForm(event.target);
    }
  };
</script>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3 flex space-x-4">
    <div class="w-full flex flex-col">
      <DirtyConfirmation {dirty} />
      <div class="w-full flex flex-col">
        <div class="mr-auto">
          <TextButton
            on:click={pop}
            icon="arrow_back"
            text={i18n('ohje.creator.back')}
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
              label={i18n('ohje.creator.title')}
              required={true}
              bind:model={sivu}
              lens={R.lensProp('title')}
              parse={R.trim}
              validators={Schema.sivu.title}
              {i18n} />
          </div>

          <div class="w-full py-4">
            <TextEditor
              id={'ohje.body'}
              name={'ohje.body'}
              label={i18n('ohje.creator.body')}
              required={true}
              bind:model={sivu}
              lens={R.lensProp('body')}
              parse={R.trim}
              validators={Schema.sivu.body}
              {i18n} />
          </div>

          <div class="flex space-x-4 pt-8">
            <Checkbox
              id={'ohje.published'}
              name={'ohje.published'}
              label={i18n('ohje.creator.published')}
              bind:model={sivu}
              lens={R.lensProp('published')}
              required={true}
              disabled={false} />
          </div>
          <div class="flex space-x-4 pt-8">
            <Button
              disabled={!dirty}
              type={'submit'}
              text={i18n('ohje.creator.submit')} />
          </div>
        </form>
      </div>
    </div>
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
