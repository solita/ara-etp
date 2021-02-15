<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Validation from '@Utility/validation';

  import * as api from './viesti-api';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';
  import * as Viestit from './viesti-util';
  import * as Schema from './schema';

  import { flashMessageStore } from '@/stores';
  import { _ } from '@Language/i18n';
  import { pop } from '@Component/Router/router';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import H1 from '@Component/H/H1.svelte';
  import Button from '@Component/Button/Button.svelte';
  import Textarea from '@Component/Textarea/Textarea.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import Input from '@Component/Input/Input.svelte';
  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';

  const i18nRoot = 'viesti.ketju.new';

  let resources = Maybe.None();
  let dirty = false;
  let overlay = true;
  let enableOverlay = _ => {
    overlay = true;
  };

  Future.fork(
    response => {
      const msg = $_(
        Maybe.orSome(
          `${i18nRoot}.messages.load-error`,
          Response.localizationKey(response)
        )
      );

      flashMessageStore.add('viesti', 'error', msg);
      overlay = false;
    },
    response => {
      resources = Maybe.Some({
        whoami: response
      });
      overlay = false;
    },
    kayttajaApi.whoami
  );

  let ketju = Viestit.emptyKetju();

  const addKetju = R.compose(
    Future.fork(
      response => {
        const msg = $_(
          Maybe.orSome(
            `${i18nRoot}.messages.error`,
            Response.localizationKey(response)
          )
        );
        flashMessageStore.add('viesti', 'error', msg);
        overlay = false;
      },
      _ => {
        flashMessageStore.add(
          'viesti',
          'success',
          $_(`${i18nRoot}.messages.success`)
        );
        dirty = false;
        pop();
      }
    ),
    api.postKetju(fetch)
  );

  const isValidForm = Validation.isValidForm(Schema.ketju);

  const submitNewKetju = event => {
    if (isValidForm(ketju)) {
      addKetju(ketju);
    } else {
      flashMessageStore.add(
        'viesti',
        'error',
        $_(`${i18nRoot}.messages.validation-error`)
      );
      Validation.blurForm(event.target);
    }
  };

  const cancel = _ => {
    ketju = Viestit.emptyKetju();
    dirty = false;
  };
</script>

<style>
</style>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    <H1 text={$_(`${i18nRoot}.title`)} />
    <DirtyConfirmation {dirty} />
    {#each resources.toArray() as { whoami }}
      <form
        id="ketju"
        on:submit|preventDefault={submitNewKetju}
        on:input={_ => {
          dirty = true;
        }}
        on:change={_ => {
          dirty = true;
        }}>
        <div class="w-full py-4">
          <Input
            id={'ketju.subject'}
            name={'ketju.subject'}
            label={$_('viesti.ketju.subject')}
            required={true}
            bind:model={ketju}
            lens={R.lensProp('subject')}
            parse={R.trim}
            validators={Schema.ketju.subject}
            i18n={$_} />
        </div>
        <div class="w-full py-4">
          <Textarea
            id={'ketju.body'}
            name={'ketju.body'}
            label={$_('viesti.ketju.body')}
            bind:model={ketju}
            lens={R.lensProp('body')}
            required={true}
            parse={R.trim}
            validators={Schema.ketju.body}
            i18n={$_} />
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
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
