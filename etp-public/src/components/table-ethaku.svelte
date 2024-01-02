<script>
  import { _, locale } from '@Localization/localization';
  import { parseDate } from '@/utilities/parsers';
  import * as formats from '@/utilities/formats';
  export let etCount;
  export let eTodistukset;
  export let postinumerot;
  export let kayttotarkoitusluokat;

  $: currentPageItemCount = eTodistukset.length;

  const findPostitoimipaikka = postinumero => {
    let foundPostinumero = postinumerot.find(
      postinum => postinum.id === parseInt(postinumero)
    );
    if (foundPostinumero) return foundPostinumero;
    else return '';
  };

  const findKayttotarkoitusluokka = (kt, versio) => {
    let aktluokat;
    if (parseInt(versio) === 2013)
      aktluokat = kayttotarkoitusluokat['2013'].alakayttotarkoitusluokat;
    else if (parseInt(versio) === 2018)
      aktluokat = kayttotarkoitusluokat['2018'].alakayttotarkoitusluokat;
    else return '';

    let foundKayttotarkoitusluokka = aktluokat.find(
      aktluokka => aktluokka.id === kt
    );
    if (foundKayttotarkoitusluokka) return foundKayttotarkoitusluokka;
    else return '';
  };

  const selectShownKatuosoite = (l, perustiedot) => {
    if (l == 'sv' && perustiedot['katuosoite-sv'])
      return perustiedot['katuosoite-sv'];
    else if (l == 'fi' && !perustiedot['katuosoite-fi'])
      return perustiedot['katuosoite-sv'];
    else return perustiedot['katuosoite-fi'];
  };

  let ref = window.history.state.path.includes('?')
    ? `&ref=${encodeURIComponent(window.history.state.path.split('?')[1])}`
    : '';
</script>

<style>
  /* Table styling in main.css */
  td {
    @apply p-0;
  }
  td a {
    @apply px-4 py-2;
  }
</style>

<div class="table-container">
  {#if etCount < 1}
    <span>{$_('HAKU_TULOKSIA_EI')}</span>
  {:else}
    <h2>{$_('HAKU_TULOKSIA')} {etCount}</h2>
    {#if currentPageItemCount > 0}
      <div class="w-full overflow-auto">
        <table class="w-full table-auto text-left my-2">
          <thead>
            <tr>
              <th>{$_('ETHAKU_TH_TUNNUS')}</th>
              <th>{$_('ETHAKU_TH_ETLUOKKA')}</th>
              <th>{$_('ETHAKU_TH_OSOITE')}</th>
              <th>{$_('ETHAKU_TH_KAYTTOTARKOITUS')}</th>
              <th>{$_('ETHAKU_TH_VOIMASSA')}</th>
            </tr>
          </thead>
          <tbody>
            {#each eTodistukset as todistus}
              <tr>
                <td>
                  <a
                    class="block"
                    href={`/energiatodistus?id=${todistus.id}&versio=${todistus.versio}${ref}`}
                    ><span class="m-title">{$_('ETHAKU_TH_TUNNUS')}</span>
                    {todistus.id}</a>
                </td>
                <td>
                  <a
                    class="block"
                    href={`/energiatodistus?id=${todistus.id}&versio=${todistus.versio}${ref}`}
                    ><span class="m-title">{$_('ETHAKU_TH_ETLUOKKA')}</span>

                    <strong>{todistus.tulokset['e-luokka']}</strong>
                    <span class="text-xs">{todistus.versio}</span>
                  </a>
                </td>
                <td>
                  <a
                    class="block"
                    href={`/energiatodistus?id=${todistus.id}&versio=${todistus.versio}${ref}`}
                    ><span class="m-title">{$_('ETHAKU_TH_OSOITE')}</span>
                    {`${selectShownKatuosoite($locale, todistus.perustiedot)}, 
                  ${todistus.perustiedot.postinumero} 
                  ${
                    $locale == 'sv'
                      ? findPostitoimipaikka(todistus.perustiedot.postinumero)[
                          'label-sv'
                        ]
                      : findPostitoimipaikka(todistus.perustiedot.postinumero)[
                          'label-fi'
                        ]
                  }
                  `}
                  </a></td>
                <td>
                  <a
                    class="block"
                    href={`/energiatodistus?id=${todistus.id}&versio=${todistus.versio}${ref}`}
                    ><span class="m-title"
                      >{$_('ETHAKU_TH_KAYTTOTARKOITUS')}</span>
                    {$locale == 'sv'
                      ? findKayttotarkoitusluokka(
                          todistus.perustiedot.kayttotarkoitus,
                          todistus.versio
                        )['label-sv']
                      : findKayttotarkoitusluokka(
                          todistus.perustiedot.kayttotarkoitus,
                          todistus.versio
                        )['label-fi']}
                  </a>
                </td>
                <td class="text-center">
                  <a
                    class="block"
                    href={`/energiatodistus?id=${todistus.id}&versio=${todistus.versio}${ref}`}
                    ><span class="m-title">{$_('ETHAKU_TH_VOIMASSA')}</span>
                    {formats.formatDate(parseDate(todistus?.allekirjoitusaika))}
                    - {formats.formatExclusiveEndDate(
                      parseDate(todistus['voimassaolo-paattymisaika'])
                    )}
                  </a>
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    {/if}
  {/if}
  <slot name="pagination" currentPageItemCount={eTodistukset.length} />
</div>
