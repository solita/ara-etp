<script>
</script>

<style type="text/postcss">
  .spinner {
    @apply inline-block relative w-12 h-12;
  }
  .spinner div {
    @apply block absolute w-12 h-12 rounded-full border-solid border-8 box-border;
    border-color: #538000 transparent transparent transparent;
    animation: spin 1.2s linear infinite;
  }
  @keyframes spin {
    0% {
      transform: rotate(0deg);
    }
    100% {
      transform: rotate(360deg);
    }
  }
</style>

<div class="spinner">
  <div />
</div>
