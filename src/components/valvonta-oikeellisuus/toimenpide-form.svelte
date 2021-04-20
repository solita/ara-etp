<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Parsers from '@Utility/parsers';
  import * as Formats from '@Utility/formats';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Toimenpiteet from './toimenpiteet';
  import * as Validation from '@Utility/validation';
  import * as Router from '@Component/Router/router';

  import { _ } from '@Language/i18n';
  import H1 from '@Component/H/H1';

  import MuistioForm from './muistio-form.svelte';
  import LisatietopyyntoForm from './lisatietopyynto-form.svelte';
  import Button from '../Button/Button.svelte';
  import { flashMessageStore } from '@/stores';
  import TextButton from '../Button/TextButton.svelte';

  export let toimenpide;
  export let dirty;
  export let submit;
  export let cancel;
  export let publish = Maybe.None();

  const i18nRoot = 'valvonta.oikeellisuus.toimenpide';

  const text = R.compose($_, Toimenpiteet.i18nKey);

  const forms = {
    'audit-report': MuistioForm,
    'rfi-extra': LisatietopyyntoForm
  };
  const Content = forms[Toimenpiteet.typeKey(toimenpide['type-id'])];

  const setDirty = _ => {
    dirty = true;
  };

  const isValidForm = Validation.isValidForm({});

  const submitToimenpide = submit => event => {
    if (isValidForm(toimenpide)) {
      submit(toimenpide);
    } else {
      flashMessageStore.add(
        'valvonta-oikeellisuus',
        'error',
        $_(`${i18nRoot}.messages.validation-error`)
      );
      Validation.blurForm(event.target);
    }
  };

  const publishToimenpide = Maybe.fold(_ => {}, submitToimenpide, publish);
</script>

<H1 text={text(toimenpide, 'title')}/>

<TextButton text="Takaisin valvontaan"
            icon="arrow_back"
            on:click={_ => Router.pop()} />

<form on:submit|preventDefault={submitToimenpide(submit)}
      on:input={setDirty}
      on:change={setDirty}>

  <Content bind:toimenpide />

  <div class="flex space-x-4 pt-8">
    <Button
        disabled={Maybe.isNone(publish) &&
            Toimenpiteet.isDraft(toimenpide)}
        on:click={publishToimenpide}
        text={text(toimenpide, 'publish-button')} />

    <Button
        disabled={!dirty && !R.isNil(toimenpide.id)}
        type={'submit'}
        text={text(toimenpide, 'save-button')} />

    <Button
        disabled={!dirty}
        on:click={cancel}
        text={text(toimenpide, 'reset-button')}
        type={'reset'}
        style={'secondary'} />
  </div>
</form>

<div class="mt-5">
<TextButton text="Takaisin valvontaan"
            icon="arrow_back"
            on:click={_ => Router.pop()} />
</div>