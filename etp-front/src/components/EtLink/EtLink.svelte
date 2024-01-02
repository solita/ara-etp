<script>
  import Link from '@Component/Link/Link.svelte';
  import { _, locale } from '@Language/i18n';

  const i18n = $_;

  export let energiatodistus;
</script>

<Link
  bold={true}
  href={`/#/energiatodistus/${energiatodistus.versio}/${energiatodistus.id}`}
  text={`${i18n('navigation.et')} ${energiatodistus.id}`} />
