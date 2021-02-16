<script>
  import * as R from 'ramda';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Fetch from '@Utility/fetch-utils';
  import * as Future from '@Utility/future-utils';

  import { locale, _ } from '@Language/i18n';

  import LaatijaDropArea from './laatija-upload-droparea';
  import * as LaatijaUploadUtils from './laatija-upload-utils';
  import FileDropArea from '@Component/FileDropArea/FileDropArea';
  import Table from '@Component/Table/Table';
  import Button from '@Component/Button/Button';

  import { flashMessageStore } from '@/stores';

  import * as LaatijaUtils from '@Component/Laatija/laatija-utils';
  import Patevuustaso from './Patevyystaso';
  import Date from './Date';
  import * as LocaleUtils from '@Language/locale-utils';

  import * as laatijaApi from '@Component/Laatija/laatija-api';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';

  let overlay = false;

  const toggleOverlay = value => (overlay = value);

  let patevyystasot = Maybe.None();
  let laatijat = [];
  let files = [];

  $: Future.fork(
    () => {
      toggleOverlay(false);
      flashMessageStore.add('Laatija', 'error', $_('errors.unexpected'));
    },
    response => {
      toggleOverlay(false);
      patevyystasot = Maybe.Some(response);
    },
    R.tap(() => toggleOverlay(true), laatijaApi.patevyydet)
  );

  // export const fields = [
  //   { id: 'etunimi', title: 'Etunimi' },
  //   { id: 'sukunimi', title: 'Sukunimi' },
  //   { id: 'henkilotunnus', title: 'Henkilötunnus' },
  //   { id: 'jakeluosoite', title: 'Jakeluosoite' },
  //   { id: 'postinumero', title: 'Postinumero' },
  //   { id: 'postitoimipaikka', title: 'Postitoimipaikka' },
  //   { id: 'email', title: 'Email' },
  //   { id: 'puhelin', title: 'Puhelin' },
  //   { id: 'patevyystaso', title: 'Pätevyystaso', component: Patevuustaso },
  //   { id: 'toteamispaivamaara', title: 'Toteamispäivämäärä', component: Date },
  //   { id: 'toteaja', title: 'Toteaja' }
  // ];

  const fields = [
    'etunimi',
    'sukunimi',
    'henkilotunnus',
    'jakeluosoite',
    'postinumero',
    'postitoimipaikka',
    'email',
    'puhelin',
    'patevyystaso',
    'toteamispaivamaara',
    'toteaja'
  ];

  // $: submit = R.compose(
  //   Future.fork(
  //     response => {
  //       const msg = Locales.uniqueViolationMessage(
  //         $_,
  //         response,
  //         'laatija.messages.save-error'
  //       );
  //       flashMessageStore.add('Laatija', 'error', msg);
  //     },
  //     _ => {
  //       flashMessageStore.add(
  //         'Laatija',
  //         'success',
  //         $_('laatija.messages.save-success')
  //       );
  //       laatijat = [];
  //     }
  //   ),
  //   LaatijaUtils.putLaatijatFuture(fetch)
  // );

  // let files = [];
  // let laatijat = [];

  // let error = '';

  // const fileAsText = file =>
  //   new Promise((resolve, reject) => {
  //     const reader = new FileReader();
  //     reader.onload = e => {
  //       resolve(e.target.result);
  //     };
  //     reader.onerror = e => {
  //       reject(e);
  //     };
  //     reader.readAsText(file, 'UTF-8');
  //   });

  // const addError = message =>
  //   flashMessageStore.add('Laatija', 'error', $_(message));

  // $: R.compose(
  //   Future.fork(
  //     R.compose(
  //       R.tap(_ => addError('laatija.messages.upload-error')),
  //       R.tap(_ => (laatijat = []))
  //     ),
  //     R.compose(
  //       R.tap(data => (laatijat = data)),
  //       R.when(
  //         R.any(R.complement(LaatijaUtils.rowValid)),
  //         R.tap(_ => addError('laatija.messages.validation-error'))
  //       ),
  //       R.when(
  //         R.isEmpty,
  //         R.tap(_ => addError('laatija.messages.upload-error'))
  //       )
  //     )
  //   ),
  //   R.map(R.flatten),
  //   R.sequence(Future.resolve),
  //   R.map(R.map(LaatijaUtils.readData)),
  //   R.map(Future.encaseP(fileAsText))
  // )(files);

  // $: valid = R.all(LaatijaUtils.rowValid, laatijat);

  // $: files && flashMessageStore.flush('Laatija');

  $: console.log(laatijat);

  $: labelLocale = LocaleUtils.label($locale);

  $: formats = {
    patevyystaso: patevyys =>
      R.compose(
        Maybe.orSome(''),
        R.map(labelLocale),
        R.map(R.find(R.propEq('id', parseInt(patevyys)))),
        R.tap(console.log)
      )(patevyystasot)
  };

  $: console.log(LaatijaUploadUtils.errors($_, laatijat));
</script>

<style>
  .breakout {
    @apply relative px-12 mt-4 overflow-x-auto;
    width: 100vw;
    left: calc(-50vw + 50%);
  }
</style>

<Overlay {overlay}>
  <div slot="content">
    <LaatijaDropArea bind:laatijat />
    {#if laatijat.length}
      <div class="breakout overflow-x-auto">
        <table class="etp-table">
          <thead class="etp-table--thead">
            <tr class="etp-table--tr">
              {#each fields as field}
                <th class="etp-table--th">{field}</th>
              {/each}
            </tr>
          </thead>
          <tbody class="etp-table--tbody">
            {#each laatijat as laatija}
              <tr class="etp-table--tr">
                {#each fields as field}
                  <td class="etp-table--td">
                    {R.defaultTo(R.identity, formats[field])(laatija[field])}
                  </td>
                {/each}
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    {/if}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>

<!--
  <FileDropArea bind:files />
{#if laatijat.length}
  <div class="w-full overflow-x-auto mt-4">
    <Table
      {fields}
      validate={LaatijaUtils.validate}
      tablecontents={laatijat}
      itemsPerPage={50} />
  </div>
  <div class="flex -mx-4 pt-8">
    <div class="px-4">
      <Button
        disabled={!valid}
        type={'submit'}
        text={$_('laatija.lisaa-laatijat')}
        on:click={event => {
          event.preventDefault();
          flashMessageStore.flush('Laatija');
          submit(laatijat);
        }} />
    </div>
    <div class="px-4">
      <Button
        style={'secondary'}
        text={$_('peruuta')}
        on:click={event => {
          event.preventDefault();
          flashMessageStore.flush('Laatija');
          laatijat = [];
        }} />
    </div>
  </div>
{/if}
-->
