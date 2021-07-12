<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Validation from '@Utility/validation';
  import { _ } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';
  import { push } from 'svelte-spa-router';

  import * as api from '@Pages/ohje/ohje-api';
  import * as Schema from '@Pages/ohje/schema';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';
  import Input from '@Component/Input/Input.svelte';
  import TextEditor from '@Component/text-editor/text-editor.svelte';
  import Checkbox from '@Component/Checkbox/Checkbox';
  import Button from '@Component/Button/Button';
  import Link from '@Component/Link/Link';
  import Confirm from '@Component/Confirm/Confirm';
  import Navigation from '@Pages/ohje/navigation';

  const i18n = $_;

  export let params;

  let dirty = false;
  let overlay = true;
  let enableOverlay = _ => {
    overlay = true;
  };
  let sivu;
  let sivut;
  let isParent = false;
  $: load(params.id);

  const load = id => {
    overlay = true;
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            'ohje.editor.load-error',
            Response.localizationKey(response)
          )
        );

        flashMessageStore.add('ohje', 'error', msg);
        overlay = false;
      },
      response => {
        sivu = response.sivu;
        sivut = response.sivut;
        isParent =
          R.findIndex(
            R.compose(Maybe.exists(R.equals(sivu['id'])), R.prop('parent-id'))
          )(sivut) > -1;

        overlay = false;
      },
      Future.parallelObject(2, {
        sivu: api.getSivu(id),
        sivut: api.getSivut
      })
    );
  };

  const updateOhje = R.compose(
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome('ohje.editor.error', Response.localizationKey(response))
        );
        flashMessageStore.add('ohje', 'error', msg);
        overlay = false;
      },
      _ => {
        dirty = false;
        overlay = false;
        push(`/ohje/${params.id}`);
      }
    ),
    R.tap(enableOverlay),
    api.putSivu(fetch, params.id)
  );
  const deleteOhje = R.compose(
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            'ohje.editor.delete-error',
            Response.localizationKey(response)
          )
        );
        flashMessageStore.add('ohje', 'error', msg);
        overlay = false;
      },
      _ => {
        dirty = false;
        overlay = false;
        push('/ohje/deleted');
      }
    ),
    R.tap(enableOverlay),
    api.deleteSivu(fetch, params.id)
  );

  const isValidForm = Validation.isValidForm(Schema.sivu);

  const submitOhje = event => {
    if (isValidForm(sivu)) {
      updateOhje(sivu);
    } else {
      flashMessageStore.add(
        'ohje',
        'error',
        i18n('ohje.editor.validation-error')
      );
      Validation.blurForm(event.target);
    }
  };
</script>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3 flex space-x-4">
    <div class="w-2/6 max-w-xs">
      <Navigation
        id={params.id}
        sortDisabled={true}
        sortButtonTitle={i18n('ohje.editor.sort-disabled')} />
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
              <div class="flex justify-between items-end">
                <div class="flex-grow mr-4">
                  <Input
                    id={'ohje.title'}
                    name={'ohje.title'}
                    label={i18n('ohje.editor.title')}
                    required={true}
                    bind:model={sivu}
                    lens={R.lensProp('title')}
                    parse={R.trim}
                    validators={Schema.sivu.title}
                    {i18n} />
                </div>

                <div class="mt-auto font-semibold">
                  <Link
                    href={`/#/ohje/${sivu.id}`}
                    icon={Maybe.Some('cancel')}
                    text={i18n('ohje.editor.cancel')} />
                </div>
              </div>

              <div class="w-full py-4">
                <TextEditor
                  id={'ohje.body'}
                  name={'ohje.body'}
                  label={i18n('ohje.editor.body')}
                  bind:model={sivu}
                  lens={R.lensProp('body')}
                  required={true}
                  parse={R.trim}
                  validators={Schema.sivu.body}
                  {i18n} />
              </div>

              <div class="flex space-x-4 pt-8">
                <Checkbox
                  id={'ohje.published'}
                  name={'ohje.published'}
                  label={i18n('ohje.editor.published')}
                  bind:model={sivu}
                  lens={R.lensProp('published')}
                  required={true}
                  disabled={false} />
              </div>
              <div class="flex space-x-4 pt-8">
                <Button
                  disabled={!dirty}
                  type={'submit'}
                  text={i18n('ohje.editor.submit')} />
                <Confirm
                  let:confirm
                  confirmButtonLabel={i18n('confirm.button.delete')}
                  confirmMessage={i18n('confirm.you-want-to-delete')}>
                  <Button
                    on:click={() => {
                      confirm(deleteOhje);
                    }}
                    disabled={isParent}
                    title={isParent ? i18n('ohje.editor.delete-disabled') : ''}
                    text={i18n('ohje.editor.delete')}
                    style={'error'} />
                </Confirm>
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
