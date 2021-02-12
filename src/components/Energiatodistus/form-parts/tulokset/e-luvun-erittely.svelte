<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Future from '@Utility/future-utils';
  import * as formats from '@Utility/formats';
  import * as fxmath from '@Utility/fxmath';

  import H3 from '@Component/H/H3';
  import Input from '@Component/Energiatodistus/Input';
  import ELukuUnit from '@Component/Energiatodistus/form-parts/units/e-luku';
  import EVuosiKulutusUnit from '@Component/Energiatodistus/form-parts/units/e-painotettu-vuosikulutus.svelte';
  import VuosikulutusUnit from '@Component/Energiatodistus/form-parts/units/annual-energy';

  import * as EtUtils from '@Component/Energiatodistus/energiatodistus-utils';
  import * as api from '@Component/Energiatodistus/energiatodistus-api';

  export let disabled;
  export let schema;
  export let energiatodistus;
  export let versio;
  export let eTehokkuus = Maybe.None();

  let eTehokkuusParams = [];

  $: energiamuotokertoimet = R.mergeRight(
    EtUtils.energiamuotokertoimet()[versio],
    EtUtils.muutEnergiamuotokertoimet(energiatodistus)
  );

  $: ostoenergiat = R.mergeRight(
    EtUtils.ostoenergiat(energiatodistus),
    EtUtils.muutOstoenergiat(energiatodistus)
  );

  $: ostoenergiaSum = EtUtils.sumEtValues(ostoenergiat);

  $: painotetutOstoenergiankulutukset = R.compose(
    R.map(R.apply(EtUtils.multiplyWithKerroin)),
    R.mergeWith(Array.of)
  )(energiamuotokertoimet, ostoenergiat);

  $: painotetutOstoenergiankulutuksetSum = R.compose(EtUtils.sumEtValues)(
    painotetutOstoenergiankulutukset
  );

  $: painotetutOstoenergiankulutuksetPerNelio = EtUtils.perLammitettyNettoala(
    energiatodistus,
    painotetutOstoenergiankulutukset
  );

  $: painotetutOstoenergiankulutuksetPerNelioSum = R.compose(
    EtUtils.sumEtValues
  )(painotetutOstoenergiankulutuksetPerNelio);

  $: eLuku = R.filter(
    R.complement(R.equals(0)),
    R.map(Math.ceil, painotetutOstoenergiankulutuksetPerNelioSum)
  );

  $: {
    const params = Maybe.toMaybeList([
      energiatodistus.perustiedot.kayttotarkoitus,
      EM.toMaybe(energiatodistus.lahtotiedot['lammitetty-nettoala']),
      eLuku
    ]);

    Maybe.orElseRun(_ => {
      eTehokkuus = Maybe.None();
    }, params);

    R.forEach(p => {
      // calculate new e-luokka only if params has changed
      if (!R.equals(p, eTehokkuusParams)) {
        eTehokkuusParams = p;
        Future.fork(
          _ => {},
          response => {
            eTehokkuus = Maybe.Some(R.assoc('e-luku', p[2], response));
          },
          R.apply(api.getEluokka(fetch, versio), p)
        );
      }
    }, params);
  }
</script>

<H3
  compact={true}
  text={$_('energiatodistus.tulokset.kaytettavat-energiamuodot.header')} />

<table class="et-table et-table__noborder mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th">
        {$_(
          'energiatodistus.tulokset.kaytettavat-energiamuodot.kaytettavat-energiamuodot'
        )}
      </th>
      <th class="et-table--th">
        <span>
          {$_(
            `energiatodistus.tulokset.kaytettavat-energiamuodot.ostoenergia.${versio}`
          )}
        </span>
        <span class="block">
          <VuosikulutusUnit />
        </span>
      </th>
      <th class="et-table--th">
        {$_(
          'energiatodistus.tulokset.kaytettavat-energiamuodot.energiamuodon-kerroin'
        )}
      </th>
      <th class="et-table--th et-table--th__twocells" colspan="2">
        <div>
          {$_(
            'energiatodistus.tulokset.kaytettavat-energiamuodot.kertoimella-painotettu-energiankulutus'
          )}
        </div>
        <div class="flex flex-row justify-around">
          <span>
            <EVuosiKulutusUnit />
          </span>
          <span>
            <ELukuUnit />
          </span>
        </div>
      </th>
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    {#each ['kaukolampo', 'sahko', 'uusiutuva-polttoaine', 'fossiilinen-polttoaine', 'kaukojaahdytys'] as energiamuoto}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(
            `energiatodistus.tulokset.kaytettavat-energiamuodot.labels.${energiamuoto}`
          )}
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['tulokset', 'kaytettavat-energiamuodot', energiamuoto]} />
        </td>
        <td class="et-table--td">
          {R.compose(
            Maybe.get,
            R.map(formats.numberFormat),
            R.prop(energiamuoto)
          )(energiamuotokertoimet)}
        </td>
        <td class="et-table--td et-table--td__fifth">
          {R.compose(
            Maybe.orSome(''),
            R.map(R.compose(formats.numberFormat, fxmath.round(0))),
            R.prop(energiamuoto)
          )(painotetutOstoenergiankulutukset)}
        </td>
        <td class="et-table--td et-table--td__fifth">
          {R.compose(
            Maybe.orSome(''),
            R.map(R.compose(formats.numberFormat, fxmath.round(0))),
            R.prop(energiamuoto)
          )(painotetutOstoenergiankulutuksetPerNelio)}
        </td>
      </tr>
    {/each}
    {#each R.defaultTo([], energiatodistus.tulokset['kaytettavat-energiamuodot'].muu) as _, index}
      <tr class="et-table--tr">
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={[
              'tulokset',
              'kaytettavat-energiamuodot',
              'muu',
              index,
              'nimi'
            ]} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={[
              'tulokset',
              'kaytettavat-energiamuodot',
              'muu',
              index,
              'ostoenergia'
            ]} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={[
              'tulokset',
              'kaytettavat-energiamuodot',
              'muu',
              index,
              'muotokerroin'
            ]} />
        </td>
        <td class="et-table--td et-table--td__fifth">
          {R.compose(
            Maybe.orSome(''),
            R.map(fxmath.round(0)),
            R.prop('muu-' + index)
          )(painotetutOstoenergiankulutukset)}
        </td>
        <td class="et-table--td et-table--td__fifth">
          {R.compose(
            Maybe.orSome(''),
            R.map(fxmath.round(0)),
            R.prop('muu-' + index)
          )(painotetutOstoenergiankulutuksetPerNelio)}
        </td>
      </tr>
    {/each}
    <tr class="et-table--tr border-t-1 border-disabled">
      <td class="et-table--td uppercase">
        {$_('energiatodistus.tulokset.yhteensa')}
      </td>
      <td class="et-table--td">
        {R.compose(
          Maybe.get,
          R.map(R.compose(formats.numberFormat, fxmath.round(0)))
        )(ostoenergiaSum)}
      </td>
      <td class="et-table--td" />
      <td class="et-table--td et-table--td__fifth">
        {R.compose(
          Maybe.get,
          R.map(R.compose(formats.numberFormat, fxmath.round(0)))
        )(painotetutOstoenergiankulutuksetSum)}
      </td>
      <td class="et-table--td et-table--td__fifth">
        {R.compose(
          Maybe.get,
          R.map(R.compose(formats.numberFormat, Math.ceil))
        )(painotetutOstoenergiankulutuksetPerNelioSum)}
      </td>
    </tr>
  </tbody>
</table>
