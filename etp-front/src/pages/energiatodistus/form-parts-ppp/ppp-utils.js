import * as Maybe from '@Utility/maybe-utils';
import * as R from 'ramda';
import * as formats from '@Utility/formats';
import * as fxmath from '@Utility/fxmath';
import * as Either from '@Utility/either-utils';
import * as EitherMaybe from '@Utility/either-maybe';

export const formatCost = R.compose(
  Maybe.orSome('-'),
  R.map(R.compose(formats.currencyFormat, fxmath.round(2))),
  R.lift(R.divide(R.__, 100))
);

export const formatCostDifference = R.compose(
  Maybe.orSome('-'),
  R.map(
    R.ifElse(
      a => a > 0,
      R.compose(s => '+' + formats.currencyFormat(s), fxmath.round(2)),
      R.compose(formats.currencyFormat, fxmath.round(2))
    )
  ),
  R.lift(R.divide(R.__, 100))
);

export const energiamuodot = [
  {
    pppLaskennallinenEnergiamuoto: 'ostoenergian-tarve-kaukolampo',
    pppToteutunutEnergiamuoto: 'toteutunut-ostoenergia-kaukolampo',
    etEnergiamuoto: 'kaukolampo',
    getToteutunutEnergiaFromEt: et =>
      et['toteutunut-ostoenergiankulutus']['ostettu-energia'][
        'kaukolampo-vuosikulutus'
      ],
    pppPriceField: 'kaukolampo-hinta',
    paastokerroin: 0.059
  },
  {
    pppLaskennallinenEnergiamuoto: 'ostoenergian-tarve-sahko',
    pppToteutunutEnergiamuoto: 'toteutunut-ostoenergia-sahko',
    etEnergiamuoto: 'sahko',
    getToteutunutEnergiaFromEt: et =>
      et['toteutunut-ostoenergiankulutus']['ostettu-energia'][
        'kokonaissahko-vuosikulutus'
      ],

    pppPriceField: 'sahko-hinta',
    paastokerroin: 0.05
  },
  {
    pppLaskennallinenEnergiamuoto: 'ostoenergian-tarve-uusiutuvat-pat',
    pppToteutunutEnergiamuoto: 'toteutunut-ostoenergia-uusiutuvat-pat',
    etEnergiamuoto: 'uusiutuva-polttoaine',
    getToteutunutEnergiaFromEt: _ => Either.Right(Maybe.None()),
    pppPriceField: 'uusiutuvat-pat-hinta',
    paastokerroin: 0.027
  },
  {
    pppLaskennallinenEnergiamuoto: 'ostoenergian-tarve-fossiiliset-pat',
    pppToteutunutEnergiamuoto: 'toteutunut-ostoenergia-fossiiliset-pat',
    etEnergiamuoto: 'fossiilinen-polttoaine',
    getToteutunutEnergiaFromEt: _ => Either.Right(Maybe.None()),
    pppPriceField: 'fossiiliset-pat-hinta',
    paastokerroin: 0.306
  },
  {
    pppLaskennallinenEnergiamuoto: 'ostoenergian-tarve-kaukojaahdytys',
    pppToteutunutEnergiamuoto: 'toteutunut-ostoenergia-kaukojaahdytys',
    etEnergiamuoto: 'kaukojaahdytys',
    getToteutunutEnergiaFromEt: et =>
      et['toteutunut-ostoenergiankulutus']['ostettu-energia'][
        'kaukojaahdytys-vuosikulutus'
      ],
    pppPriceField: 'kaukojaahdytys-hinta',
    paastokerroin: 0.014
  }
];

const addTotal = metrics =>
  R.assoc(
    'total',
    R.compose(
      R.reduce(
        (acc, cost) => (Maybe.isSome(acc) ? R.lift(R.add)(acc, cost) : cost),
        Maybe.None()
      ),
      R.filter(Maybe.isSome),
      R.values
    )(metrics),
    metrics
  );

const etLaskennallinenKulutus = et =>
  R.compose(
    addTotal,
    R.fromPairs,
    R.map(({ etEnergiamuoto }) => [
      etEnergiamuoto,
      EitherMaybe.toMaybe(
        et.tulokset['kaytettavat-energiamuodot'][etEnergiamuoto]
      )
    ])
  )(energiamuodot);

const etToteutunutKulutus = et =>
  R.compose(
    addTotal,
    R.fromPairs,
    R.map(({ etEnergiamuoto, getToteutunutEnergiaFromEt }) => [
      etEnergiamuoto,
      EitherMaybe.toMaybe(getToteutunutEnergiaFromEt(et))
    ])
  )(energiamuodot);

const pppVaiheLaskennallinenKulutus = vaihe =>
  R.compose(
    addTotal,
    R.fromPairs,
    R.map(({ etEnergiamuoto, pppLaskennallinenEnergiamuoto }) => [
      etEnergiamuoto,
      EitherMaybe.toMaybe(vaihe.tulokset[pppLaskennallinenEnergiamuoto])
    ])
  )(energiamuodot);

const pppVaiheToteutunutKulutus = vaihe =>
  R.compose(
    addTotal,
    R.fromPairs,
    R.map(({ etEnergiamuoto, pppToteutunutEnergiamuoto }) => [
      etEnergiamuoto,
      EitherMaybe.toMaybe(vaihe.tulokset[pppToteutunutEnergiamuoto])
    ])
  )(energiamuodot);

const calculateCosts = (energia, ppp) =>
  R.compose(
    addTotal,
    R.fromPairs,
    R.map(({ etEnergiamuoto, pppPriceField }) => [
      etEnergiamuoto,
      R.lift(R.multiply)(
        energia[etEnergiamuoto],
        EitherMaybe.toMaybe(ppp.tulokset[pppPriceField])
      )
    ])
  )(energiamuodot);

const calculateCO2Emissions = energia =>
  R.compose(
    addTotal,
    R.fromPairs,
    R.map(({ etEnergiamuoto, paastokerroin }) => [
      etEnergiamuoto,
      R.lift(R.multiply)(energia[etEnergiamuoto], Maybe.Some(paastokerroin))
    ])
  )(energiamuodot);

export const calculateDerivedValues = (energiatodistus, perusparannuspassi) => {
  const etMetrics = {
    laskennallinenKulutus: etLaskennallinenKulutus(energiatodistus),
    toteutunutKulutus: etToteutunutKulutus(energiatodistus)
  };

  const pppMetrics = R.map(vaihe => {
    const laskennallinenKulutus = pppVaiheLaskennallinenKulutus(vaihe);
    const uusiutuvanEnergianHyodynnettyOsuus = EitherMaybe.toMaybe(
      vaihe.tulokset['uusiutuvan-energian-hyodynnetty-osuus']
    );

    return {
      'vaiheen-alku-pvm': EitherMaybe.toMaybe(
        vaihe.tulokset['vaiheen-alku-pvm']
      ),
      laskennallinenKulutus,
      toteutunutKulutus: pppVaiheToteutunutKulutus(vaihe),
      uusiutuvanEnergianHyodynnettyOsuus,
      uusiutuvanEnergianOsuusOstoenergianKokonaistarpeesta: R.lift(R.divide)(
        uusiutuvanEnergianHyodynnettyOsuus,
        laskennallinenKulutus.total
      )
    };
  }, perusparannuspassi.vaiheet);

  return R.map(
    metrics =>
      R.compose(
        R.assoc(
          'laskennallinenKustannus',
          calculateCosts(metrics.laskennallinenKulutus, perusparannuspassi)
        ),
        R.assoc(
          'toteutunutKustannus',
          calculateCosts(metrics.toteutunutKulutus, perusparannuspassi)
        ),
        R.assoc(
          'laskennallinenCO2',
          calculateCO2Emissions(metrics.laskennallinenKulutus)
        )
      )(metrics),
    [etMetrics, ...pppMetrics]
  );
};

/**
 * Format a ppp vaihe heading using a possible starting year.
 *
 * If `startingYear` contains a value, the returned string is:
 *   "<title> (<startingYear>) <unit>"
 *
 * If `startingYear` is absent, the fallback will be:
 *   "<title> (<noStartingYear>)"
 *
 * Parameters:
 *  - title: string — main heading text
 *  - unit: string — unit string appended when starting year is present
 *  - startingYear: Either [*, Maybe number] value — mapped to produce the year;
 *    if absent, fallback is used
 *  - noStartingYear: string — text to use inside parentheses when starting year
 *    is missing
 *
 * Returns: string
 */
export const formatVaiheHeading = (title, unit, startingYear, noStartingYear) =>
  R.compose(
    EitherMaybe.orSome(title + ' (' + noStartingYear + ')'),
    EitherMaybe.map(aloitusvuosi => `${title} (${aloitusvuosi}) ${unit}`)
  )(startingYear);
