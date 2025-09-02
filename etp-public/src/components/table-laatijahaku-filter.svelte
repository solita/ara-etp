<script>
  import { _, locale, labelLocale } from '@Localization/localization';
  import { createEventDispatcher } from 'svelte';

  const dispatch = createEventDispatcher();

  export let patevyydet;
  export let showPatevyydet = '1,2,3,4';

  const configPromise = fetch('config.json').then(response => response.json());

  // Individual toggle states for ETP 2026 filters
  let ylempitasoToggle = false;
  let pppToggle = false;
  let isUpdatingFromProp = false;

  // Lookup table for toggle combinations
  const toggleMappings = {
    // From toggle states to IDs
    'false,false': '1,2,3,4',
    'true,false': '2,4',
    'false,true': '3,4',
    'true,true': '4'
  };

  // Reverse mapping from IDs to toggle states
  const reverseMappings = Object.fromEntries(
    Object.entries(toggleMappings).map(([key, value]) => [value, key])
  );

  // Function to update toggle states from showPatevyydet
  const updateTogglesFromProp = value => {
    isUpdatingFromProp = true;
    const sortedValue = value.split(',').sort().join(',');
    const toggleState = reverseMappings[sortedValue];

    if (toggleState) {
      const [first, second] = toggleState.split(',');
      ylempitasoToggle = first === 'true';
      pppToggle = second === 'true';
    }
    isUpdatingFromProp = false;
  };

  // Function to update showPatevyydet from toggle states
  const updatePropFromToggles = () => {
    if (isUpdatingFromProp) return;

    const toggleKey = `${ylempitasoToggle},${pppToggle}`;
    const newValue = toggleMappings[toggleKey];

    if (newValue) {
      showPatevyydet = newValue;
      dispatch('change', newValue);
    }
  };

  // Watch for external changes to showPatevyydet
  $: if (showPatevyydet && showPatevyydet !== 'on' && !isUpdatingFromProp) {
    updateTogglesFromProp(showPatevyydet);
  }

  // Watch toggle changes and update showPatevyydet
  $: ylempitasoToggle,
    pppToggle,
    (() => {
      if (!isUpdatingFromProp) {
        updatePropFromToggles();
      }
    })();
</script>

<style>
  .info-popup {
    display: none;
  }

  .icon-container {
    @apply relative;
  }

  .icon-container:hover .info-popup,
  .icon-container:focus .info-popup {
    bottom: 135%;
    left: -1rem;
    @apply block absolute z-10 bg-white text-black border border-black rounded-lg p-2 whitespace-pre;
  }

  .info-popup::after {
    content: '';
    position: absolute;
    border-style: solid;
    border-width: 10px 10px 0;
    border-color: black transparent;
    display: block;
    width: 0;
    z-index: 1;
    bottom: -10px;
    left: 1rem;
  }

  @supports (-moz-appearance: none) {
    .info-popup::after {
      bottom: -35px;
    }
  }

  .radio-container {
    padding-left: 25px;
    @apply select-none block relative;
  }

  .radio-container input {
    @apply absolute opacity-0 cursor-pointer select-none outline-none pointer-events-none;
  }
  .radio-container .radio-visual {
    top: 0.1em;
    left: 0;
    height: 20px;
    width: 20px;
    border-radius: 50%;
    @apply bg-lightgrey absolute;
  }

  .radio-container input:focus ~ .radio-visual,
  .radio-container:focus input ~ .radio-visual,
  .radio-container:hover input ~ .radio-visual {
    @apply bg-darkgrey;
  }
  .radio-container input:checked:focus ~ .radio-visual,
  .radio-container:focus input:focus ~ .radio-visual {
    @apply bg-black;
  }
  .radio-container input:checked ~ .radio-visual {
    @apply bg-green;
  }
  .radio-visual:after {
    content: '';
    @apply absolute hidden;
  }
  .radio-container input:checked ~ .radio-visual:after {
    @apply block;
  }
  .radio-container .radio-visual:after {
    top: 7px;
    left: 7px;
    width: 6px;
    height: 6px;
    border-radius: 50%;
    @apply bg-white;
  }

  /* Toggle styles for ETP 2026 */
  .toggle-container {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    cursor: pointer;
  }

  .toggle-switch {
    position: relative;
    display: inline-block;
    width: 3rem;
    height: 1.5rem;
    background-color: #d1d5db;
    border-radius: 9999px;
    transition: all 0.2s ease-in-out;
  }

  .toggle-switch.active {
    background-color: #538000;
  }

  .toggle-switch::after {
    content: '';
    position: absolute;
    top: 0.25rem;
    left: 0.25rem;
    width: 1rem;
    height: 1rem;
    background-color: white;
    border-radius: 9999px;
    transition: all 0.2s ease-in-out;
  }

  .toggle-switch.active::after {
    transform: translateX(1.5rem);
  }

  .toggle-label {
    font-size: 0.875rem;
    font-weight: 500;
  }

  .checkbox-container input:focus ~ .toggle-switch {
    outline: 2px solid #538000;
    outline-offset: 2px;
  }

  .checkbox-container input {
    position: absolute;
    opacity: 0;
    cursor: pointer;
    pointer-events: none;
  }
</style>

{#await configPromise then config}
  <div data-filter-component>
    {#if config?.isEtp2026}
      <!-- ETP 2026 version with toggle controls -->
      <fieldset class="flex flex-row gap-6 items-center">
        <legend class="sr-only">{$_('LAATIJA_PATEVYYSTASO')}:</legend>

        <!-- Ylempi taso toggle -->
        <label class="checkbox-container toggle-container">
          <input type="checkbox" bind:checked={ylempitasoToggle} />
          <div class="toggle-switch {ylempitasoToggle ? 'active' : ''}"></div>
          <span class="toggle-label">{$_('LHAKU_TOGGLE_YLEMPI_TASO')}</span>
        </label>

        <!-- PPP toggle -->
        <label class="checkbox-container toggle-container">
          <input type="checkbox" bind:checked={pppToggle} />
          <div class="toggle-switch {pppToggle ? 'active' : ''}"></div>
          <span class="toggle-label">{$_('LHAKU_TOGGLE_PPP')}</span>
        </label>
      </fieldset>
    {:else}
      <!-- Original version without PPP support -->
      <fieldset class="flex flex-col md:flex-row">
        <legend class="sr-only">{$_('LAATIJA_PATEVYYSTASO')}:</legend>
        <div class="flex items-start space-x-1 py-3 md:py-0 mr-3">
          <label class="radio-container">
            <input
              id="kaikkitasot"
              type="radio"
              bind:group={showPatevyydet}
              value={'1,2'}
              on:change />
            <span class="radio-visual" />
            {$_('LHAKU_FILTER_KAIKKI')}
          </label>

          <div class="icon-container hidden md:block" tabindex="0">
            <span class="material-icons text-green" aria-hidden="true"
              >error_outline</span>
            <div class="info-popup">
              <strong>{$_('LHAKU_KAIKKI_TITLE')}</strong>
              <p>{$_('LHAKU_KAIKKI_TEXT')}</p>
            </div>
          </div>
        </div>
        <div class="flex items-start space-x-1 py-3 md:py-0 mr-3">
          <label class="radio-container">
            <input
              id="perustaso"
              type="radio"
              bind:group={showPatevyydet}
              value={'1'}
              on:change />
            <span class="radio-visual" />
            {labelLocale(
              $locale,
              patevyydet.find(p => p.id === 1)
            )}
          </label>
          <div class="icon-container hidden md:block" tabindex="0">
            <span class="material-icons text-green" aria-hidden="true"
              >error_outline</span>
            <div class="info-popup">
              <strong>{$_('LHAKU_PERUSTASO_TITLE')}</strong>
              <p>{$_('LHAKU_PERUSTASO_TEXT')}</p>
            </div>
          </div>
        </div>
        <div class="flex items-start space-x-1 py-3 md:py-0">
          <label class="radio-container">
            <input
              id="ylempitaso"
              type="radio"
              bind:group={showPatevyydet}
              value={'2'}
              on:change />
            <span class="radio-visual" />
            {labelLocale(
              $locale,
              patevyydet.find(p => p.id === 2)
            )}
          </label>
          <div class="icon-container hidden md:block" tabindex="0">
            <span class="material-icons text-green" aria-hidden="true"
              >error_outline</span>
            <div class="info-popup">
              <strong>{$_('LHAKU_YLEMPITASO_TITLE')}</strong>
              <p>{$_('LHAKU_YLEMPITASO_TEXT')}</p>
            </div>
          </div>
        </div>
      </fieldset>
    {/if}
  </div>
{/await}
