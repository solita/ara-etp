<script>

</script>

<style type="text/postcss">
  div {
    @apply bg-primary opacity-75 rounded-full w-8 h-8;
    animation: ball-scale 2s infinite ease-in-out;
  }

  @keyframes ball-scale {
    0%,
    100% {
      transform: scaleX(0);
    }
    50% {
      transform: scaleX(1);
    }
  }
</style>

<div />
