<script>
  export let src;
  export let alt;

  let box;
  let image;
  let width = 0;
  let height = 0;
</script>

<style>
  .offset-t {
    top: -15px;
  }

  .offset-l {
    left: 15px;
  }
</style>

<svelte:window
  on:resize={_ => {
    width = image.width;
    height = image.height;
  }} />

<div class="offset-t relative block mr-2">
  <div
    bind:this={box}
    style={`width:${width}px;height:${height}px;`}
    class="absolute offset-t offset-l border-4 border-white" />
  <img
    bind:this={image}
    on:load={evt => {
      width = evt.target.width;
      height = evt.target.height;
    }}
    {src}
    {alt} />
</div>
