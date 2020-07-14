<script>
  import * as R from 'ramda';
  import Select from '@Component/Select/Select';
  import SimpleInput from '@Component/Input/SimpleInput';
  import { _ } from '@Language/i18n';

  export let model;
  export let lens;
  export let operators = [];

  const operatorLens = R.compose(
    lens,
    R.lensIndex(0)
  );

  const paramLens = R.compose(
    lens,
    R.lensIndex(2)
  );
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
    <SimpleInput
      center={true}
      on:input={evt => (model = R.set(paramLens, evt.target.value, model))}
      viewValue={R.view(paramLens, model)} />
  </div>
</div>
