<script>
  import * as R from 'ramda';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Formats from '@Utility/formats';
  import ShowMore from '@Component/show-more/show-more';

  export let note;
  export let i18n;
  export let valvojat;

  const i18nRoot = 'valvonta.oikeellisuus.note';

  $: author = Maybe.findById(R.prop('author-id', note), valvojat);
</script>

<div class="flex flex-col space-y-1">
  <div class="flex">
    <span class="mr-2 whitespace-no-wrap">
      {Formats.formatTimeInstantMinutes(note['create-time'])}
    </span>
    <span class="font-icon mr-1">comment</span>
    <span>{i18n(`${i18nRoot}.title`)}</span>
    {#each Maybe.toArray(author) as a}
      <span class="ml-1">
        ({`${R.prop('etunimi', a)} ${R.prop('sukunimi', a)}`})
      </span>
    {/each}
  </div>
  <ShowMore>
    <p>{note.description}</p>
  </ShowMore>
</div>
