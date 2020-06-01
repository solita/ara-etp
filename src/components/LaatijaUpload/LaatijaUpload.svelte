<script>
  import * as R from 'ramda';

  import * as Fetch from '@Utility/fetch-utils';
  import * as Future from '@Utility/future-utils';

  import { locale, _ } from '@Language/i18n';

  import FileDropArea from '@Component/FileDropArea/FileDropArea';
  import Table from '@Component/Table/Table';
  import Button from '@Component/Button/Button';

  import { flashMessageStore } from '@/stores';

  import * as LaatijaUtils from '@Component/Laatija/laatija-utils';
  import Patevuustaso from './Patevyystaso';
  import Date from './Date';

  export const fields = [
    { id: 'etunimi', title: 'Etunimi' },
    { id: 'sukunimi', title: 'Sukunimi' },
    { id: 'henkilotunnus', title: 'Henkilötunnus' },
    { id: 'jakeluosoite', title: 'Jakeluosoite' },
    { id: 'postinumero', title: 'Postinumero' },
    { id: 'postitoimipaikka', title: 'Postitoimipaikka' },
    { id: 'email', title: 'Email' },
    { id: 'puhelin', title: 'Puhelin' },
    { id: 'patevyystaso', title: 'Pätevyystaso', component: Patevuustaso },
    { id: 'toteamispaivamaara', title: 'Toteamispäivämäärä', component: Date },
    { id: 'toteaja', title: 'Toteaja' }
  ];

  $: submit = R.compose(
    Future.forkBothDiscardFirst(
      R.compose(
        flashMessageStore.add('Laatija', 'error'),
        R.always($_('laatija.messages.save-error'))
      ),
      R.compose(
        flashMessageStore.add('Laatija', 'success'),
        R.always($_('laatija.messages.save-success')),
        R.tap(_ => {
          laatijat = [];
        })
      )
    ),
    LaatijaUtils.putLaatijatFuture(fetch)
  );

  let files = [];
  let laatijat = [];

  let error = '';

  const fileAsText = file =>
    new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = e => {
        resolve(e.target.result);
      };
      reader.onerror = e => {
        reject(e);
      };
      reader.readAsText(file, 'UTF-8');
    });

  const addError = message =>
    flashMessageStore.add('Laatija', 'error', $_(message));

  $: R.compose(
    Future.fork(
      R.compose(
        R.tap(_ => addError('laatija.messages.upload-error')),
        R.tap(_ => (laatijat = []))
      ),
      R.compose(
        R.tap(data => (laatijat = data)),
        R.when(
          R.any(R.complement(LaatijaUtils.rowValid)),
          R.tap(_ => addError('laatija.messages.validation-error'))
        ),
        R.when(R.isEmpty, R.tap(_ => addError('laatija.messages.upload-error')))
      )
    ),
    R.map(R.flatten),
    R.sequence(Future.resolve),
    R.map(R.map(LaatijaUtils.readData)),
    R.map(Future.encaseP(fileAsText))
  )(files);

  $: valid = R.all(LaatijaUtils.rowValid, laatijat);

  $: files && flashMessageStore.flush('Laatija');
</script>

<FileDropArea bind:files />
{#if laatijat.length}
  <div class="w-full overflow-x-auto mt-4">
    <Table {fields} validate={LaatijaUtils.validate} tablecontents={laatijat} />
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
