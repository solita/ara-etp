<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';

  import Button from '@Component/Button/Button';
  import Spinner from '@Component/Spinner/Spinner';
  import Input from '@Component/Input/Input';
  import H1 from '@Component/H/H1';
  import { _ } from '@Language/i18n';
  import * as api from './viesti-api';

  export let close;
  export let ketjuId;
  export let energiatodistusId = '';

  const i18n = $_;
  const i18nRoot = 'viesti.ketju.existing.link-et';

  let form;
  let showLinkSpinner = false;
  let showUnlinkSpinner = false;
  let error = Maybe.None();

  const updateKetju = R.compose(
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            `${i18nRoot}.messages.update-error`,
            Response.localizationKey(response)
          )
        );
        buttonsDisabled = false;
        showLinkSpinner = false;
        showUnlinkSpinner = false;
        error = Maybe.Some(msg);
      },
      _ => {
        buttonsDisabled = false;
        showLinkSpinner = false;
        showUnlinkSpinner = false;
        close(true);
      }
    ),
    R.tap(() => {
      buttonsDisabled = true;
    }),
    api.putKetju(fetch, ketjuId)
  );

  const addLink = () => {
    if (!energiatodistusId) {
      error = Maybe.Some(i18n(`${i18nRoot}.messages.validation-error`));
    } else {
      error = Maybe.None();
      showLinkSpinner = true;

      updateKetju({
        'energiatodistus-id': parseInt(energiatodistusId)
      });
    }
  };
  const removeLink = () => {
    error = Maybe.None();
    showUnlinkSpinner = true;
    updateKetju({
      'energiatodistus-id': null
    });
  };

  let buttonsDisabled = false;
</script>

<style type="text/postcss">
  dialog {
    @apply fixed top-0 w-screen left-0 z-50 h-screen bg-hr cursor-default flex justify-center items-center;
  }

  .content {
    @apply relative bg-light w-2/3 py-10 px-10 rounded-md shadow-lg flex flex-col justify-center;
  }

  .buttons {
    @apply flex flex-wrap items-center mt-5 pt-4 border-t-1 border-tertiary;
  }

  .error {
    @apply flex py-2 px-2 bg-error text-light;
  }
</style>

<dialog on:click|stopPropagation>
  <form class="content" bind:this={form}>
    <H1 text={i18n(i18nRoot + '.title')} />

    {#each error.toArray() as txt}
      <div class="my-2 error">
        <span class="font-icon mr-2">error_outline</span>
        <div>{txt}</div>
      </div>
    {/each}

    <div class="lg:mr-64">
      <Input
        id={'dialog.link-et.input'}
        name={'dialog.link-et.input'}
        label={i18n(i18nRoot + '.input-label')}
        compact={false}
        required={true}
        bind:model={energiatodistusId}
        {i18n} />
    </div>
    <div
      class="buttons flex-col lg:flex-row space-y-2 lg:space-x-2 lg:space-y-0">
      <Button
        disabled={buttonsDisabled}
        on:click={addLink}
        style="primary"
        text={i18n(i18nRoot + '.button-link')}>
        {#if showLinkSpinner}
          <Spinner smaller={true} white={true} />
        {/if}
      </Button>
      <Button
        disabled={!energiatodistusId || buttonsDisabled}
        on:click={removeLink}
        style="secondary"
        text={i18n(i18nRoot + '.button-unlink')}>
        {#if showUnlinkSpinner}
          <Spinner smaller={true} />
        {/if}
      </Button>
      <div class="lg:justify-self-end lg:ml-auto">
        <Button
          on:click={() => {
            close(false);
          }}
          style="secondary"
          text={i18n('peruuta')} />
      </div>
    </div>
  </form>
</dialog>
