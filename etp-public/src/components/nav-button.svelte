<script context="module">
  export const styles = {
    lightgreen: {
      text: 'black',
      background: 'lightgreen'
    },
    green: {
      text: 'white',
      background: 'green'
    },
    darkgreen: {
      text: 'white',
      background: 'altgreen'
    },
    ashblue: {
      text: 'white',
      background: 'ashblue'
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

<!-- purgecss: 
bg-lightgreen 
bg-green 
bg-altgreen 
bg-ashblue
text-white
text-black
shadow-lightgreen
shadow-green
shadow-altgreen
shadow-ashblue 
shadow-none 
border-lightgreen
border-green
border-darkgreen
border-altgreen
border-ashblue
border-black
border-white
-->
<a
  class="flex box-border lg:flex-full flex-auto py-4 xl:py-6 px-2 navcontainer
  bg-{background} text-{text} border-{text} focus:z-10 hover:z-10 hover:underline
  focus:outline-none xl:flex-row lg:flex-col flex-row lg:items-center
  xl:items-center md:items-start {$activePath === link
    ? 'border-l-4'
    : 'border-l-0'} lg:border-l-0 lg:border-{background} focus:underline"
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
