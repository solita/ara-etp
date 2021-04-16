<script>
  import * as Formats from '@Utility/formats';

  import { _, locale } from '@Language/i18n';
  import * as Locales from '@Language/locale-utils';

  export let toimenpide;
  export let toimenpidetyypit;
</script>

<div>
  {Formats.formatTimeInstant(toimenpide['publish-time'])} -
  {Locales.labelForId($locale, toimenpidetyypit)(toimenpide['type-id'])}
</div>
