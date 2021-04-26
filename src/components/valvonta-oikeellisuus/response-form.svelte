<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Parsers from '@Utility/parsers';
  import * as Formats from '@Utility/formats';
  import * as Locales from '@Language/locale-utils';

  import { _, locale } from '@Language/i18n';

  import * as Toimenpiteet from './toimenpiteet';
  import Textarea from '@Component/Textarea/Textarea.svelte';

  const i18nRoot = 'valvonta.oikeellisuus.toimenpide';
  const i18n = $_;
  const text = R.compose(i18n, Toimenpiteet.i18nKey);

  export let toimenpide;
  export let disabled;

</script>

<div class="w-full py-4">
  <Textarea
      id={'toimenpide.document'}
      name={'toimenpide.document'}
      label={text(toimenpide, 'document')}
      {disabled}
      bind:model={toimenpide}
      lens={R.lensProp('document')}
      required={false}
      format={Maybe.orSome('')}
      parse={Parsers.optionalString}
      {i18n} />
</div>