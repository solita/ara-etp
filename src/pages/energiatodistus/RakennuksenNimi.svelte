<script>
  import * as R from 'ramda';

  import { _, locale } from '@Language/i18n';

  import * as Locales from '@Language/locale-utils';
  import * as Maybe from '@Utility/maybe-utils';

  export let energiatodistus;

  $: names = [
    energiatodistus.perustiedot['nimi-fi'],
    energiatodistus.perustiedot['nimi-sv']
  ];

  $: namesOrdered = Locales.isSV($locale) ? R.reverse(names) : names;

  $: name = Maybe.orElse(namesOrdered[1], namesOrdered[0]);
</script>

{Maybe.orSome('', name)}
