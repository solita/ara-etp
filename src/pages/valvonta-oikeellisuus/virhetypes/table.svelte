<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as MD from '@Component/text-editor/markdown';

  import { _ } from '@Language/i18n';

  import TypeForm from './type-form.svelte';

  const i18n = $_;
  const i18nRoot = 'valvonta.oikeellisuus.virhetypes';

  export let virhetypes;
  export let api;
  export let dirty;

  let virheInEditMode = Maybe.None();
  const close = _ => {
    if (!dirty) {
      virheInEditMode = Maybe.None();
    }
  };

  const edit = virhetype => {
    if (!dirty) {
      virheInEditMode = Maybe.Some(virhetype.id);
    }
  }

  const toForm = R.evolve({ ordinal: Either.Right });
</script>

<style>
</style>

<table class="etp-table">
  <thead class="etp-table--thead">
  <tr class="etp-table--tr">
    <th class="etp-table--th">{i18n(i18nRoot + '.label-fi')}</th>
    <th class="etp-table--th">{i18n(i18nRoot + '.label-sv')}</th>
    <th class="etp-table--th">{i18n(i18nRoot + '.description-fi')}</th>
    <th class="etp-table--th">{i18n(i18nRoot + '.description-sv')}</th>
  </tr>
  </thead>
  <tbody class="etp-table--tbody">
  {#each virhetypes as virhetype (virhetype.id)}
    {#if !Maybe.exists(R.equals(virhetype.id), virheInEditMode)}
      <tr class="etp-table--tr" class:etp-table--tr__link={!dirty}
          on:click={_ => edit(virhetype)}>

          <td class="etp-table--td">
            {@html MD.toHtml(virhetype['label-fi'])}
          </td>
          <td class="etp-table--td">
            {@html MD.toHtml(virhetype['label-sv'])}
          </td>
          <td class="etp-table--td">
            {@html MD.toHtml(virhetype['description-fi'])}
          </td>
          <td class="etp-table--td">
            {@html MD.toHtml(virhetype['description-sv'])}
          </td>
      </tr>
    {:else}
      <tr class="etp-table--tr">
        <td colspan="4">
          <TypeForm virhetype={toForm(virhetype)}
                    bind:dirty
                    api = {R.assoc('close', close, api)} />
        </td>
      </tr>
    {/if}
  {/each}
  </tbody>
</table>
