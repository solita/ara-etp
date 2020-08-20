<script>
  import {_} from '@Language/i18n';
  import Spinner from "../Spinner/Spinner.svelte";
  import * as Maybe from "@Utility/maybe-utils";

  export let error = Maybe.None();
</script>

<style type="text/postcss">
</style>

{#if error.isNone()}
<div class="flex flex-row items-center">
  <Spinner/> <p class="ml-6">{$_('loading')}</p>
</div>
{:else}
  <p>{error.some()}</p>
{/if}