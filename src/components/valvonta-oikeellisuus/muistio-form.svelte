<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Parsers from '@Utility/parsers';
  import * as Formats from '@Utility/formats';

  import { _ } from '@Language/i18n';

  import Datepicker from '@Component/Input/Datepicker.svelte';
  import Select from '@Component/Select/Select.svelte';

  const i18nRoot = 'valvonta.oikeellisuus.new-toimenpide.audit-report';

  export let toimenpide;
</script>

<div class="flex py-4">
  <Datepicker
      label="Määräpäivä"
      bind:model={toimenpide}
      lens={R.lensProp('deadline-date')}
      format={Maybe.fold('', Formats.formatDateInstant)}
      parse={Parsers.optionalParser(Parsers.parseDate)}
      transform={EM.fromNull}
      i18n={$_} />
</div>

<div class="w-1/2 py-4">
  <Select
      label="Valitse asiakirjapohja"
      model={Maybe.None()}
      lens={R.lens(R.identity, R.identity)}
      items={[
            'Energiatodistus 2013 FI',
            'Energiatodistus 2013 SV',
            'Energiatodistus 2018 FI',
            'Energiatodistus 2018 SV'
          ]} />
</div>