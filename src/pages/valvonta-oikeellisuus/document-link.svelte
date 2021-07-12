<script>
  import * as ValvontaApi from '@Pages/valvonta-oikeellisuus/valvonta-api';
  import Link from '@Component/Link/Link.svelte';

  export let toimenpide;
</script>

<Link
  text={toimenpide.filename}
  target={'_blank'}
  href={ValvontaApi.url.document(
    toimenpide['energiatodistus-id'],
    toimenpide.id,
    toimenpide['filename']
  )} />
