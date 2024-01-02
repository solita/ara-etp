<script>
  export let href;
  export let target;
  export let referrerpolicy;
</script>

<a class="underline text-darkgreen" {href} {target} {referrerpolicy}>
  <slot />
</a>
