<script>
  import { laatijat as laatijatStore } from '@/stores';
  import { _, locale } from '@Localization/localization';
  import Seo from '@Component/seo';

  import Container, { styles as containerStyles } from '@Component/container';
  import Button, { styles as buttonStyles } from '@Component/button';
  import Spinner from '@Component/spinner';
  import Link from '@Component/link';
  import { onMount } from 'svelte';
  import { backReferred } from '@/router/router';
  import { parseDate } from '@/utilities/parsers';
  import * as formats from '@/utilities/formats';
  import { announceAssertively } from '@/utilities/announce';

  export let id;
  let ref = '';

  let component = null;
  $: laatijaPromise = $laatijatStore.then(laatijat => {
    const laatijaFound = laatijat.find(laatija => laatija.id == id);
    if (laatijaFound) return laatijaFound;

    return Promise.reject('Laatijaa ei löytynyt.');
  });

  let didAnnounce = false;

  $: laatijaPromise?.then(laatija => {
    if (!didAnnounce)
      announceAssertively(`${$_('ET_LAATIJA')} ${id} - ${laatija.nimi}`);
    didAnnounce = true;
  });

  onMount(() => {
    component.scrollIntoView();

    ref = window.history.state.path.includes('&ref=')
      ? decodeURIComponent(window.history.state.path.split('&ref=')[1])
      : '';
    if (ref)
      window.history.replaceState(
        {},
        document.title,
        window.history.state.path.split('&ref=')[0]
      );
  });
</script>

<Seo
  title="{$_('ENERGIATODISTUSREKISTERI')} - {$_('ET_LAATIJA')}"
  descriptionFi={$locale == 'fi' ? $_('ET_LAATIJA') : undefined}
  descriptionSv={$locale == 'sv' ? $_('ET_LAATIJA') : undefined} />
<div bind:this={component}>
  <Container {...containerStyles.beige}>
    <div
      class="flex flex-col justify-center items-left sm:px-16 sm:py-8 px-4 py-4">
      <div>
        <Button
          {...buttonStyles.green}
          on:click={() => {
            backReferred(ref ? '/laatijahaku?' + ref : '/laatijahaku');
          }}>
          <span class="material-icons" aria-hidden="true">arrow_back</span>
          <span>{$_('LAATIJA_TAKAISIN')}</span>
        </Button>
      </div>
    </div>
  </Container>
  <Container {...containerStyles.white}>
    {#await laatijaPromise}
      <div class="flex justify-center">
        <Spinner />
      </div>
    {:then laatija}
      <div
        class="flex flex-col px-4 lg:px-8 xl:px-16 mx-auto items-start mb-16">
        <h1 class="text-xl my-8">{laatija.nimi}</h1>
        <div class="flex flex-col md:flex-row text-lg space-x-2 my-1 w-full">
          <strong class="w-full md:w-1/3 text-lg text-ashblue tracking-widest"
            >{$_('LAATIJA_PATEVYYSTASO')}:</strong>
          <span>{laatija.patevyys}</span>
        </div>
        <div class="flex flex-col md:flex-row text-lg space-x-2 my-1 w-full">
          <strong class="w-full md:w-1/3 text-lg text-ashblue tracking-widest"
            >{$_('LAATIJA_VOIMASSAOLOAIKA')}:</strong>
          <span>
            {formats.formatExclusiveEndDate(
              parseDate(laatija['voimassaolo-paattymisaika'])
            )}
          </span>
        </div>
        <div class="flex flex-col md:flex-row text-lg space-x-2 my-1 w-full">
          <strong class="w-full md:w-1/3 text-lg text-ashblue tracking-widest"
            >{$_('LAATIJA_PAATOIMINTAALUE')}:</strong>
          <span>{laatija['toimintaalue-nimi']}</span>
        </div>
        {#if laatija['muuttoimintaalueet-nimet'].length}
          <div class="flex flex-col md:flex-row text-lg space-x-2 my-1 w-full">
            <strong class="w-full md:w-1/3 text-lg text-ashblue tracking-widest"
              >{$_('LAATIJA_MUUT_TOIMINTAALUEET')}:</strong>
            <span>{laatija['muuttoimintaalueet-nimet'].join(', ')}</span>
          </div>
        {/if}
        {#if laatija.jakeluosoite}
          <div class="flex flex-col md:flex-row text-lg space-x-2 my-1 w-full">
            <strong class="w-full md:w-1/3 text-lg text-ashblue tracking-widest"
              >{$_('LAATIJA_OSOITE')}:</strong>
            <span>
              {laatija.jakeluosoite},
              {laatija.postinumero}
              {laatija.postitoimipaikka}
            </span>
          </div>
        {/if}
        {#if laatija.jakeluosoite === undefined && laatija.postinumero}
          <div class="flex flex-col md:flex-row text-lg space-x-2 my-1 w-full">
            <strong class="w-full md:w-1/3 text-lg text-ashblue tracking-widest"
              >{$_('LAATIJA_POSTINUMERO')}:</strong>
            <span>
              {laatija.postinumero}
              {laatija.postitoimipaikka}
            </span>
          </div>
        {/if}
        {#if laatija.wwwosoite}
          <div class="flex flex-col md:flex-row text-lg space-x-2 my-1 w-full">
            <strong class="w-full md:w-1/3 text-lg text-ashblue tracking-widest"
              >{$_('LAATIJA_WWW')}:</strong>
            <Link class="text-darkgreen underline" href={laatija.wwwosoite}
              >{laatija.wwwosoite}</Link>
          </div>
        {/if}
        {#if laatija.email}
          <div class="flex flex-col md:flex-row text-lg space-x-2 my-1 w-full">
            <strong class="w-full md:w-1/3 text-lg text-ashblue tracking-widest"
              >{$_('LAATIJA_EMAIL')}:</strong>
            <Link class="text-darkgreen underline" href="mailto:{laatija.email}"
              >{laatija.email}</Link>
          </div>
        {/if}
        {#if laatija.puhelin}
          <div class="flex flex-col md:flex-row text-lg space-x-2 my-1 w-full">
            <strong class="w-full md:w-1/3 text-lg text-ashblue tracking-widest"
              >{$_('LAATIJA_PUH')}:</strong>
            <Link class="text-darkgreen underline" href="tel:{laatija.puhelin}"
              >{laatija.puhelin}</Link>
          </div>
        {/if}
      </div>
    {:catch error}
      <div class="px-3 pb-8 lg:p-8 xl:p-16 w-full">{$_('SERVER_ERROR')}</div>
    {/await}
  </Container>
</div>
