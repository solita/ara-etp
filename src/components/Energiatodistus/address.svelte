<script>
  import * as R from 'ramda';

  import { _, locale } from '@Language/i18n';

  import * as Locales from '@Language/locale-utils';
  import * as Postinumerot from './postinumero';
  import * as Maybe from '@Utility/maybe-utils';

  export let energiatodistus;
  export let postinumerot;

  $: katuosoitteet = [
    energiatodistus.perustiedot['katuosoite-fi'],
    energiatodistus.perustiedot['katuosoite-sv']
  ];

  $: katuosoitteetOrdered = Locales.isSV($locale)
    ? R.reverse(katuosoitteet)
    : katuosoitteet;
</script>

<style type="text/postcss">
  address {
    @apply not-italic;
  }
</style>

<address>
  {Maybe.orSome(
    '',
    Maybe.orElse(katuosoitteetOrdered[1], katuosoitteetOrdered[0])
  )}
  <span class="whitespace-no-wrap">
    {Maybe.fold(
      '',
      Postinumerot.formatPostinumero(postinumerot, $locale),
      energiatodistus.perustiedot.postinumero
    )}
  </span>
</address>
