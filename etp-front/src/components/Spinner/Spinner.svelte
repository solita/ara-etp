<script>
  export let white = false;
  export let smaller = false;
  export let disableAnimation = false;
</script>

<style type="text/postcss">
  .spinner {
    @apply inline-block relative w-12 h-12;
  }

  .spinner div {
    @apply block absolute w-12 h-12 rounded-full border-solid border-8;
    box-sizing: border-box;
    border-color: #538000 transparent transparent transparent;
    animation: spin 1.2s linear infinite;
  }

  .spinner.smaller {
    transform: scale(0.75);
  }

  .white div {
    border-color: white transparent transparent transparent;
  }

  @keyframes spin {
    0% {
      transform: rotate(0deg);
    }
    100% {
      transform: rotate(360deg);
    }
  }

  .disable-animation > * {
    animation: none !important;
    transform: none !important;
    transition: none !important;
  }
</style>

<div
  class="spinner"
  class:smaller
  class:white
  class:disable-animation={disableAnimation}
  data-cy="spinner">
  <div />
</div>
