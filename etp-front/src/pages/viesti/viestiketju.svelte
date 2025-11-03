<script>
  import * as R from 'ramda';
  import * as Formats from '@Utility/formats';
  import * as Viestit from '@Pages/viesti/viesti-util';
  import * as MD from '@Component/text-editor/markdown';

  import SenderRecipients from './sender-recipients.svelte';
  import User from './user.svelte';
  import ViestiketjuHandler from './viestiketju-handler.svelte';
  import Style from './style-body-preview.svelte';

  export let ketju;
  export let whoami;
  export let vastaanottajaryhmat;
  export let kasittelijat;
  export let submitKasitelty;
  const sentTime = R.compose(
    Formats.formatTimeInstantMinutes,
    R.prop('sent-time'),
    R.last,
    R.prop('viestit')
  );

  $: unread = Viestit.hasUnreadViesti(ketju.viestit);
</script>

<style>
  a:first-child {
    @apply border-t-2;
  }
  .unread {
    @apply bg-backgroundhalf;
  }
  .unread:hover {
    @apply bg-althover;
  }
</style>

<a
  href={`#/viesti/${ketju.id}`}
  class="flex flex-col border-b-2 border-background p-2 hover:bg-althover"
  class:unread>
  <div class="flex w-full items-start justify-start">
    <span class="w-2/12 border border-transparent py-1">
      {sentTime(ketju)}
    </span>
    <div
      class="flex w-9/12 flex-wrap items-center py-1"
      title={R.gt(R.length(ketju.subject), 40) ? ketju.subject : ''}>
      <span class="subject mr-3 self-start truncate font-bold">
        {ketju.subject}
      </span>

      <SenderRecipients
        icons="true"
        sender={R.prop('from', R.head(ketju.viestit))}
        {whoami}
        recipients={R.prop('vastaanottajat', ketju)}
        recipientGroup={Viestit.findKetjuVastaanottajaryhma(
          vastaanottajaryhmat,
          ketju
        )} />
    </div>
    {#if Viestit.isKasittelija(whoami)}
      <div class="ml-auto flex-shrink justify-self-end">
        <button
          on:click|preventDefault|stopPropagation={submitKasitelty(
            R.prop('id', ketju),
            !ketju.kasitelty
          )}
          class="flex items-center space-x-1">
          {#if ketju.kasitelty}
            <span class="material-icons text-primary"> check_box </span>
          {:else}
            <span class="material-icons"> check_box_outline_blank </span>
          {/if}
        </button>
      </div>
    {/if}
  </div>

  <div class="flex items-center">
    <div class="flex w-2/12 flex-col">
      <span class="block">
        <span
          class="mr-1 font-icon"
          class:text-primary={!unread}
          class:text-error={unread}>
          chat
        </span>
        <span class:font-bold={unread}>{R.length(ketju.viestit)}</span>
      </span>
    </div>

    <div class="flex w-10/12 items-center justify-between">
      <div class="flex items-center overflow-hidden">
        <div class="whitespace-nowrap font-bold">
          <User user={R.prop('from', R.last(ketju.viestit))} {whoami} />
        </div>
        :
        <div class="flex space-x-1 truncate p-1" class:font-bold={unread}>
          <Style>
            {@html MD.toHtml(R.last(ketju.viestit).body)}
          </Style>
        </div>
      </div>
      {#if Viestit.isKasittelija(whoami)}
        <ViestiketjuHandler {ketju} {kasittelijat} {whoami} />
      {/if}
    </div>
  </div>
</a>
