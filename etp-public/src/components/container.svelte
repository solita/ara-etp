<script context="module">
  export const styles = {
    beige: { background: 'beige' },
    mainnavigation: { background: 'mainnavigation' },
    ashbluewhite: { background: 'ashbluewhite' },
    grey: { background: 'grey' },
    white: { background: 'white' },
    hero: { background: 'heroblurred' },
    red: { background: 'red' }
  };
</script>

<script>
  export let background;
</script>

<style>
  .bg-mainnavigation {
    background: rgb(197, 225, 145);
    background: linear-gradient(
      90deg,
      rgba(197, 225, 145, 1) 50%,
      rgba(46, 80, 83, 1) 50%
    );
  }

  .bg-ashbluewhite {
    background: rgb(46, 80, 83);
    background: linear-gradient(
      90deg,
      rgba(46, 80, 83, 1) 33%,
      rgba(255, 255, 255, 1) 33%
    );
  }

  .bg-heroblurred {
    background-image: url('@Asset/heroblurred.jpg?{fit:"inside",height:555,width:1440}');
    background-size: cover;
  }

  .bg-red {
    background-color: #ff0000;
  }
</style>

<!-- purgecss: 
    bg-beige
    bg-mainnavigation
    bg-ashbluewhite
    bg-grey
    bg-white
    bg-heroblurred
    bg-red
-->

<div class="bg-{background}">
  <div class="mx-auto xxl:container">
    <slot />
  </div>
</div>
