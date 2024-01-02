<script>
  import LuokitteluInput from './luokittelu-input';

  import * as R from 'ramda';

  export let nameprefix;
  export let index = 1;
  export let luokittelut = [];
  export let values = [];
</script>

<LuokitteluInput
  {nameprefix}
  {index}
  {values}
  {luokittelut}
  luokittelu={'patevyydet'} />
