<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Parsers from '@Utility/parsers';
  import * as Formats from '@Utility/formats';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Toimenpiteet from './toimenpiteet';

  import * as ValvontaApi from './valvonta-api';

  import { _ } from '@Language/i18n';
  import H1 from '@Component/H/H1';

  export let params;
  const i18nRoot = 'valvonta.oikeellisuus.new-toimenpide';

  const text = R.compose($_, Toimenpiteet.i18nKey);
</script>

<H1 text={text(params, 'title')} />
