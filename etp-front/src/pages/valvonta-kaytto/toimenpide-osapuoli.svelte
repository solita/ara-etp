<script>
  import { onMount } from 'svelte';
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Osapuolet from './osapuolet';
  import * as Toimenpiteet from './toimenpiteet';

  import { _, locale } from '@Language/i18n';
  import * as Locales from '@Language/locale-utils';

  import Link from '@Component/Link/Link.svelte';

  import * as valvontaApi from './valvonta-api';

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.valvonta.toimenpide';

  export let valvonta;
  export let toimenpide;
  export let roolit;
  export let toimitustavat;
  export let osapuoli;
  export let type;

  const types = {
    henkilo: {
      pdf: valvontaApi.url.documentHenkilo,
      courtAttachment: valvontaApi.url.courtAttachmentHenkilo
    },
    yritys: {
      pdf: valvontaApi.url.documentYritys,
      courtAttachment: valvontaApi.url.courtAttachmentYritys
    }
  };

  $: rooliLabel = R.compose(
    Maybe.fold(
      i18n(i18nRoot + '.rooli-none'),
      Locales.labelForId($locale, roolit)
    ),
    R.prop('rooli-id')
  );

  $: toimitustapaLabel = R.compose(
    Maybe.fold(
      i18n(i18nRoot + '.toimitustapa-none'),
      Locales.labelForId($locale, toimitustavat)
    ),
    R.prop('toimitustapa-id')
  );

  let node;
  let truncate = true;

  let truncated;

  onMount(() => {
    truncated = !!node && node.offsetWidth < node.scrollWidth;
  });
</script>

{#if Osapuolet.isOmistaja(osapuoli)}
  {#if Toimenpiteet.hasCourtAttachment(toimenpide) && Toimenpiteet.osapuoliHasHallintoOikeus(toimenpide, osapuoli)}
    (<Link
      text="pdf"
      target={'_blank'}
      href={types[type].pdf(
        osapuoli.id,
        valvonta.id,
        toimenpide.id,
        toimenpide.filename
      )} />,
    <Link
      text={i18n(i18nRoot + '.court-attachment')}
      target={'_blank'}
      href={types[type].courtAttachment(
        osapuoli.id,
        valvonta.id,
        toimenpide.id
      )} />),
  {:else}
    (<Link
      text="pdf"
      target={'_blank'}
      href={types[type].pdf(
        osapuoli.id,
        valvonta.id,
        toimenpide.id,
        toimenpide.filename
      )} />),
  {/if}
{:else if Toimenpiteet.sendTiedoksi(toimenpide)}
  ({i18n(i18nRoot + '.fyi')}),
{:else}
  ({i18n(i18nRoot + '.fyi-disabled')})
{/if}
{rooliLabel(osapuoli)}, {toimitustapaLabel(osapuoli)}
