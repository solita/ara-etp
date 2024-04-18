<script>
  import Button from '@Component/Button/Button.svelte';
  import { _ } from '@Language/i18n';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as R from 'ramda';
  import * as etApi from '@Pages/energiatodistus/energiatodistus-api';

  export let energiatodistus;
  export let reload;

  const i18n = $_;

  let error = Maybe.None();
  let inProgress = false;

  const signingProcess = () => {
    Future.fork(
      response => {
        const errorKey =
          'energiatodistus.signing.error.' + R.path(['body', 'type'], response);
        const message = i18n(errorKey);
        error = R.equals(message, errorKey)
          ? Maybe.Some(i18n('energiatodistus.signing.error.signing-failed'))
          : Maybe.Some(message);
      },
      response => {
        console.log('jee');
      },
      etApi.signPdfUsingSystemSignature(
        fetch,
        energiatodistus.versio,
        energiatodistus.id,
        'fi'
      )
    );
  };

  const sign = () => {
    inProgress = true;
    error = Maybe.None();
    signingProcess();
  };
</script>

<style type="text/postcss">
  .buttons {
    @apply flex flex-wrap items-center mt-5 border-t-1 border-tertiary;
  }
</style>

<div>
  <div>Allekirjoitamme ilman korttia kiitos</div>

  {#each error.toArray() as txt}
    <div class="my-2 error">
      <span class="font-icon mr-2">error_outline</span>
      <div>{txt}</div>
    </div>
  {/each}

  {#if inProgress}
    <div class="mt-2">
      <Spinner />
    </div>
  {/if}

  <div class="buttons">
    <div class="mr-10 mt-5">
      <Button
        prefix="signing-submit"
        disabled={inProgress}
        text={i18n('energiatodistus.signing.button.start')}
        on:click={sign} />
    </div>
    <div class="mt-5">
      <Button
        prefix="signing-close"
        disabled={inProgress}
        text={i18n('energiatodistus.signing.button.close')}
        style={'secondary'}
        on:click={reload} />
    </div>
  </div>
</div>
