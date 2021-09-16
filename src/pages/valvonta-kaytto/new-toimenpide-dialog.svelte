<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Parsers from '@Utility/parsers';
  import * as Formats from '@Utility/formats';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Locales from '@Language/locale-utils';

  import * as Toimenpiteet from './toimenpiteet';
  import * as Schema from './schema';
  import * as Osapuolet from './osapuolet';

  import * as ValvontaApi from './valvonta-api';

  import { _, locale } from '@Language/i18n';

  import H2 from '@Component/H/H2.svelte';
  import Button from '@Component/Button/Button';
  import Textarea from '@Component/Textarea/Textarea';
  import Datepicker from '@Component/Input/Datepicker';
  import { flashMessageStore } from '@/stores';
  import Select from '@Component/Select/Select';
  import * as Validation from '@Utility/validation';

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.toimenpide';

  export let id;
  export let templatesByType;
  export let toimenpide;
  export let reload;
  export let henkilot;
  export let yritykset;

  let form;
  let error = Maybe.None();
  let roolit = Maybe.None();
  let toimitustavat = Maybe.None();
  let osapuolet = R.concat(henkilot, yritykset);

  const text = R.compose(i18n, Toimenpiteet.i18nKey);

  $: templates = Toimenpiteet.templates(templatesByType)(toimenpide);
  $: formatTemplate = Locales.labelForId($locale, templates);

  $: schema = Schema.toimenpidePublish(templates, toimenpide);
  $: isValidForm = Validation.isValidForm(schema);

  $: hasErrorToimitustapaHtunnus = false;
  $: hasErrorToimitustapaYtunnus = false;
  $: hasErrorToimitustapaEmail = false;

  $: publish = toimenpide => {
    if (isValidForm(toimenpide)) {
      Future.fork(
        response => {
          const msg = i18n(
            Maybe.orSome(
              `${i18nRoot}.messages.publish-error`,
              Response.localizationKey(response)
            )
          );
          error = Maybe.Some(msg);
        },
        _ => {
          flashMessageStore.add(
            'valvonta-kaytto',
            'success',
            i18n(`${i18nRoot}.messages.publish-success`)
          );
          reload();
        },
        ValvontaApi.postToimenpide(id, toimenpide)
      );
    } else {
      error = Maybe.Some($_(`${i18nRoot}.messages.validation-error`));
      Validation.blurForm(form);
    }
  };

  $: preview = api => {
    if (isValidForm(toimenpide)) {
      Future.fork(
        response => {
          const msg = i18n(
            Maybe.orSome(
              `${i18nRoot}.messages.preview-error`,
              Response.localizationKey(response)
            )
          );
          error = Maybe.Some(msg);
        },
        response => {
          let link = document.createElement('a');
          link.target = '_blank';
          link.href = URL.createObjectURL(response);
          link.click();
          URL.revokeObjectURL(link.href);
        },
        api
      );
    } else {
      error = Maybe.Some($_(`${i18nRoot}.messages.validation-error`));
      Validation.blurForm(form);
    }
  };

  Future.fork(
    response => {
      flashMessageStore.add(
        'valvonta-kaytto',
        'error',
        i18n(Response.errorKey(i18nRoot, 'load', response))
      );
    },
    response => {
      roolit = response.roolit;
      toimitustavat = response.toimitustavat;
    },
    Future.parallelObject(2, {
      roolit: ValvontaApi.roolit,
      toimitustavat: ValvontaApi.toimitustavat
    })
  );

  $: rooliLabel = R.compose(
    Maybe.fold('', Locales.labelForId($locale, roolit)),
    R.prop('rooli-id')
  );
  $: toimitustapaLabel = R.compose(
    Maybe.fold('', Locales.labelForId($locale, toimitustavat)),
    R.prop('toimitustapa-id')
  );
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

  .buttons {
    @apply flex flex-wrap items-center mt-5 border-t-1 border-tertiary;
  }

  .error {
    @apply flex py-2 px-2 bg-error text-light;
  }
</style>

<dialog on:click|stopPropagation>
  <form class="content" bind:this={form}>
    <h1>{text(toimenpide, 'title')}</h1>

    {#each error.toArray() as txt}
      <div class="my-2 error">
        <span class="font-icon mr-2">error_outline</span>
        <div>{txt}</div>
      </div>
    {/each}

    <p>{text(toimenpide, 'info')}</p>

    {#if Toimenpiteet.hasDeadline(toimenpide)}
      <div class="flex py-4">
        <Datepicker
          label="Määräpäivä"
          bind:model={toimenpide}
          required={true}
          lens={R.lensProp('deadline-date')}
          format={Maybe.fold('', Formats.formatDateInstant)}
          parse={Parsers.optionalParser(Parsers.parseDate)}
          transform={EM.fromNull}
          validators={schema['deadline-date']}
          {i18n} />
      </div>
    {/if}

    {#if !R.isEmpty(templates)}
      <div class="w-1/2 py-4">
        <Select
          label="Valitse asiakirjapohja"
          bind:model={toimenpide}
          lens={R.lensProp('template-id')}
          parse={Maybe.fromNull}
          required={true}
          validation={true}
          format={formatTemplate}
          items={R.pluck('id', templates)} />
      </div>
    {:else}
      <div class="w-full py-4">
        <Textarea
          id={'toimenpide.description'}
          name={'toimenpide.description'}
          label={text(toimenpide, 'description')}
          bind:model={toimenpide}
          lens={R.lensProp('description')}
          required={false}
          format={Maybe.orSome('')}
          parse={Parsers.optionalString}
          validators={schema.description}
          {i18n} />
      </div>
    {/if}

    {#if !R.isEmpty(templates)}
      <div class="w-2/3">
        <H2 text={i18n(i18nRoot + '.vastaanottajat')} />
        <div class="flex flex-row my-4">
          <table class="etp-table">
            <table class="etp-table">
              <thead class="etp-table--thead">
                <tr class="etp-table--tr etp-table--tr__light">
                  <th class="etp-table--th"> {i18n(i18nRoot + '.nimi')} </th>

                  <th class="etp-table--th"> {i18n(i18nRoot + '.rooli')} </th>

                  <th class="etp-table--th">
                    {i18n(i18nRoot + '.toimitustapa')}
                  </th>

                  <th class="etp-table--th">
                    {i18n(i18nRoot + '.esikatselu')}
                  </th>
                </tr>
              </thead>
              <tbody class="etp-table--tbody">
                {#each osapuolet as osapuoli}
                  <tr class="etp-table-tr">
                    <td class="etp-table--td">
                      {#if osapuoli.nimi}
                        {`${osapuoli.nimi}`}
                      {:else}
                        {`${osapuoli.etunimi} ${osapuoli.sukunimi}`}
                      {/if}
                    </td>
                    <td class="etp-table--td">
                      {rooliLabel(osapuoli)}
                      {#if Osapuolet.otherRooli(osapuoli)}
                        {`- ${osapuoli['rooli-description']}`}
                      {/if}
                    </td>
                    <td class="etp-table--td">
                      {toimitustapaLabel(osapuoli)}
                      {#if Osapuolet.toimitustapa.other(osapuoli)}
                        {`- ${osapuoli['toimitustapa-description']}`}
                      {/if}
                    </td>
                    <td class="etp-table--td">
                      {#if osapuoli.nimi}
                        <div
                          class="text-primary cursor-pointer etp-table--td__center"
                          on:click|stopPropagation={preview(
                            ValvontaApi.previewToimenpideForYritysOsapuoli(
                              id,
                              osapuoli.id,
                              toimenpide
                            )
                          )}>
                          <span class="font-icon text-2xl"> visibility </span>
                        </div>
                      {:else}
                        <div
                          class="text-primary cursor-pointer etp-table--td__center"
                          on:click|stopPropagation={preview(
                            ValvontaApi.previewToimenpideForHenkiloOsapuoli(
                              id,
                              osapuoli.id,
                              toimenpide
                            )
                          )}>
                          <span class="font-icon text-2xl"> visibility </span>
                        </div>
                      {/if}
                    </td>
                  </tr>
                {/each}
              </tbody>
            </table>
          </table>
        </div>
      </div>
      <div class="w-full">
        {#if hasErrorToimitustapaHtunnus}
          <div class="w-full flex items-center space-x-2 ml-2">
            <span class="font-icon text-error text-2xl">info</span>
            <span
              >{i18n(i18nRoot + '.messages.error-toimitustapa-htunnus')}</span>
          </div>
        {:else if hasErrorToimitustapaYtunnus}
          <div class="w-full flex items-center space-x-2 ml-2">
            <span class="font-icon text-error text-2xl">info</span>
            <span
              >{i18n(i18nRoot + '.messages.error-toimitustapa-ytunnus')}</span>
          </div>
        {:else if hasErrorToimitustapaEmail}
          <div class="w-full flex items-center space-x-2 ml-2">
            <span class="font-icon text-error text-2xl">info</span>
            <span>{i18n(i18nRoot + '.messages.error-toimitustapa-email')}</span>
          </div>
        {/if}
      </div>
    {/if}

    <div class="buttons">
      <div class="mr-5 mt-5">
        <Button
          text={text(toimenpide, 'publish-button')}
          on:click={publish(toimenpide)} />
      </div>

      <div class="mt-5">
        <Button
          text={i18n(i18nRoot + '.cancel-button')}
          style={'secondary'}
          on:click={reload} />
      </div>
    </div>
  </form>
</dialog>
