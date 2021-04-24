<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Parsers from '@Utility/parsers';
  import * as Formats from '@Utility/formats';

  import { _, locale } from '@Language/i18n';

  import * as Toimenpiteet from './toimenpiteet';

  import Datepicker from '@Component/Input/Datepicker.svelte';
  import Select from '@Component/Select/Select.svelte';

  const i18nRoot = 'valvonta.oikeellisuus.toimenpide.audit-report';

  export let toimenpide;
  export let templatesByType;
  export let disabled;

  $: templates = Toimenpiteet.templates(templatesByType)(toimenpide);
  $: formatTemplate  = Toimenpiteet.templateLabel($locale, templates);
</script>

<div class="flex py-4">
  <Datepicker
      label="Määräpäivä"
      {disabled}
      bind:model={toimenpide}
      lens={R.lensProp('deadline-date')}
      format={Maybe.fold('', Formats.formatDateInstant)}
      parse={Parsers.optionalParser(Parsers.parseDate)}
      transform={EM.fromNull}
      i18n={$_} />
</div>

{#if !R.isEmpty(templates)}
  <div class="w-1/2 py-4">
    <Select
        label="Valitse asiakirjapohja"
        bind:model={toimenpide}
        lens={R.lensProp('template-id')}
        parse={Maybe.fromNull}
        format={formatTemplate}
        items={R.pluck('id', templates)} />
  </div>
{/if}