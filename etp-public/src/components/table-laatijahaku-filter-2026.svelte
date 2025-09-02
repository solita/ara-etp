<script>
  import { _ } from '@Localization/localization';
  import { createEventDispatcher } from 'svelte';

  const dispatch = createEventDispatcher();

  export let showPatevyydet = '1,2,3,4';

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

<div data-filter-component>
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
</div>
