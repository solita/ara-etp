<script>
  import * as Maybe from '@Utility/maybe-utils';
  import * as Response from '@Utility/response';
  import { _ } from '@Language/i18n';

  export let error;
</script>

<style type="text/postcss">
  .content {
    @apply flex flex-col flex-grow py-8 px-10 mx-auto bg-light;
  }
</style>

<section class="content">
  <p class="bg-error flex justify-around py-4 px-4 bg-primary text-light rounded-lg">
    <span class="font-icon pr-4">error</span>
    {$_(Maybe.orSome('errors.whoami', Response.localizationKey(error)))}
  </p>
</section>