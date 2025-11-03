<script>
  import H4 from '@Component/H/H4';
  import { _ } from '@Language/i18n';
  import Input from '@Pages/energiatodistus/Input';

  export let perusparannuspassi;
  export let schema;
</script>

<H4 text={$_('perusparannuspassi.laskennan-tulokset.vaiheistus.header')} />

<p>
  {$_('perusparannuspassi.laskennan-tulokset.vaiheistus.info')}
  <span class="font-bold"
    >{$_(
      'perusparannuspassi.laskennan-tulokset.vaiheistus.info-aloitusvuosi'
    )}</span>
  .
</p>

<div>
  <table class="et-table w-auto overflow-x-auto md:overflow-x-hidden">
    <thead class="et-table--thead">
      <tr class="et-table--tr">
        <th
          class="et-table--th et-table--th-left-aligned et-table--th__twocells"
          >{$_('perusparannuspassi.laskennan-tulokset.vaihe')}</th>
        <th
          class="et-table--th et-table--th-left-aligned et-table--th__twocells"
          >{$_('perusparannuspassi.laskennan-tulokset.aloitusvuosi')}</th>
      </tr>
    </thead>
    <tbody class="et-table--tbody">
      {#each perusparannuspassi.vaiheet as vaihe}
        <tr class="et-table--tr">
          <td class="et-table--td">Vaihe {vaihe['vaihe-nro']}</td>
          <td class="et-table--td">
            <Input
              {schema}
              center={false}
              bind:model={perusparannuspassi}
              compact={true}
              i18nRoot="perusparannuspassi"
              path={[
                'vaiheet',
                vaihe['vaihe-nro'] - 1,
                'tulokset',
                'vaiheen-alku-pvm'
              ]} />
          </td>
        </tr>
      {/each}
    </tbody>
  </table>
</div>
