<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as formats from '@Utility/formats';

  import Input from '@Component/Energiatodistus/Input';
  import BasicInput from '@Component/Input/Input';

  export let eTehokkuus = Maybe.None();
</script>

<div class="flex lg:flex-row flex-col">
  <div class="lg:w-1/5 w-full py-2">
    <BasicInput
      id="energiatodistus.tulokset.e-luku"
      name="energiatodistus.tulokset.e-luku"
      label={$_('energiatodistus.tulokset.e-luku')}
      disabled={true}
      model={Maybe.fold('-',
        R.compose(formats.numberFormat, R.prop('e-luku')),
        eTehokkuus)}
      lens={R.lens(R.identity, R.identity)}
      i18n={$_} />
  </div>
  <div class="lg:w-1/5 w-full py-2">
    <BasicInput
        id="energiatodistus.tulokset.e-luokka"
        name="energiatodistus.tulokset.e-luokka"
        label={$_('energiatodistus.tulokset.e-luokka')}
        disabled={true}
        model={Maybe.fold('-', R.prop('e-luokka'), eTehokkuus)}
        lens={R.lens(R.identity, R.identity)}
        i18n={$_} />
  </div>
</div>
