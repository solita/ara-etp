<script>
  import LuokitteluInput from './luokittelu-input';

  export let nameprefix;
  export let index = 0;
  export let luokittelut = [];
  export let values = [];
</script>

<LuokitteluInput
  {nameprefix}
  {index}
  {values}
  {luokittelut}
  luokittelu={'lammitysmuoto'} />
