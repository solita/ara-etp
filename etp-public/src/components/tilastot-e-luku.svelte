<script>
  import { _ } from '@Localization/localization';
  import InfoTooltip, { AnchorPosition } from '@Component/info-tooltip';

  export let data;
  export let tooltip = '';
  export let tooltipAnchorPosition = AnchorPosition.bottomLeft;
</script>

<div class="w-full flex flex-col">
  <div class="mt-4 mb-2">
    <InfoTooltip
      anchor={tooltipAnchorPosition}
      title={$_('TILASTOT_E_LUKU')}
      {tooltip}>
      <h2>{$_('TILASTOT_E_LUKU')}</h2>
    </InfoTooltip>
  </div>

  <dl>
    <div class="w-full flex justify-between">
      <dt>{$_('TILASTOT_KESKIARVO')}</dt>
      <dd>{data?.avg.toFixed(0) || 0}{$_('TILASTOT_E_LUKU_UNIT')}</dd>
    </div>
    {#if data?.['percentile-15']}
      <div class="w-full flex justify-between">
        <dt>{$_('TILASTOT_PARAS_15')}</dt>
        <dd>
          {$_('TILASTOT_ALLE')}
          {data?.['percentile-15'].toFixed(0) || 0}{$_('TILASTOT_E_LUKU_UNIT')}
        </dd>
      </div>
    {/if}
    {#if data?.['percentile-85']}
      <div class="w-full flex justify-between">
        <dt>{$_('TILASTOT_HEIKOIN_15')}</dt>
        <dd>
          {$_('TILASTOT_YLI')}
          {data?.['percentile-85'].toFixed(0) || 0}{$_('TILASTOT_E_LUKU_UNIT')}
        </dd>
      </div>
    {/if}
  </dl>
</div>
