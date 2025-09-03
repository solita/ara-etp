<script>
  import { _ } from '@Localization/localization';
  import { createEventDispatcher, tick, onMount } from 'svelte';

  const dispatch = createEventDispatcher();

  export let showPatevyydet = '1,2,3,4';

  // Individual toggle states for ETP 2026 filters
  let ylempitasoToggle = false;
  let pppToggle = false;
  let isUpdatingFromProp = false;
  let ylempitasoInput;
  let pppInput;
  let mounted = false;

  // Store focus state in sessionStorage to persist across re-mounts
  let focusElementKey = 'laatija-filter-focus';

  // Lookup table for toggle combinations
  const toggleMappings = {
    'false,false': '1,2,3,4',
    'true,false': '2,4',
    'false,true': '3,4',
    'true,true': '4'
  };

  // Reverse mapping from IDs to toggle states
  const reverseMappings = Object.fromEntries(
    Object.entries(toggleMappings).map(([key, value]) => [value, key])
  );

  onMount(() => {
    mounted = true;

    // Restore focus after component mounts
    const storedFocus = sessionStorage.getItem(focusElementKey);
    if (storedFocus) {
      // Use setTimeout to ensure DOM is fully ready
      setTimeout(() => {
        if (storedFocus === 'ylempi' && ylempitasoInput) {
          ylempitasoInput.focus();
        } else if (storedFocus === 'ppp' && pppInput) {
          pppInput.focus();
        }
        // Clear stored focus after restoration
        sessionStorage.removeItem(focusElementKey);
      }, 50);
    }
  });

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

  const updatePropFromToggles = async () => {
    if (isUpdatingFromProp || !mounted) return;

    const toggleKey = `${ylempitasoToggle},${pppToggle}`;
    const newValue = toggleMappings[toggleKey];

    if (newValue && newValue !== showPatevyydet) {
      // Store which element has focus before navigation
      const focusedElement = document.activeElement;
      if (focusedElement === ylempitasoInput) {
        sessionStorage.setItem(focusElementKey, 'ylempi');
      } else if (focusedElement === pppInput) {
        sessionStorage.setItem(focusElementKey, 'ppp');
      }

      showPatevyydet = newValue;
      dispatch('change', newValue);
    }
  };

  const handleToggleChange = toggleName => {
    if (!mounted || isUpdatingFromProp) return;

    // Store focus before triggering update
    sessionStorage.setItem(focusElementKey, toggleName);

    // Trigger the update which will cause re-mount
    updatePropFromToggles();
  };

  $: if (showPatevyydet && showPatevyydet !== 'on' && !isUpdatingFromProp) {
    updateTogglesFromProp(showPatevyydet);
  }

  const handleKeydown = (event, toggleName) => {
    if (event.code === 'Space') {
      event.preventDefault();
      event.stopPropagation();

      if (toggleName === 'ylempi') {
        ylempitasoToggle = !ylempitasoToggle;
      } else {
        pppToggle = !pppToggle;
      }

      handleToggleChange(toggleName);
    }
  };

  const handleClick = (event, toggleName) => {
    event.preventDefault();

    if (toggleName === 'ylempi') {
      ylempitasoToggle = !ylempitasoToggle;
    } else {
      pppToggle = !pppToggle;
    }

    handleToggleChange(toggleName);
  };
</script>

<style>
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

  /* Custom positioning for the checkbox input */
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

<div>
  <fieldset class="flex flex-row gap-6 items-center pt-3">
    <legend class="sr-only">{$_('LAATIJA_PATEVYYSTASO')}</legend>

    <!-- Ylempi taso toggle -->
    <label class="checkbox-container toggle-container">
      <input
        type="checkbox"
        bind:this={ylempitasoInput}
        bind:checked={ylempitasoToggle}
        role="switch"
        aria-checked={ylempitasoToggle}
        aria-describedby="ylempi-help"
        on:keydown={e => handleKeydown(e, 'ylempi')}
        on:click={e => handleClick(e, 'ylempi')} />
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
        bind:this={pppInput}
        role="switch"
        aria-checked={pppToggle}
        aria-describedby="ppp-help"
        on:keydown={e => handleKeydown(e, 'ppp')}
        on:click={e => handleClick(e, 'ppp')} />
      <div class="toggle-switch {pppToggle ? 'active' : ''}" aria-hidden="true">
      </div>
      <span class="toggle-label">{$_('LHAKU_TOGGLE_PPP')}</span>
    </label>
    <span id="ppp-help" class="sr-only">
      {pppToggle ? $_('COMMON_ON') : $_('COMMON_OFF')}
    </span>
  </fieldset>
</div>
