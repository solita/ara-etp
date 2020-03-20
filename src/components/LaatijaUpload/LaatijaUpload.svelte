<script>
  import * as R from 'ramda';

  import * as Fetch from '@Utility/fetch-utils';
  import * as Future from '@Utility/future-utils';

  import { locale, _ } from '@Language/i18n';

  import Fileupload from '@Component/Fileupload/Fileupload';
  import Table from '@Component/Table/Table';
  import Button from '@Component/Button/Button';

  import { flashMessageStore } from '@/stores';

  import {
    parse,
    validate,
    readData,
    dataValid,
    putLaatijatFuture
  } from './laatija-utils';
  import Patevuustaso from './Patevyystaso';
  import Date from './Date';

  let laatijaData;
  let update = data => {
    // TODO: Same file selection does not trigger this
    flashMessageStore.flush('Laatija');
    let files = R.compose(data)({});
    const reader = new FileReader();
    reader.onload = e => {
      laatijaData = e.target.result;
    };
    reader.readAsText(files.files[0], 'UTF-8');
  };

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
        R.always($_('laatija.messages.save-success'))
      )
    ),
    putLaatijatFuture(fetch)
  );

  $: tablecontents = readData(laatijaData);
  $: valid = dataValid(tablecontents);
  $: isLaatijaData = R.allPass([
    R.complement(R.isEmpty),
    R.complement(R.isNil)
  ])(laatijaData);
  $: readError = R.and(isLaatijaData, R.isEmpty(tablecontents));
  $: readOk = R.and(isLaatijaData, R.complement(R.isEmpty)(tablecontents));

  $: R.when(
    R.allPass([R.always(readOk), R.not]),
    R.compose(
      flashMessageStore.add('Laatija', 'error'),
      R.always($_('laatija.messages.validation-error'))
    )
  )(valid);

  $: R.when(
    R.identity,
    R.compose(
      flashMessageStore.add('Laatija', 'error'),
      R.always($_('laatija.messages.load-error'))
    )
  )(readError);
</script>

<style type="text/postcss">

</style>

<Fileupload {update} />
{#if readOk}
  <div class="w-full overflow-x-auto mt-4">
    <Table {fields} {validate} {tablecontents} />
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
          submit(tablecontents);
        }} />
    </div>
    <div class="px-4">
      <Button
        style={'secondary'}
        text={$_('peruuta')}
        on:click={event => {
          event.preventDefault();
          flashMessageStore.flush('Laatija');
          laatijaData = undefined;
        }} />
    </div>
  </div>
{/if}
