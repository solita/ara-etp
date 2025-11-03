<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as formats from '@Utility/formats';

  import Input from '@Component/Input/Input';

  export let eTehokkuus = Maybe.None();
  export let idSuffix;
</script>

<div class="mb-12 flex flex-col lg:flex-row">
  <div class="w-full lg:w-1/5">
    <Input
      id={'energiatodistus.tulokset.e-luku.' + idSuffix}
      name="energiatodistus.tulokset.e-luku"
      label={$_('energiatodistus.tulokset.e-luku')}
      disabled={true}
      model={Maybe.fold(
        '-',
        R.compose(formats.numberFormat, R.prop('e-luku')),
        eTehokkuus
      )}
      lens={R.lens(R.identity, R.identity)}
      i18n={$_} />
  </div>
  <div class="w-full lg:w-1/5">
    <Input
      id={'energiatodistus.tulokset.e-luokka.' + idSuffix}
      name="energiatodistus.tulokset.e-luokka"
      label={$_('energiatodistus.tulokset.e-luokka')}
      disabled={true}
      model={Maybe.fold('-', R.prop('e-luokka'), eTehokkuus)}
      lens={R.lens(R.identity, R.identity)}
      i18n={$_} />
  </div>
</div>
