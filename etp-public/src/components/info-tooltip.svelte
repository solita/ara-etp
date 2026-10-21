<script context="module">
  export const AnchorPosition = {
    topLeft: 'top-left',
    topRight: 'top-right',
    bottomLeft: 'bottom-left',
    bottomRight: 'bottom-right'
  };
</script>

<script>
  import { tick } from 'svelte';
  import InfoIcon from '@Asset/icons/info_green.svg';
  import InfoIconHover from '@Asset/icons/info_green_hover.svg';

  export let anchor = AnchorPosition.bottomLeft;
  export let title = '';
  export let tooltip = '';
  export let ariaLabel = '';

  let open = false;
  let popup;
  let shiftX = 0;

  $: [vertical, horizontal] = anchor.split('-');

  async function showTooltip() {
    open = true;
    await tick();
    adjustPosition();
  }

  function hideTooltip() {
    open = false;
    shiftX = 0;
  }

  function adjustPosition() {
    if (!open || !popup) return;

    const margin = 32;
    const bounds = popup.getBoundingClientRect();
    shiftX = Math.max(
      margin - bounds.left,
      Math.min(0, window.innerWidth - margin - bounds.right)
    );
  }
</script>

<style>
  .info-popup {
    display: none;
    max-width: 36em;
    min-width: 24em;
    transform: translateX(var(--tooltip-shift-x, 0px));
  }

  .info-container:hover .info-popup,
  .info-container:focus .info-popup,
  .info-container:active .info-popup {
    @apply block absolute z-10 bg-white text-black border border-black rounded-lg p-2;
  }

  .bottom {
    bottom: 135%;
  }

  .top {
    top: 135%;
  }

  .left {
    left: -1rem;
  }

  .right {
    right: -1rem;
  }

  .info-popup::after {
    content: '';
    position: absolute;
    border-style: solid;
    border-width: 10px 10px 0;
    border-color: black transparent;
    display: block;
    width: 0;
    z-index: 10;
  }

  .info-popup p {
    @apply my-2;
  }

  .top::after {
    transform: rotate(180deg);
    top: -10px;
  }

  .bottom::after {
    bottom: -10px;
  }
  .left::after {
    left: calc(1rem - var(--tooltip-shift-x, 0px));
  }
  .right::after {
    right: calc(1rem + var(--tooltip-shift-x, 0px));
  }

  @media print {
    .info-container {
      display: none;
    }
  }
</style>

<svelte:window on:resize={adjustPosition} />

<div class="flex items-center space-x-1">
  <slot />
  {#if tooltip.length}
    <div
      class="relative info-container hidden md:block"
      role="button"
      tabindex="0"
      aria-haspopup="true"
      aria-label={ariaLabel || `${title} - ${tooltip}`}
      on:mouseenter={showTooltip}
      on:mouseleave={hideTooltip}
      on:focus={showTooltip}
      on:blur={hideTooltip}>
      <span class="relative inline-block md:block">
        <slot name="icon">
          <img alt="" class="w-5 h-5" src={open ? InfoIconHover : InfoIcon} />
        </slot>
      </span>
      <div
        bind:this={popup}
        class="info-popup {vertical} {horizontal}"
        role="tooltip"
        style={`--tooltip-shift-x: ${shiftX}px`}>
        <strong>{title}</strong>
        {#each tooltip.split('\n') as paragraph}
          <p>{paragraph}</p>
        {/each}
        <slot name="custom" />
      </div>
    </div>
  {/if}
</div>
