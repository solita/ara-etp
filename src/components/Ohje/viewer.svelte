<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Kayttajat from '@Utility/kayttajat';

  import H1 from '@Component/H/H1';
  import Link from '@Component/Link/Link';

  export let sivu;
  export let whoami;
</script>

<style>
</style>

<div class="w-full flex flex-col">
  <div class="flex justify-between items-start">
    <H1 text={sivu.title} />
    {#if Kayttajat.isPaakayttaja(whoami)}
      <div class="mb-auto">
        <Link
          href={`/#/ohje/${sivu.id}/edit`}
          icon={Maybe.Some('edit')}
          text={'Edit_Page'} />
      </div>
    {/if}
  </div>
  <p class="whitespace-pre-wrap">{sivu.body}</p>
</div>
