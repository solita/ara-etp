<script>
  import H2 from '@Component/H/H2';
  import H3 from '@Component/H/H3';

  import Textarea from '@Component/Energiatodistus/Textarea';

  export let disabled;
  export let schema;
  export let energiatodistus;
</script>

<style>

</style>

<H2 text="Toimenpide-ehdotuksia e-luvun parantamiseksi" />
<H3
  text="Keskeiset suositukset rakennuksen e-lukua parantaviksi toimenpiteiksi" />

<div class="w-full py-4 mb-4">
  <Textarea
    {disabled}
    {schema}
    bind:model={energiatodistus}
    path={['perustiedot', 'keskeiset-suositukset-fi']} />
</div>
