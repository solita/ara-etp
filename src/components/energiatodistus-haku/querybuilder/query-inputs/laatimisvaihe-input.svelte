<script>
  import LuokitteluInput from './luokittelu-input';

  import * as R from 'ramda';

  export let nameprefix;
  export let index = 0;
  export let luokittelut = [];
</script>

<LuokitteluInput
  {nameprefix}
  {index}
  {luokittelut}
  luokittelu={'laatimisvaiheet'} />
