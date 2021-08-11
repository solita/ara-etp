<script>
  import * as R from 'ramda';

  import { _, locale } from '@Language/i18n';

  import * as Locales from '@Language/locale-utils';
  import * as Maybe from '@Utility/maybe-utils';

  import * as Address from '@Component/address/building-address.svelte';

  export let energiatodistus;
  export let postinumerot;

  $: katuosoitteet = [
    energiatodistus.perustiedot['katuosoite-fi'],
    energiatodistus.perustiedot['katuosoite-sv']
  ];

  $: katuosoitteetOrdered = Locales.isSV($locale)
    ? R.reverse(katuosoitteet)
    : katuosoitteet;

  $: katuosoite = Maybe.orElse(katuosoitteetOrdered[1], katuosoitteetOrdered[0]);
  $: postinumero = energiatodistus.perustiedot.postinumero;
</script>

<Address {katuosoite} {postinumero} {postinumerot} />
