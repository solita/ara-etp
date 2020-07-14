<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';

  import Select from '@Component/Select/Select';
  import Datepicker from '@Component/Datepicker/Datepicker';
  import { _ } from '@Language/i18n';

  import * as dfns from 'date-fns';

  export let model;
  export let lens;
  export let operators;

  const operatorLens = R.compose(
    lens,
    R.lensIndex(0)
  );

  const paramLens = R.compose(
    lens,
    R.lensIndex(2)
  );

  let selected = R.compose(
    Maybe.orSome(new Date()),
    R.map(dfns.parseISO),
    Maybe.fromEmpty,
    R.view(paramLens)
  )(model);

  $: model = R.set(paramLens, dfns.lightFormat(selected, 'yyyy-MM-dd'), model);
</script>

<style>

</style>

<div class="flex">
  <div class="w-1/2">
    <Select
      items={R.map(R.prop('label'), operators)}
      bind:model
      lens={operatorLens}
      format={R.compose( $_, R.concat('energiatodistus.haku.') )}
      parse={R.identity}
      allowNone={false} />
  </div>
  <div class="w-1/2 pl-4 flex flex-col justify-end">
    <Datepicker bind:selected />
  </div>
</div>
