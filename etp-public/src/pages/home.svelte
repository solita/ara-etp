<script>
  import { onMount } from 'svelte';
  import { slide } from 'svelte/transition';
  import { navigate } from '@/router/router';
  import { _, locale } from '@Localization/localization';

  import EtHakuImage from '@Asset/ethaku.jpg';
  import LaatijaHakuImage from '@Asset/laatijahaku.jpg';
  import ETMalli from '@Asset/energiatodistusmalli_2018.pdf';

  import BorderImage from '@Component/border-image';
  import Hero from '@Component/hero';
  import Button, { styles as buttonStyles } from '@Component/button';
  import ButtonLink from '@Component/buttonlink';
  import Input from '@Component/input-search';
  import Container, { styles as containerStyles } from '@Component/container';
  import Seo from '@Component/seo';

  import * as TilastotApi from '@/api/tilastot-api';
  import * as LaatijaApi from '@/api/laatija-api';

  import { announceAssertively } from '@/utilities/announce';

  announceAssertively($_('NAVBAR_ETUSIVU'));

  let etHakuId = '';
  let etHakuKeyword = '';

  let laatijahakuNimi = '';
  let laatijahakuAlue = '';

  let scrollOnAloita = null;

  const etCount = TilastotApi.statisticsCount(fetch).then(result => {
    return result.count;
  });
  const laatijatCount = LaatijaApi.laatijatCount(fetch).then(result => {
    return result.count;
  });

  onMount(() => {
    window.scrollTo({
      top: 0,
      left: 0,
      behavior: 'smooth'
    });
  });
</script>

<style>
  h3 {
    @apply uppercase tracking-wider font-bold;
  }
</style>

<Seo
  title="{$_('ENERGIATODISTUSREKISTERI')} - {$_('NAVBAR_ETUSIVU')}"
  descriptionFi={$locale == 'fi' ? $_('HERO_DESCRIPTION') : undefined}
  descriptionSv={$locale == 'sv' ? $_('HERO_DESCRIPTION') : undefined} />

<Hero
  buttonClick={() => scrollOnAloita.scrollIntoView({ behavior: 'smooth' })} />

<Container {...containerStyles.beige}>
  <article class="flex pb-16 px-2 sm:px-16">
    <div class="flex sm:-mx-16 flex-col lg:flex-row">
      <section
        class="lg:w-1/2 justify-between sm:px-16 flex flex-col flex-auto">
        <div
          class="flex justify-center lg:justify-start"
          bind:this={scrollOnAloita}>
          <BorderImage src={EtHakuImage} alt="" />
        </div>
        <div>
          <h2 class="mb-4">{$_('HOME_ENERGIATODISTUSHAKU')}</h2>
          <p>{$_('HOME_ENERGIATODISTUSHAKU_KUVAUS')}</p>
        </div>
        <div class="flex flex-col items-start">
          <form
            class="w-full"
            on:submit|preventDefault={_ => {
              const id = parseInt(etHakuId, 10);
              const idQuery = !isNaN(id)
                ? `where=${JSON.stringify([[['=', 'id', id]]])}`
                : '';
              const keywordQuery = etHakuKeyword.length
                ? `keyword=${etHakuKeyword}`
                : '';

              const query = [idQuery, keywordQuery]
                .filter(item => item.length)
                .join('&');

              navigate(
                ['/ethaku', query].filter(item => item.length).join('?')
              );
            }}>
            <h3 class="block pt-4 pb-1">
              {$_('HOME_ENERGIATODISTUSHAKU_TITLE')}
            </h3>
            <div class="w-4/5">
              <Input
                label={$_('HOME_ENERGIATODISTUSHAKU_TODISTUSTUNNUKSELLA')}
                value={etHakuId}
                on:change={evt => (etHakuId = evt.target.value)} />
            </div>
            <aside class="italic text-sm font-normal mt-4">
              {$_('HOME_ENERGIATODISTUSHAKU_ASIDE')}
            </aside>
            <div class="w-4/5">
              <Input
                label={$_('HOME_ENERGIATODISTUSHAKU_ALUEELLA')}
                value={etHakuKeyword}
                on:change={evt => (etHakuKeyword = evt.target.value)} />
            </div>
            <div class="mt-4">
              <Button
                ariaLabel="{$_('HOME_ENERGIATODISTUSHAKU_HAE')}}"
                {...buttonStyles.green}>{$_('HAE')}</Button>
            </div>
          </form>
        </div>
      </section>
      <section
        class="lg:w-1/2 justify-between sm:px-16 flex flex-col flex-auto mt-16
        lg:mt-0">
        <div class="flex justify-center lg:justify-start">
          <BorderImage src={LaatijaHakuImage} alt="" />
        </div>
        <div>
          <h2 class="mb-4">{$_('HOME_LAATIJAHAKU')}</h2>
          <p>{$_('HOME_LAATIJAHAKU_KUVAUS')}</p>
        </div>
        <div class="flex flex-col items-start">
          <form
            class="w-full"
            on:submit|preventDefault={_ => {
              const nameQuery = laatijahakuNimi.length
                ? `nimihaku=${laatijahakuNimi}`
                : '';
              const alueQuery = laatijahakuAlue.length
                ? `aluehaku=${laatijahakuAlue}`
                : '';

              const query = [nameQuery, alueQuery]
                .filter(item => item.length)
                .join('&');

              navigate(
                ['/laatijahaku', query].filter(item => item.length).join('?')
              );
            }}>
            <h3 class="block mt-4 pb-1">{$_('HOME_LAATIJAHAKU_TITLE')}</h3>
            <div class="w-4/5">
              <Input
                label={$_('HOME_LAATIJAHAKU_NIMELLA')}
                value={laatijahakuNimi}
                on:change={evt => (laatijahakuNimi = evt.target.value)} />
            </div>
            <aside class="italic text-sm font-normal mt-4">
              {$_('HOME_LAATIJAHAKU_ASIDE')}
            </aside>
            <div class="w-4/5">
              <Input
                label={$_('HOME_LAATIJAHAKU_ALUEELLA')}
                value={laatijahakuAlue}
                on:change={evt => (laatijahakuAlue = evt.target.value)} />
            </div>
            <div class="mt-4">
              <Button
                ariaLabel="{$_('HOME_LAATIJAHAKU_HAE')}}"
                {...buttonStyles.green}>{$_('HAE')}</Button>
            </div>
          </form>
        </div>
      </section>
    </div>
  </article>
</Container>
<Container {...containerStyles.ashbluewhite}>
  <article class="flex lg:flex-row flex-col">
    <section
      class="text-white bg-ashblue lg:w-1/3 py-4 sm:py-16 px-2 sm:px-16 lg:pr-8">
      <!-- <span class="font-icon text-6xl">info</span> -->
      <h2 class="mb-4 text-lg text-white">{$_('INFO_TITLE')}</h2>
      <p>{$_('INFO_KUVAUS')}</p>
      <div class="mt-4">
        <ButtonLink {...buttonStyles.white} href={ETMalli} target="_blank">
          <span class="material-icons" aria-hidden="true">picture_as_pdf</span>
          <span>{$_('INFO_ENERGIATODISTUSMALLI')}</span>
          <span class="sr-only">(pdf)</span>
        </ButtonLink>
      </div>
    </section>
    <section class="bg-white lg:w-2/3 lg:pl-8 px-2 sm:px-16 py-4 sm:py-16">
      <h2 class="mb-4 text-ashblue text-lg">{$_('LISATIETOA_TITLE')}</h2>
      <div class="flex flex-col md:flex-row md:space-x-10">
        <section class="md:w-1/2 py-4 md:py-0">
          <h3 class="mb-4">{$_('LISATIETOA_MOTIVAN_PALVELUT')}</h3>
          <div class="flex flex-col -my-2">
            <div class="py-2">
              <strong>{$_('LISATIETOA_KULUTTAJALLE')}</strong>
              <ul>
                <li>{$_('LISATIETOA_ENERGIATODISTUS')}</li>
                <li>{$_('LISATIETOA_LUKEMINEN')}</li>
                <li>{$_('LISATIETOA_TARVE')}</li>
                <li>{$_('LISATIETOA_FAQ')}</li>
              </ul>
            </div>
            <div class="py-2">
              <strong>{$_('LISATIETOA_AMMATTILAISILLE')}</strong>
              <ul>
                <li>{$_('LISATIETOA_LAATIJOILLE')}</li>
                <li>{$_('LISATIETOA_PATEVYYS')}</li>
              </ul>
            </div>
            <a
              href="https://www.motiva.fi/ratkaisut/energiatodistusneuvonta"
              class="block py-2 underline text-darkgreen">
              {$_('LISATIETOA_MOTIVA_LINKKI')}
            </a>
          </div>
        </section>
        <section class="md:w-1/2 py-4 md:py-0">
          <h3 class="mb-4">{$_('LISATIETOA_TUNNUSLUVUT')}</h3>
          <div class="flex flex-col space-y-2">
            {#await laatijatCount then count}
              <div
                class="flex space-x-2"
                transition:slide|local={{ duration: 200 }}>
                <span>{$_('LISATIETOA_LAATIJOITA')}</span>
                <span>{count}</span>
              </div>
            {/await}
            {#await etCount then count}
              <div
                class="flex space-x-2"
                transition:slide|local={{ duration: 200 }}>
                <span>{$_('LISATIETOA_ENERGIATODISTUKSIA')}</span>
                <span>{count}</span>
              </div>
            {/await}
          </div>
          <div class="flex flex-col">
            <p class="block py-2">{$_('LISATIETOA_ARAN_JULKAISUT')}</p>
            <a href="/tilastot" class="block py-2 underline text-darkgreen">
              {$_('LISATIETOA_TILASTOT_LINKKI')}
            </a>
          </div>
        </section>
      </div>
    </section>
  </article>
</Container>
