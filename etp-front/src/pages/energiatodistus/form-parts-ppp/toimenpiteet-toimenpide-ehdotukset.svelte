<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import { _, locale } from '@Language/i18n';
  import * as ETUtils from '@Pages/energiatodistus/energiatodistus-utils';
  import * as LocaleUtils from '@Language/locale-utils';

  import H5 from '@Component/H/H5';
  import {
    default as Select,
    Variants as SelectVariants
  } from '@Component/Select/Select';

  import Textarea from '@Pages/energiatodistus/Textarea.svelte';

  const i18n = $_;

  export let perusparannuspassi;
  export let pppSchema;
  export let vaihe;
  export let inputLanguage;
  export let toimenpideEhdotuksetLuokittelu;

  const vaiheIndex = vaihe => vaihe['vaihe-nro'] - 1;
</script>

<div>
  <H5 text={i18n('perusparannuspassi.toimenpiteet.ehdotukset.header')} />
  <div class="grid grid-flow-col grid-rows-3 gap-6">
    {#each R.range(0, 6) as toimenpideN}
      <div>
        <Select
          variant={SelectVariants.LIGHT}
          items={R.pluck('id', toimenpideEhdotuksetLuokittelu)}
          parse={Maybe.Some}
          format={ETUtils.selectFormat(
            LocaleUtils.label($locale),
            toimenpideEhdotuksetLuokittelu
          )}
          validation={false}
          required={false}
          allowNone={true}
          bind:model={perusparannuspassi}
          lens={R.lensPath([
            'vaiheet',
            vaiheIndex(vaihe),
            'toimenpiteet',
            'toimenpide-ehdotukset',
            toimenpideN
          ])}
          label={i18n(
            'perusparannuspassi.toimenpiteet.ehdotukset.toimenpide-n',
            { values: { n: toimenpideN + 1 } }
          )} />
      </div>
    {/each}
  </div>
  <div class="w-full">
    <Textarea
      schema={pppSchema}
      showOrdinal={false}
      inputLanguage={Maybe.Some(inputLanguage)}
      bind:model={perusparannuspassi}
      path={['vaiheet', vaiheIndex(vaihe), 'toimenpiteet', 'toimenpideseloste']}
      i18nRoot="perusparannuspassi" />
  </div>
</div>
