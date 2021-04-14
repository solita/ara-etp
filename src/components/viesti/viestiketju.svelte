<script>
  import * as R from 'ramda';
  import * as Formats from '@Utility/formats';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as Viestit from '@Component/viesti/viesti-util';

  import SenderRecipients from './sender-recipients.svelte';

  import { _ } from '@Language/i18n';

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
</script>

<style>
  a:first-child {
    @apply border-t-2;
  }
</style>

<!-- purgecss: font-bold text-primary -->

<a
  href={`#/viesti/${ketju.id}`}
  class="flex flex-col border-b-2 border-background hover:bg-background p-2">
  <div class="flex items-start w-full justify-start">
    <span class="w-2/12 py-1 border border-transparent">
      {sentTime(ketju)}
    </span>
    <div
      class="w-9/12 py-1 flex flex-wrap items-center"
      title={R.gt(R.length(ketju.subject), 40) ? ketju.subject : ''}>
      <span class="subject truncate font-bold self-start mr-2">
        {ketju.subject}
      </span>

      <SenderRecipients
        sender={R.prop('from', R.head(ketju.viestit))}
        {whoami}
        recipients={R.prop('vastaanottajat', ketju)}
        recipientGroup={Viestit.findKetjuVastaanottajaryhma(
          vastaanottajaryhmat,
          ketju
        )} />
    </div>
    {#if !Kayttajat.isLaatija(whoami)}
      <div class="flex-shrink justify-self-end ml-auto">
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
    <div class="flex flex-col w-2/12">
      <span class="block">
        <span class="font-icon outline mr-1 text-primary"> chat </span>
        <span>{R.length(ketju.viestit)}</span>
      </span>
    </div>

    <div class="flex w-10/12 items-center justify-between">
      <div class="flex items-center overflow-hidden">
        <SenderRecipients
          sender={R.prop('from', R.last(ketju.viestit))}
          {whoami} />:
        <div class="truncate p-1">
          {R.last(ketju.viestit).body}
        </div>
      </div>
      {#if !Kayttajat.isLaatija(whoami)}
        {#if R.prop('kasittelija-id', ketju)}
          <span class="whitespace-no-wrap">
            {R.compose(
              R.join(' '),
              R.props(['etunimi', 'sukunimi']),
              R.find(R.propEq('id', R.prop('kasittelija-id', ketju)))
            )(kasittelijat)}
          </span>
        {:else}
          <span class="text-error whitespace-no-wrap">
            {$_('viesti.ketju.existing.no-handler')}
          </span>
        {/if}
      {/if}
    </div>
  </div>
</a>
