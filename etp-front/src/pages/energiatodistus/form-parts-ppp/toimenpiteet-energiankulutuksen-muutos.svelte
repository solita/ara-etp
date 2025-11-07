<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';

  import { _ } from '@Language/i18n';
  import VuosikulutusUnit from '@Pages/energiatodistus/form-parts/units/annual-energy';
  import HiilidioksidiekvivalenttiTonnitPerVuosiUnit from '@Pages/energiatodistus/form-parts/units/annual-co2-ekv-tons';
  import EurosPerVuosiUnit from '@Pages/energiatodistus/form-parts/units/annual-euros';
  import * as formats from '@Utility/formats.js';
  import * as PppUtils from './ppp-utils.js';

  import H5 from '@Component/H/H5';

  export let energiatodistus;
  export let perusparannuspassi;
  export let vaihe;

  const i18n = $_;

  const calculateMetrics = (et, ppp) => {
    const allMetrics = PppUtils.calculateDerivedValues(et, ppp);

    return {
      previous: allMetrics[vaihe['vaihe-nro'] - 1],
      current: allMetrics[vaihe['vaihe-nro']]
    };
  };

  $: metrics = calculateMetrics(energiatodistus, perusparannuspassi);
</script>

<div>
  <H5
    text={i18n(
      'perusparannuspassi.toimenpiteet.energiankulutuksen-muutos.header'
    )} />

  <dl class="ppp-description-list">
    <dt>
      {i18n(
        'perusparannuspassi.toimenpiteet.energiankulutuksen-muutos.ostoenergian-tarve-kaukolampo-muutos'
      )}
    </dt>
    <dd>
      {R.compose(
        Maybe.orSome('-'),
        R.map(formats.numberFormat)
      )(
        R.lift(R.subtract)(
          metrics.current.laskennallinenKulutus.kaukolampo,
          metrics.previous.laskennallinenKulutus.kaukolampo
        )
      )}
      {#each Maybe.toArray(metrics.current.laskennallinenKulutus.kaukolampo) as _}
        <VuosikulutusUnit />
      {/each}
    </dd>
    <dt>
      {i18n(
        'perusparannuspassi.toimenpiteet.energiankulutuksen-muutos.ostoenergian-tarve-sahko-muutos'
      )}
    </dt>
    <dd>
      {R.compose(
        Maybe.orSome('-'),
        R.map(formats.numberFormat)
      )(
        R.lift(R.subtract)(
          metrics.current.laskennallinenKulutus.sahko,
          metrics.previous.laskennallinenKulutus.sahko
        )
      )}
      {#each Maybe.toArray(metrics.current.laskennallinenKulutus.sahko) as _}
        <VuosikulutusUnit />
      {/each}
    </dd>
    <dt>
      {i18n(
        'perusparannuspassi.toimenpiteet.energiankulutuksen-muutos.ostoenergian-tarve-uusiutuvat-pat-muutos'
      )}
    </dt>
    <dd>
      {R.compose(
        Maybe.orSome('-'),
        R.map(formats.numberFormat)
      )(
        R.lift(R.subtract)(
          metrics.current.laskennallinenKulutus['uusiutuva-polttoaine'],
          metrics.previous.laskennallinenKulutus['uusiutuva-polttoaine']
        )
      )}
      {#each Maybe.toArray(metrics.current.laskennallinenKulutus['uusiutuva-polttoaine']) as _}
        <VuosikulutusUnit />
      {/each}
    </dd>
    <dt>
      {i18n(
        'perusparannuspassi.toimenpiteet.energiankulutuksen-muutos.ostoenergian-tarve-fossiiliset-pat-muutos'
      )}
    </dt>
    <dd>
      {R.compose(
        Maybe.orSome('-'),
        R.map(formats.numberFormat)
      )(
        R.lift(R.subtract)(
          metrics.current.laskennallinenKulutus['fossiilinen-polttoaine'],
          metrics.previous.laskennallinenKulutus['fossiilinen-polttoaine']
        )
      )}
      {#each Maybe.toArray(metrics.current.laskennallinenKulutus['fossiilinen-polttoaine']) as _}
        <VuosikulutusUnit />
      {/each}
    </dd>
    <dt>
      {i18n(
        'perusparannuspassi.toimenpiteet.energiankulutuksen-muutos.ostoenergian-tarve-kaukojaahdytys-muutos'
      )}
    </dt>
    <dd>
      {R.compose(
        Maybe.orSome('-'),
        R.map(formats.numberFormat)
      )(
        R.lift(R.subtract)(
          metrics.current.laskennallinenKulutus.kaukojaahdytys,
          metrics.previous.laskennallinenKulutus.kaukojaahdytys
        )
      )}
      {#each Maybe.toArray(metrics.current.laskennallinenKulutus.kaukojaahdytys) as _}
        <VuosikulutusUnit />
      {/each}
    </dd>
    <dt>
      {i18n(
        'perusparannuspassi.toimenpiteet.ostoenergian-kokonaistarve-vaiheen-jalkeen'
      )}
    </dt>
    <dd>
      {R.compose(
        Maybe.orSome('-'),
        R.map(formats.numberFormat)
      )(metrics.current.laskennallinenKulutus.total)}
      {#each Maybe.toArray(metrics.current.laskennallinenKulutus.total) as _}
        <VuosikulutusUnit />
      {/each}
    </dd>
    <dt>
      {i18n(
        'perusparannuspassi.toimenpiteet.uusiutuvan-energian-osuus-ostoenergian-kokonaistarpeesta'
      )}
    </dt>
    <dd>
      {R.compose(
        Maybe.orSome('-'),
        R.map(formats.percentFormat)
      )(metrics.current.uusiutuvanEnergianOsuusOstoenergianKokonaistarpeesta)}
    </dd>
    <dt>
      {i18n(
        'perusparannuspassi.toimenpiteet.ostoenergian-kokonaistarve-vaiheen-jalkeen-toteutunut-kulutus'
      )}
    </dt>
    <dd>
      {R.compose(
        Maybe.orSome('-'),
        R.map(formats.numberFormat)
      )(metrics.current.toteutunutKulutus.total)}
      {#each Maybe.toArray(metrics.current.toteutunutKulutus.total) as _}
        <VuosikulutusUnit />
      {/each}
    </dd>
    <dt>
      {i18n(
        'perusparannuspassi.toimenpiteet.toteutuneen-ostoenergian-vuotuinen-energiakustannus-arvio'
      )}
    </dt>
    <dd>
      {R.compose(
        Maybe.orSome('-'),
        R.map(formats.numberFormat)
      )(metrics.current.laskennallinenKustannus.total)}
      {#each Maybe.toArray(metrics.current.laskennallinenKustannus.total) as _}
        <EurosPerVuosiUnit />
      {/each}
    </dd>
    <dt>
      {i18n(
        'perusparannuspassi.toimenpiteet.energiankaytosta-aiheutuvat-hiilidioksidipaastot'
      )}
    </dt>
    <dd>
      {R.compose(
        Maybe.orSome('-'),
        R.map(formats.numberFormat),
        R.map(R.divide(R.__, 1000))
      )(metrics.current.laskennallinenCO2.total)}
      {#each Maybe.toArray(metrics.current.laskennallinenCO2.total) as _}
        <HiilidioksidiekvivalenttiTonnitPerVuosiUnit />
      {/each}
    </dd>
  </dl>
</div>
