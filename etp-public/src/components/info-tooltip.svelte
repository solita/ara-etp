<script context="module">
  export const AnchorPosition = {
    topLeft: 'top-left',
    topRight: 'top-right',
    bottomLeft: 'bottom-left',
    bottomRight: 'bottom-right'
  };
</script>

<script>
  import InfoIcon from '@Asset/icons/info_green.svg';
  import InfoIconHover from '@Asset/icons/info_green_hover.svg';

  export let anchor = AnchorPosition.bottomLeft;
  export let title = '';
  export let tooltip = '';

  let open = false;

  $: [vertical, horizontal] = anchor.split('-');
</script>

<style>
  .info-popup {
    display: none;
    max-width: 36em;
    min-width: 24em;
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
    left: 1rem;
  }
  .right::after {
    right: 1rem;
  }

  @media print {
    .info-container {
      display: none;
    }
  }
</style>

<!-- purgecss: top bottom right left -->
<div class="flex items-center space-x-1">
  <slot />
  {#if tooltip.length}
    <div
      class="relative info-container hidden md:block"
      aria-haspopup="true"
      on:mouseover={() => (open = true)}
      on:mouseout={() => (open = false)}>
      <span class="relative inline-block md:block">
        <img alt="" class="w-5 h-5" src={open ? InfoIconHover : InfoIcon} />
      </span>
      <div class="info-popup {vertical} {horizontal}">
        <strong>{title}</strong>
        {#each tooltip.split('\n') as paragraph}
          <p>{paragraph}</p>
        {/each}
        <slot name="custom" />
      </div>
    </div>
  {/if}
</div>
