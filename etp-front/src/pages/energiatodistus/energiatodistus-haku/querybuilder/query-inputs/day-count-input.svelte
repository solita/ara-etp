<script>
  import { _ } from '@Language/i18n';
  import NumberInput from './number-input';
  import * as Formats from '@Utility/formats';
  import * as Parsers from '@Utility/parsers';

  import * as R from 'ramda';

  export let values = [];
  export let nameprefix;
  export let index = 0;

  const i18n = $_;
  const suffix = i18n('units.days');

  const format = num => `${num} ${suffix}`;
  const parse = Parsers.parseDayCount;
</script>

<NumberInput {values} {nameprefix} {index} {format} {parse} />
