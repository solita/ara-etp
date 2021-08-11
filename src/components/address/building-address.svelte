<script>
  /**
   * An html element to show a building street address
   */

  import { _, locale } from '@Language/i18n';
  import * as Postinumerot from '@Component/address/postinumero-fi';
  import * as Maybe from '@Utility/maybe-utils';

  export let katuosoite = Maybe.None();
  export let postinumero = Maybe.None();
  export let postinumerot;
</script>

<style type="text/postcss">
  address {
    @apply not-italic;
  }
</style>

<address>
  {Maybe.orSome('', katuosoite)}
  <span class="whitespace-no-wrap">
    {Maybe.fold(
      '',
      Postinumerot.formatPostinumero(postinumerot, $locale),
      postinumero
    )}
  </span>
</address>
