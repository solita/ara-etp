<script>
  import H1 from '@Component/H/H1.svelte';
  import { _ } from '@Language/i18n';

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.new-kohde';
</script>

<H1 text={i18n(i18nRoot + '.title')} />

