<script>
  import { _ } from '@Localization/localization';
</script>

<div class="flex flex-col items-center text-white py-4">
  <strong class="uppercase">{$_('DISCLAIMER_KEHITYSVERSIO')}</strong>
  <p>
    <slot />
    {$_('DISCLAIMER_VIRALLINENVERSIO')}
    <a href="https://www.energiatodistusrekisteri.fi">
      www.energiatodistusrekisteri.fi
    </a>
  </p>
</div>
