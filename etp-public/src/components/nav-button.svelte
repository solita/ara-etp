<script context="module">
  export const styles = {
    lightgreen: {
      text: 'text-black',
      border: 'border-black',
      lg_border: 'border-lightgreen',
      background: 'bg-lightgreen'
    },
    green: {
      text: 'text-white',
      border: 'border-white',
      lg_border: 'border-green',
      background: 'bg-green'
    },
    darkgreen: {
      text: 'text-white',
      border: 'border-white',
      lg_border: 'border-altgreen',
      background: 'bg-altgreen'
    },
    ashblue: {
      text: 'text-white',
      border: 'border-white',
      lg_border: 'border-ashblue',
      background: 'bg-ashblue'
    }
  };
</script>

<script>
  export let link;
  export let text;
  export let background;
  export let click = () => {};

  import { activePath } from '@Router/router';
</script>

<style>
  a {
    transition: box-shadow 0.1s;
  }
</style>

<a
  class="flex box-border lg:flex-full flex-auto py-4 xl:py-6 px-2 navcontainer
  {background} {text} {text} focus:z-10 hover:z-10 hover:underline
  focus:outline-none xl:flex-row lg:flex-col flex-row lg:items-center
  xl:items-center md:items-start {$activePath === link
    ? 'border-l-4'
    : 'border-l-0'} lg:border-l-0 lg:{background} focus:underline"
  class:shadow-lightgreen={$activePath === link && background === 'lightgreen'}
  class:shadow-green={$activePath === link && background === 'green'}
  class:shadow-altgreen={$activePath === link && background === 'altgreen'}
  class:shadow-ashblue={$activePath === link && background === 'ashblue'}
  aria-current={$activePath === link}
  href={link}
  on:click={click}>
  <div class="inline-block items-start justify-end flex-shrink mx-2">
    <slot name="iconleft" />
  </div>
  <div class="flex flex-col ml-2 flex-1 flex-grow items-start justify-start">
    <slot name="title" />
    <slot name="subtitle" />
  </div>
  <div class="inline-flex items-start justify-end flex-shrink lg:hidden">
    <slot name="iconright" />
  </div>
</a>
