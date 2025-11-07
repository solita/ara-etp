<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EitherMaybe from '@Utility/either-maybe';
  import * as ETUtils from '@Pages/energiatodistus/energiatodistus-utils';
  import { _ } from '@Language/i18n';
  import * as formats from '@/utils/formats.js';

  const i18n = $_;

  export let energiatodistus;
  export let perusparannuspassi;
  export let pppSchema;
  export let vaihe;

  const vaiheIndex = vaihe => vaihe['vaihe-nro'] - 1;

  const laskennallisetOstoenergiat = [
    {
      ppp_energiamuoto: 'ostoenergian-tarve-kaukolampo',
      et_energiamuoto: 'kaukolampo'
    },
    { ppp_energiamuoto: 'ostoenergian-tarve-sahko', et_energiamuoto: 'sahko' },
    {
      ppp_energiamuoto: 'ostoenergian-tarve-uusiutuvat-pat',
      et_energiamuoto: 'uusiutuva-polttoaine'
    },
    {
      ppp_energiamuoto: 'ostoenergian-tarve-fossiiliset-pat',
      et_energiamuoto: 'fossiilinen-polttoaine'
    },
    {
      ppp_energiamuoto: 'ostoenergian-tarve-kaukojaahdytys',
      et_energiamuoto: 'kaukojaahdytys'
    }
  ];

  const etEnergiamuodotFromPppVaihe = vaihe =>
    R.compose(
      R.fromPairs,
      R.map(({ ppp_energiamuoto, et_energiamuoto }) => [
        et_energiamuoto,
        vaihe[ppp_energiamuoto]
      ])
    )(laskennallisetOstoenergiat);

  const eluku = {
    calculate: ({ energiatodistus, perusparannuspassi, vaihe }) =>
      ETUtils.eluku(
        energiatodistus.versio,
        energiatodistus.lahtotiedot['lammitetty-nettoala'],
        etEnergiamuodotFromPppVaihe(
          perusparannuspassi.vaiheet[vaiheIndex(vaihe)].tulokset
        )
      ),
    format: Maybe.orSome('')
  };
</script>

<dl class="ppp-description-list">
  <dt>
    {i18n('perusparannuspassi.toimenpiteet.vaihe-alkaa')}
  </dt>
  <dd>
    {R.compose(
      EitherMaybe.orSome('-'),
      R.map(R.map(formats.yearFormat))
    )(
      R.view(
        R.lensPath([
          'vaiheet',
          vaiheIndex(vaihe),
          'tulokset',
          'vaiheen-alku-pvm'
        ]),
        perusparannuspassi
      )
    )}
  </dd>
  <dt>
    {i18n('perusparannuspassi.toimenpiteet.e-luokka-jalkeen')}
  </dt>
  <dd>TODO E-luokka</dd>
  <dt>
    {i18n('perusparannuspassi.toimenpiteet.e-luku-jalkeen')}
  </dt>
  <dd>
    {R.compose(
      eluku.format,
      eluku.calculate
    )({
      energiatodistus,
      perusparannuspassi,
      vaihe
    })}
  </dd>
</dl>
