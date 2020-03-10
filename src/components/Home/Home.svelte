<script>
  import { _ } from '@Language/i18n';
  import { currentUserStore } from '@/stores';
  import Link from '@Component/Link/Link';
  // This component should use currentUserStore to check if the user role and if
  // the user is laatija. Then render a real component based on that.
</script>

<style type="text/postcss">
</style>

<div>
  <h1 class="text-3xl font-fold">Laatijan etusivu</h1>
  <Link href="#/yritys/new" text="{$_('yritys.uusi_yritys')}" />
</div>
