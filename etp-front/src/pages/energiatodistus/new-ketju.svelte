<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';

  import * as EtApi from '@Pages/energiatodistus/energiatodistus-api';
  import NewKetju from '@Pages/viesti/new-ketju.svelte';

  export let params;
</script>

<NewKetju
  etFuture={R.map(
    Maybe.Some,
    EtApi.getEnergiatodistusById(params.version, params.id)
  )} />
