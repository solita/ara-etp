<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as MD from '@Component/text-editor/markdown';
  import * as Schema from './schema';

  import { _ } from '@Language/i18n';

  import TypeForm from './type-form.svelte';

  const i18n = $_;
  const i18nRoot = 'valvonta.oikeellisuus.virhetypes';

  export let virhetypes;
  export let newVirhetype;
  export let api;
  export let dirty;

  const closeNew = _ => {
    if (!dirty) {
      newVirhetype = Maybe.None();
    }
  };

  $: if (Maybe.isSome(newVirhetype)) {
    closeEdit();
  }

  let virheInEditMode = Maybe.None();
  const closeEdit = _ => {
    if (!dirty) {
      virheInEditMode = Maybe.None();
    }
  };

  const edit = virhetype => {
    if (!dirty) {
      virheInEditMode = Maybe.Some(virhetype.id);
    }
  }

  const toForm = R.evolve({
    id: Maybe.Some,
    ordinal: Either.Right
  });

  const serialize = R.evolve({ id: Maybe.get, ordinal: Either.right })

  const editApi = {
    save: virhetype => api.save(serialize(virhetype), api.reload),
    cancel: api.reload,
    close: closeEdit
  }

  const addApi = {
    save: virhetype => api.add(serialize(R.dissoc('id', virhetype)), api.reload),
    cancel: _ => {
      newVirhetype = Maybe.Some(Schema.newVirhetype);
      dirty = false;
    },
    close: closeNew
  }
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
  {#each Maybe.toArray(newVirhetype) as virhetype}
    <tr class="etp-table--tr">
      <td colspan="4">
        <TypeForm virhetype={R.assoc('id', Maybe.None(), toForm(virhetype))}
                  bind:dirty
                  api = {addApi} />
      </td>
    </tr>
  {/each}
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
                    api = {editApi} />
        </td>
      </tr>
    {/if}
  {/each}
  </tbody>
</table>
