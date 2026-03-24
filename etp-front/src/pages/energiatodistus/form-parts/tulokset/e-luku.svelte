<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as formats from '@Utility/formats';
  import * as ETUtils from '@Pages/energiatodistus/energiatodistus-utils';

  import Input from '@Component/Input/Input';

  export let eTehokkuus = Maybe.None();
  export let idSuffix;
  export let tayttaaAplusVaatimukset = false;
  export let tayttaaA0Vaatimukset = false;

  $: displayETehokkuus = Maybe.map(raw => {
    const rajaAsteikko = raw['raja-asteikko'];
    const localELuku = raw['e-luku'];
    if (rajaAsteikko && localELuku != null) {
      const rawEluokka = ETUtils.eluokkaFromRajaAsteikko(
        rajaAsteikko,
        localELuku
      );
      return R.assoc(
        'e-luokka',
        ETUtils.applyEluokkaDowngrade(
          rawEluokka,
          tayttaaAplusVaatimukset,
          tayttaaA0Vaatimukset
        ),
        raw
      );
    }
    return raw;
  }, eTehokkuus);
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
        displayETehokkuus
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
      model={Maybe.fold('-', R.prop('e-luokka'), displayETehokkuus)}
      lens={R.lens(R.identity, R.identity)}
      i18n={$_} />
  </div>
</div>
