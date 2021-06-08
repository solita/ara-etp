<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';

  import * as EtApi from '@Component/Energiatodistus/energiatodistus-api';
  import NewKetju from '@Component/viesti/new-ketju.svelte';

  export let params;
</script>

<NewKetju etFuture={R.map(Maybe.Some, EtApi.getEnergiatodistusById(params.version, params.id))} />