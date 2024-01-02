<script>
  import { _, locale, labelLocale } from '@Localization/localization';

  export let patevyydet;
  export let showPatevyydet = '1,2';
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
</style>

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
      {labelLocale($locale, patevyydet[0])}
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
      {labelLocale($locale, patevyydet[1])}
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
