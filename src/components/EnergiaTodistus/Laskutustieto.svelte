<script>
  import { slide } from 'svelte/transition';
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import Input from '@Component/Input/Input';

  export let laskutustieto;
</script>

<style>

</style>

<div transition:slide={{ duration: 200 }}>
  <Input
    disabled={true}
    id={'nimi'}
    name={'nimi'}
    label={$_('energiatodistus.laskutustiedot.nimi')}
    required={true}
    bind:model={laskutustieto}
    lens={R.lensProp('nimi')} />

  <Input
    disabled={true}
    id={'ytunnus'}
    name={'ytunnus'}
    label={$_('yritys.y-tunnus')}
    required={true}
    bind:model={laskutustieto}
    lens={R.lensProp('ytunnus')} />
</div>
