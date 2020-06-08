<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as formats from '@Utility/formats';

  import Input from '@Component/Energiatodistus/Input';
  import BasicInput from '@Component/Input/Input';

  export let schema;
  export let energiatodistus;
  export let eLuku = Maybe.None();
</script>

<div class="flex lg:flex-row flex-col">
  <div class="lg:w-1/3 w-full py-2">
    <BasicInput
      id="energiatodistus.e-luku"
      name="energiatodistus.e-luku"
      label={$_('energiatodistus.e-luku')}
      disabled={true}
      model={R.compose( Maybe.orSome(''), R.map(formats.numberFormat) )(eLuku)}
      lens={R.lens(R.identity, R.identity)}
      i18n={$_} />
  </div>
  <div class="lg:w-1/3 w-full py-2">
    <Input
      disabled={true}
      {schema}
      center={false}
      bind:model={energiatodistus}
      format={R.compose( Maybe.orSome('- m²'), R.map(v => v + ' m²') )}
      path={['lahtotiedot', 'lammitetty-nettoala']} />
  </div>
</div>
