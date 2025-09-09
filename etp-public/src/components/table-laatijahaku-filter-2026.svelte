<script>
  import { _ } from '@Localization/localization';
  import { createEventDispatcher, tick } from 'svelte';

  const dispatch = createEventDispatcher();

  export let showPatevyydet = '1,2,3,4';

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

  const reverseMappings = Object.fromEntries(
    Object.entries(toggleMappings).map(([key, value]) => [value, key])
  );

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

  const updatePropFromToggles = () => {
    if (isUpdatingFromProp) return;

    const toggleKey = `${ylempitasoToggle},${pppToggle}`;
    const newValue = toggleMappings[toggleKey];

    if (newValue) {
      showPatevyydet = newValue;
      dispatch('change', newValue);
    }
  };

  $: if (showPatevyydet && showPatevyydet !== 'on' && !isUpdatingFromProp) {
    updateTogglesFromProp(showPatevyydet);
  }

  $: ylempitasoToggle,
    pppToggle,
    (() => {
      if (!isUpdatingFromProp) {
        updatePropFromToggles();
      }
    })();
</script>

<style>
  /* Toggle styles for ETP 2026 */
  .toggle-container {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    cursor: pointer;
    position: relative;
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

  .checkbox-container input {
    position: absolute;
    top: 0;
    left: 0;
    width: 3rem;
    height: 1.5rem;
    opacity: 0;
    cursor: pointer;
    margin: 0;
    z-index: 1;
  }

  .checkbox-container input:focus {
    outline: none;
  }

  .checkbox-container input:focus ~ .toggle-switch,
  .checkbox-container input:focus-visible ~ .toggle-switch {
    outline: 2px solid #538000;
    outline-offset: 2px;
  }
</style>

<div class="px-3 lg:px-8 xl:px-16 pb-8 flex flex-col w-full">
  <fieldset class="flex flex-row gap-6 items-center pt-3">
    <legend class="font-bold">{$_('LHAKU_PATEVYYS_LABEL')}</legend>
    <label class="checkbox-container toggle-container">
      <input
        type="checkbox"
        bind:checked={ylempitasoToggle}
        role="switch"
        aria-checked={ylempitasoToggle}
        aria-describedby="ylempi-help"
        on:keydown={event => {
          if (event.code === 'Space') {
            event.preventDefault();
            event.stopPropagation();
            ylempitasoToggle = !ylempitasoToggle;
          }
        }}
        on:click={event => {
          event.preventDefault();
          ylempitasoToggle = !ylempitasoToggle;
        }} />
      <div
        class="toggle-switch {ylempitasoToggle ? 'active' : ''}"
        aria-hidden="true">
      </div>
      <span class="toggle-label">{$_('LHAKU_TOGGLE_YLEMPI_TASO')}</span>
    </label>
    <span id="ylempi-help" class="sr-only">
      {ylempitasoToggle ? $_('COMMON_ON') : $_('COMMON_OFF')}
    </span>

    <label class="checkbox-container toggle-container">
      <input
        type="checkbox"
        bind:checked={pppToggle}
        role="switch"
        aria-checked={pppToggle}
        aria-describedby="ppp-help"
        on:keydown={event => {
          if (event.code === 'Space') {
            event.preventDefault();
            event.stopPropagation();
            pppToggle = !pppToggle;
          }
        }}
        on:click={event => {
          event.preventDefault();
          pppToggle = !pppToggle;
        }} />
      <div class="toggle-switch {pppToggle ? 'active' : ''}" aria-hidden="true">
      </div>
      <span class="toggle-label">{$_('LHAKU_TOGGLE_PPP')}</span>
    </label>
    <span id="ppp-help" class="sr-only">
      {pppToggle ? $_('COMMON_ON') : $_('COMMON_OFF')}
    </span>
  </fieldset>
</div>
