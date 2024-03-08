<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Locales from '@Language/locale-utils';

  import Autocomplete from '@Component/Autocomplete/Autocomplete';

  import { locale } from '@Language/i18n';

  export let values = [];
  export let nameprefix;
  export let kunnat;

  const kuntaLabel = Locales.label($locale);

  let queryKuntaId = R.defaultTo('', R.head(values));
  let queryKuntaLabel = Locales.labelForId($locale, kunnat)(queryKuntaId);
  let input;

  const updateInput = label => {
    const kunta = Maybe.find(R.compose(R.equals(label), kuntaLabel), kunnat);

    R.forEach(
      id => {
        input.value = id;
        input.dispatchEvent(new Event('change', { bubbles: true }));
      },
      R.map(R.prop('id'), kunta)
    );
  };

  $: !R.isNil(input) && updateInput(queryKuntaLabel);
</script>

<style>
</style>

<Autocomplete
  bind:completedValue={queryKuntaLabel}
  items={R.map(kuntaLabel, kunnat)}
  size={10000} />
<input
  bind:this={input}
  tabindex="-1"
  class="hidden"
  name={`${nameprefix}_value_0`} />
