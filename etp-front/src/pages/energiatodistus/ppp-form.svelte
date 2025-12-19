<script>
  import H3 from '@Component/H/H3';
  import { _ } from '@Language/i18n';
  import * as schemas from './schema';
  import * as R from 'ramda';

  import Perustiedot from './form-parts-ppp/ppp-perustiedot';
  import RakenteidenUArvot from './form-parts-ppp/rakenteiden-u-arvot';
  import Energiajarjestelmat from './form-parts-ppp/energiajarjestelmat';
  import Vaiheistus from './form-parts-ppp/laskennan-tulokset-vaiheistus';
  import LaskennallinenOstoenergia from './form-parts-ppp/laskennan-tulokset-laskennallinen-ostoenergia';
  import UusiutuvaEnergia from './form-parts-ppp/laskennan-tulokset-uusiutuva-energia';
  import ToteutunutOstoenergia from './form-parts-ppp/laskennan-tulokset-toteutunut-ostoenergia';
  import EnergianHinta from './form-parts-ppp/laskennan-tulokset-energian-hinta';
  import LaskennallinenEnergiaKustannukset from './form-parts-ppp/laskennan-tulokset-kustannukset-laskennallinen-ostoenergia';
  import ToteutunutOstoenergiaKustannukset from './form-parts-ppp/laskennan-tulokset-kustannukset-toteutunut-ostoenergia';
  import Toimenpiteet from './form-parts-ppp/toimenpiteet';
  import Lisatietoja from './form-parts-ppp/lisatietoja.svelte';

  export let energiatodistus;
  export let inputLanguage;
  export let luokittelut;
  export let perusparannuspassi;
  export let schema;
  export let pppvalidation;

  const required = perusparannuspassi =>
    perusparannuspassi['bypass-validation-limits']
      ? pppvalidation.requiredBypass
      : pppvalidation.requiredAll;

  const saveSchema = R.compose(
    R.reduce(schemas.assocRequired, R.__, required(perusparannuspassi)),
    schema =>
      perusparannuspassi['bypass-validation-limits']
        ? schema
        : R.reduce(
            schemas.redefineNumericValidation,
            schema,
            pppvalidation.numeric
          )
  )(schemas.perusparannuspassi);

  const vaiheSchema = R.reduce(
    schemas.redefineNumericValidation,
    schemas.perusparannuspassi,
    pppvalidation.vaiheNumeric
  );
</script>

<div class="flex flex-col gap-6">
  <H3 text={$_('perusparannuspassi.passin-perustiedot.header')} />

  <Perustiedot bind:perusparannuspassi schema={saveSchema} />

  <H3 text={$_('perusparannuspassi.rakennuksen-perustiedot.header')} />

  <RakenteidenUArvot
    bind:perusparannuspassi
    {energiatodistus}
    schema={saveSchema} />

  <Energiajarjestelmat
    bind:perusparannuspassi
    {energiatodistus}
    {inputLanguage}
    {luokittelut}
    {schema} />

  <H3 text={$_('perusparannuspassi.laskennan-tulokset.header')} />
  <Vaiheistus bind:perusparannuspassi schema={vaiheSchema} />

  <LaskennallinenOstoenergia
    bind:perusparannuspassi
    {energiatodistus}
    {schema} />
  <UusiutuvaEnergia bind:perusparannuspassi {energiatodistus} {schema} />
  <ToteutunutOstoenergia bind:perusparannuspassi {energiatodistus} {schema} />
  <EnergianHinta
    bind:perusparannuspassi
    {energiatodistus}
    schema={saveSchema} />

  <LaskennallinenEnergiaKustannukset
    bind:perusparannuspassi
    {energiatodistus}
    {schema} />

  <ToteutunutOstoenergiaKustannukset
    bind:perusparannuspassi
    {energiatodistus}
    {schema} />

  <Toimenpiteet
    {energiatodistus}
    bind:perusparannuspassi
    pppSchema={schema}
    toimenpideEhdotuksetLuokittelu={R.prop('toimenpide-ehdotus', luokittelut)}
    {inputLanguage} />

  <Lisatietoja
    {energiatodistus}
    bind:perusparannuspassi
    {schema}
    {inputLanguage} />
</div>
