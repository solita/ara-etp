<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Parsers from '@Utility/parsers';
  import * as Formats from '@Utility/formats';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Toimenpiteet from './toimenpiteet';

  import * as ValvontaApi from './valvonta-api';

  import { _ } from '@Language/i18n';

  import Button from '@Component/Button/Button';
  import Textarea from '@Component/Textarea/Textarea';
  import Datepicker from '@Component/Input/Datepicker';
  import { flashMessageStore } from '@/stores';
  import Select from '../Select/Select.svelte';

  const i18nRoot = 'valvonta.oikeellisuus.new-toimenpide';

  export let id;
  export let toimenpide;
  export let reload;

  const publish = toimenpide => {
    Future.fork(
      response => {
        const msg = $_(
          Maybe.orSome(
            `${i18nRoot}.messages.error`,
            Response.localizationKey(response)
          )
        );
        flashMessageStore.add('valvonta-oikeellisuus', 'error', msg);
      },
      _ => {
        flashMessageStore.add(
          'valvonta-oikeellisuus',
          'success',
          $_(`${i18nRoot}.messages.success`)
        );
        reload();
      },
      ValvontaApi.postToimenpide(id, toimenpide)
    );
  };

  const text = R.compose($_, Toimenpiteet.i18nKey);
</script>

<style type="text/postcss">
  dialog {
    @apply fixed top-0 w-screen left-0 z-50 h-screen bg-hr cursor-default flex justify-center items-center;
  }

  .content {
    @apply relative bg-light w-2/3 py-10 px-10 rounded-md shadow-lg flex flex-col justify-center;
  }

  h1 {
    @apply text-secondary font-bold uppercase text-lg mb-4 pb-2 border-b-1 border-tertiary tracking-xl;
  }

  p {
    @apply mt-2;
  }

  .buttons {
    @apply flex flex-wrap items-center mt-5 border-t-1 border-tertiary;
  }
</style>

<dialog on:click|stopPropagation>
  <div class="content">
    <h1>{text(toimenpide, 'title')}</h1>

    <p>{text(toimenpide, 'description')}</p>

    {#if Toimenpiteet.hasDeadline(toimenpide)}
      <div class="flex py-4">
        <Datepicker
          label="Määräpäivä"
          bind:model={toimenpide}
          lens={R.lensProp('deadline-date')}
          format={Maybe.fold('', Formats.formatDateInstant)}
          parse={Parsers.optionalParser(Parsers.parseDate)}
          transform={EM.fromNull}
          i18n={$_} />
      </div>
    {/if}

    {#if Toimenpiteet.hasDocumentTemplate(toimenpide)}
      <div class="w-1/2 py-4">
        <Select
          label="Valitse asiakirjapohja"
          model={Maybe.None()}
          lens={R.lens(R.identity, R.identity)}
          items={[
            'Energiatodistus 2013 FI',
            'Energiatodistus 2013 SV',
            'Energiatodistus 2018 FI',
            'Energiatodistus 2018 SV'
          ]} />
      </div>
    {:else}
      <div class="w-full py-4">
        <Textarea
          id={'toimenpide.document'}
          name={'toimenpide.document'}
          label={text(toimenpide, 'document')}
          bind:model={toimenpide}
          lens={R.lensProp('document')}
          required={false}
          format={Maybe.orSome('')}
          parse={Parsers.optionalString}
          i18n={$_} />
      </div>
    {/if}

    <div class="buttons">
      <div class="mr-5 mt-5">
        <Button
          text={$_(
            `${i18nRoot}.${Toimenpiteet.typeKey(
              toimenpide['type-id']
            )}.publish-button`
          )}
          on:click={publish(toimenpide)} />
      </div>

      <div class="mt-5">
        <Button
          text={$_(i18nRoot + '.cancel-button')}
          style={'secondary'}
          on:click={reload} />
      </div>
    </div>
  </div>
</dialog>
