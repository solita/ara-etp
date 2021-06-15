<script>
  import * as R from 'ramda';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Formats from '@Utility/formats';
  import ShowMore from '@Component/show-more/show-more';

  export let note;
  export let i18n;
  export let valvojat;

  $: author = Maybe.findById(R.prop('author-id', note), valvojat);
</script>

<div class="mb-3">
  <span class="mr-4 whitespace-no-wrap">
    {Formats.formatTimeInstantMinutes(note['create-time'])}
  </span>
  <span class="font-icon">comment</span>
  <span>Muistiinpano</span>
  {#each Maybe.toArray(author) as a}
    <span class="ml-1">
      ({`${R.prop('etunimi', a)} ${R.prop('sukunimi', a)}`})
    </span>
  {/each}
  <ShowMore>
    <p>{note.description}</p>
  </ShowMore>
</div>
