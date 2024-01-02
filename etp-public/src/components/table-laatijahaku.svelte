<script>
  import { _ } from '@Localization/localization';

  export let laatijaCount;
  export let laatijat;

  $: currentPageItemCount = laatijat.length;
  $: ref = window.history.state.path.includes('?')
    ? `&ref=${encodeURIComponent(window.history.state.path.split('?')[1])}`
    : '';
</script>

<style>
  /* Table styling in main.css */
</style>

<div class="table-container">
  {#if laatijaCount < 1}
    <span>{$_('HAKU_TULOKSIA_EI')}</span>
    <slot name="filter" />
  {:else}
    <h2>{$_('HAKU_TULOKSIA')} {laatijaCount}</h2>
    <slot name="filter" />
    {#if currentPageItemCount > 0}
      <div class="w-full overflow-auto">
        <table class="w-full table-auto text-left my-2">
          <thead>
            <tr>
              <th>{$_('LHAKU_TH_NIMI')}</th>
              <th>{$_('LHAKU_TH_PATEVYYS')}</th>
              <th>{$_('LHAKU_TH_PAATOIMINTAALUE')}</th>
              <th>{$_('LHAKU_TH_POSTITOIMIPAIKKA')}</th>
              <th>{$_('LHAKU_TH_WWW')}</th>
              <th>{$_('LHAKU_TH_EMAIL')}</th>
              <th>{$_('LHAKU_TH_PUH')}</th>
            </tr>
          </thead>
          <tbody>
            {#each laatijat as laatija}
              <tr>
                <td>
                  <a
                    class="text-darkgreen text-xl flex items-center md:text-base"
                    href="/laatija?id={laatija.id}{ref}">
                    <span class="underline md:no-underline"
                      >{laatija.nimi || '-'}</span>
                    <span
                      class="material-icons px-1 md:hidden"
                      aria-hidden="true">navigate_next</span>
                  </a>
                </td>
                <td>
                  <span class="m-title">{$_('LHAKU_TH_PATEVYYS')}</span>
                  <span>{laatija.patevyys || '-'}</span>
                </td>
                <td>
                  <span class="m-title">{$_('LHAKU_TH_PAATOIMINTAALUE')}</span>
                  <span>{laatija['toimintaalue-nimi'] || '-'}</span>
                </td>
                <td>
                  <span class="m-title">{$_('LHAKU_TH_POSTITOIMIPAIKKA')}</span>
                  <span>{laatija.postitoimipaikka || '-'}</span>
                </td>
                <td class="text-center">
                  <span class="m-title">{$_('LHAKU_TH_WWW')}</span>
                  {#if laatija.wwwosoite}
                    <a href={laatija.wwwosoite} title={laatija.wwwosoite}>
                      <span
                        class="material-icons text-darkgreen"
                        aria-hidden="true">
                        public
                      </span>
                    </a>
                  {:else}<span>-</span>{/if}
                </td>
                <td class="text-center">
                  <span class="m-title">{$_('LHAKU_TH_EMAIL')}</span>
                  {#if laatija.email}
                    <a href="mailto:{laatija.email}" title={laatija.email}>
                      <span
                        class="material-icons text-darkgreen"
                        aria-hidden="true">
                        alternate_email
                      </span>
                    </a>
                  {:else}<span>-</span>{/if}
                </td>
                <td>
                  <span class="m-title">{$_('LHAKU_TH_PUH')}</span>
                  {#if laatija.puhelin}
                    <a
                      class="inline-flex"
                      href="tel:{laatija.puhelin}"
                      title={laatija.puhelin}>
                      <span
                        class="material-icons text-darkgreen"
                        aria-hidden="true">
                        call
                      </span>
                      <span class="text-darkgreen">{laatija.puhelin}</span>
                    </a>
                  {:else}<span>-</span>{/if}
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    {/if}
  {/if}
  <slot name="pagination" currentPageItemCount={laatijat.length} />
</div>
