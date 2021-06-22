<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';

  import Button from '@Component/Button/Button';
  import Input from '@Component/Input/Input';
  import H1 from '@Component/H/H1';
  import { _ } from '@Language/i18n';
  import * as api from './viesti-api';

  export let close;
  export let ketjuId;
  export let energiatodistusId;

  const i18n = $_;
  const i18nRoot = 'viesti.ketju.existing.link-et';

  let form;
  let error = Maybe.None();
  let inputEtId = energiatodistusId || '';

  const updateKetju = ketjuId =>
    R.compose(
      Future.fork(
        response => {
          const msg = i18n(
            Maybe.orSome(
              `${i18nRoot}.messages.error`,
              Response.localizationKey(response)
            )
          );
          overlay = false;
          error = Maybe.Some(msg);
        },
        _ => {
          flashMessageStore.add(
            'viesti',
            'success',
            i18n(`${i18nRoot}.messages.update-success`)
          );
          close();
        }
      ),
      R.tap(enableOverlay),
      api.putKetju(fetch, ketjuId)
    );

  // const submitForm = () => {
  //   if (!inputEtId) {
  //     error = Maybe.Some('ei voi olla tyhja');
  //   } else {
  //     error = Maybe.None();
  //     console.log('ketjuId', ketjuId);
  //     console.log('inputEtId', inputEtId);
  //     updateKetju(ketjuId, {
  //       'energiatodistus-id': inputEtId
  //     });
  //   }
  // };

  let overlay = false;
  let enableOverlay = _ => {
    console.log('overlay?');
    overlay = true;
  };
</script>

<style type="text/postcss">
  dialog {
    @apply fixed top-0 w-screen left-0 z-50 h-screen bg-hr cursor-default flex justify-center items-center;
  }

  .content {
    @apply relative bg-light w-2/3 py-10 px-10 rounded-md shadow-lg flex flex-col justify-center;
  }

  .buttons {
    @apply flex flex-wrap items-center mt-5 border-t-1 border-tertiary;
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

    <div class="mr-64">
      <Input
        id={'dialog.link-et.input'}
        name={'dialog.link-et.input'}
        label={i18n(i18nRoot + '.input-label')}
        compact={false}
        required={true}
        bind:model={inputEtId}
        {i18n} />
    </div>
    <div class="buttons">
      <div class="mr-2">
        <Button
          disabled={overlay}
          on:click={updateKetju(ketjuId, {
            'energiatodistus-id': inputEtId
          })}
          style="primary"
          text={i18n(i18nRoot + '.button-link')} />
      </div>
      <div class="mr-2">
        <Button
          disabled={energiatodistusId || overlay}
          on:click={updateKetju(ketjuId, {
            'energiatodistus-id': Maybe.None()
          })}
          style="secondary"
          text={i18n(i18nRoot + '.button-unlink')} />
      </div>
      <div class="justify-self-end ml-auto">
        <Button
          disabled={overlay}
          on:click={close}
          style="secondary"
          text={i18n('peruuta')} />
      </div>
    </div>
  </form>
</dialog>
