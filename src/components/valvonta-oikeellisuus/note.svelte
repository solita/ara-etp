<script>
  import * as Formats from '@Utility/formats';
  import ShowMore from '@Component/show-more/show-more';
  export let note;
</script>

<div class="mb-3">
  <span class="mr-4 whitespace-no-wrap">
    {Formats.formatTimeInstantMinutes(note['create-time'])}
  </span>
  <span class="font-icon">comment</span>
  <ShowMore>
    <p>{note.description}</p>
  </ShowMore>
</div>
