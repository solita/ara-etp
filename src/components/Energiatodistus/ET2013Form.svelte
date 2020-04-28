<script>

  import H1 from '@Component/H/H1';
  import Input from './Input';

  export let energiatodistus;
  export let schema;
  export let disabled = false;

</script>

<div class="w-full mt-3">
  <H1 text="Energiatodistus 2013 - not implemented" />
  <div class="flex flex-col py-4 -mx-4">
    <div class="lg:w-1/2 w-full px-4 py-4">
      <Input
          {disabled}
          {schema}
          bind:model={energiatodistus}
          path={['perustiedot', 'yritys', 'nimi']} />
    </div>
  </div>
</div>
