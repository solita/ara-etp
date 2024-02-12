<script>
  export let text = '';

  export let focus = true;

  const setFocus = e => {
    if (focus) {
      e.focus();
    }
  };
</script>

<style type="text/postcss">
  /* outline-none is generally discouraged, but for h1 we do not expect
   * the user to want to focus there. The intended effect here is that
   * an average, able user will not perceive it as a focusable item in any
   * way, but we can set the focus there so that the screen reader will read
   * it out and we get the tab context into some consistent place.
   */
  h1 {
    @apply text-secondary font-bold uppercase text-lg mb-8 tracking-xl outline-none;
  }
</style>

<h1 tabindex="-1" use:setFocus>{text}</h1>
