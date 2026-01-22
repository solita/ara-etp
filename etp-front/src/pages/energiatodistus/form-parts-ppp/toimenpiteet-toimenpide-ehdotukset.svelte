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
  export let luokittelut;

  const toimenpideEhdotukset = R.prop('toimenpide-ehdotus', luokittelut);
  const toimenpideEhdotusGroup = R.prop(
    'toimenpide-ehdotus-group',
    luokittelut
  );

  const vaiheIndex = vaihe => vaihe['vaihe-nro'] - 1;

  const toimenpideEhdotusLabel = label => toimenpideEhdotusId => {
    const toimendpideEhdotusGroupId = R.compose(
      Maybe.orSome(0),
      R.map(R.prop('group-id')),
      Maybe.findById(toimenpideEhdotusId)
    )(toimenpideEhdotukset);

    return (
      ETUtils.selectFormat(
        label,
        toimenpideEhdotusGroup
      )(toimendpideEhdotusGroupId) +
      ' / ' +
      ETUtils.selectFormat(label, toimenpideEhdotukset)(toimenpideEhdotusId)
    );
  };
</script>

<div>
  <H5 text={i18n('perusparannuspassi.toimenpiteet.ehdotukset.header')} />
  <div class="grid grid-flow-col grid-rows-3 gap-6">
    {#each R.range(0, 6) as toimenpideN}
      <div>
        <Select
          variant={SelectVariants.LIGHT}
          items={R.pluck('id', toimenpideEhdotukset)}
          parse={Maybe.Some}
          format={toimenpideEhdotusLabel(LocaleUtils.label($locale))}
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
