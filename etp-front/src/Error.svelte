<script>
  import * as Maybe from '@Utility/maybe-utils';
  import * as Response from '@Utility/response';
  import { _ } from '@Language/i18n';

  export let error;
</script>

<style type="text/postcss">
  .content {
    @apply mx-auto flex flex-grow flex-col bg-light px-10 py-8;
  }
</style>

<section class="content">
  <p
    data-cy="error"
    class="flex justify-around rounded-lg bg-error bg-primary px-4 py-4 text-light">
    <span class="pr-4 font-icon">error</span>
    {$_(Maybe.orSome('errors.whoami', Response.localizationKey(error)))}
  </p>
</section>
