<script>
  export let text = '';

  let element = undefined;

  /*
   * Here we are making an assumption that there will consistently be exactly
   * one H1 on the page and we want to focus it, to get two effects:
   *
   * 1. The screen reader will read out the title of the page when it loads.
   * 2. The tab context will be set to a consistent place, so that the user
   *    can start tabbing through the page from a known place.
   */
  const setFocus = e => {
    e.focus();
    element = e;
  };

  /*
   * If the text paramter changes, the h1 does not get re-created - only
   * its content is updated. This makes things work at least in the ohje
   * pages, where navigation will not cause the whole page to get reloaded.
   */
  const setFocusWithText = _text => {
    if (element) {
      element.focus();
    }
  };

  $: setFocusWithText(text);
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
