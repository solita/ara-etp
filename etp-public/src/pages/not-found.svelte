<script>
  import { _ } from '@Localization/localization';
  import Container, { styles as containerStyles } from '@Component/container';

  import Seo from '@Component/seo';
</script>

<Seo
  title={`${$_('ENERGIATODISTUSREKISTERI')} - ${$_('PAGE_NOT_FOUND_ERROR')}`}
  noindex={true}
  nofollow={true} />

<Container {...containerStyles.white}>
  <div class="flex flex-col px-16 mt-16">
    <span class="text-6xl">404</span><span>{$_('PAGE_NOT_FOUND_ERROR')}</span>
  </div>
</Container>
