<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Parsers from '@Utility/parsers';
  import * as Formats from '@Utility/formats';
  import * as Locales from '@Language/locale-utils';

  import { _, locale } from '@Language/i18n';

  import * as Toimenpiteet from './toimenpiteet';
  import TextEditor from '@Component/text-editor/text-editor';
  import Liitteet from '@Component/liitteet/liitteet.svelte';
  import H2 from '@Component/H/H2.svelte';

  const i18nRoot = 'valvonta.oikeellisuus.toimenpide';
  const i18n = $_;
  const text = R.compose(i18n, Toimenpiteet.i18nKey);

  export let toimenpide;
  export let schema;
  export let disabled;
  export let liiteApi;
  export let liitteet;
</script>

<div class="w-full py-4 mb-4">
  <TextEditor
    id={'toimenpide.description'}
    label={text(toimenpide, 'description')}
    {disabled}
    bind:model={toimenpide}
    lens={R.lensProp('description')}
    required={true}
    format={Maybe.orSome('')}
    parse={Parsers.optionalString}
    validators={schema.description}
    {i18n} />
</div>

<H2 text="Vastauksen liitteet" />

<Liitteet
  liiteApi={R.applySpec(liiteApi)(toimenpide)}
  {liitteet}
  {disabled}
  emptyMessageKey="valvonta.oikeellisuus.toimenpide.liitteet.empty"
  flashModule="valvonta-oikeellisuus" />
