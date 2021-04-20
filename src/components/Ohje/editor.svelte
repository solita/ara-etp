<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';
  import * as api from './ohje-api';
  import * as Parsers from '@Utility/parsers';
  import * as Validation from '@Utility/validation';
  import { _, locale } from '@Language/i18n';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';
  import Input from '@Component/Input/Input.svelte';
  import Textarea from '@Component/Textarea/Textarea.svelte';
  import TextEditor from '@Component/text-editor/text-editor.svelte';
  import Checkbox from '@Component/Checkbox/Checkbox';
  import Button from '@Component/Button/Button';

  export let params;

  const emptySivu = { public: false, title: '', body: '' };
  let checked = false;

  const i18nRoot = 'ohje.editor';
  let sivu = emptySivu;

  let dirty = false;
  let overlay = true;
  let enableOverlay = _ => {
    overlay = true;
  };

  const cancel = _ => {
    sivu = emptySivu;
    dirty = false;
  };

  $: console.log('params', params);

  let resources = Maybe.None();
  $: id = params.id;

  $: {
    overlay = true;
    Future.fork(
      response => {
        const msg = $_(
          Maybe.orSome(
            'viesti.all.messages.load-error',
            Response.localizationKey(response)
          )
        );

        flashMessageStore.add('viesti', 'error', msg);
        overlay = false;
      },
      response => {
        resources = Maybe.Some(response);
        overlay = false;
      },
      Future.parallelObject(2, {
        whoami: kayttajaApi.whoami,
        sivu: api.getSivu(id)
      })
    );
  }
</script>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    {#each Maybe.toArray(resources) as { sivu, whoami }}
      <div class="w-full flex flex-col">
        <DirtyConfirmation {dirty} />
        <div class="w-full flex flex-col">
          <form>
            <div class="w-full py-4">
              <!-- <Input
            id={'ohje.title'}
            name={'ohje.title'}
            label={'TITLE'}
            required={true}
            bind:model={sivu}
            lens={R.compose(R.lensProp('title'), arrayHeadLens)}
            parse={Parsers.optionalParser(parseVastaanottaja(laatijat))}
            i18n={$_} /> -->
            </div>

            <div class="w-full py-4">
              <!-- <Textarea
            id={'ketju.body'}
            name={'ketju.body'}
            label={$_('ohje.ketju.body')}
            bind:model={sivu}
            lens={R.lensProp('body')}
            required={true}
            parse={R.trim}
            validators={}
            i18n={$_} /> -->
              <TextEditor />
            </div>

            <div class="flex space-x-4 pt-8">
              <Checkbox
                label={$_('ohje.julkaistu')}
                bind:model={checked}
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
              <Button
                disabled={!dirty}
                on:click={cancel}
                text={$_(`${i18nRoot}.delete`)}
                style={'error'} />
            </div>
          </form>
        </div>
      </div>
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
