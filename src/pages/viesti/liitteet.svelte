<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Parsers from '@Utility/parsers';
  import * as Formats from '@Utility/formats';
  import * as Locales from '@Language/locale-utils';

  import { _, locale } from '@Language/i18n';

  import Liitteet from '@Component/liitteet/liitteet.svelte';

  const i18nRoot = 'viesti.ketju.liitteet';
  const i18n = $_;

  export let params
</script>

<div class='mt-2'>
  <Liitteet
    liiteApi={{}}
    liitteet={[]}
    disabled={false}
    emptyMessageKey={i18nRoot + '.empty'}
    flashModule="viesti" />
</div>