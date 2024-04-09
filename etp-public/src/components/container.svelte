<script context="module">
  export const styles = {
    beige: { background: 'bg-beige' },
    mainnavigation: { background: 'bg-mainnavigation' },
    ashbluewhite: { background: 'bg-ashbluewhite' },
    grey: { background: 'bg-grey' },
    white: { background: 'bg-white' },
    hero: { background: 'bg-heroblurred' },
    red: { background: 'bg-red' }
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
    background-image: url('~@Asset/heroblurred.jpg?{fit:"inside",height:555,width:1440}');
    background-size: cover;
  }

  .bg-red {
    background-color: #ff0000;
  }
</style>

<div class={background}>
  <div class="mx-auto xxl:container">
    <slot />
  </div>
</div>
