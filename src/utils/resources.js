import * as R from 'ramda';
import * as Fetch from './fetch-utils';
import * as Future from './future-utils';
import * as Maybe from './maybe-utils';

const baseApiUrl = 'api/private';
const yritysApiUrl = `${baseApiUrl}/yritykset`;
const urlForYritysId = R.cond([
  [R.equals('all'), R.always(Maybe.of(yritysApiUrl))],
  [R.equals('new'), Maybe.None],
  [R.T, R.compose(Maybe.of, R.concat(`${yritysApiUrl}/`))]
]);

const kayttajaApiUrl = `${baseApiUrl}/kayttajat`;
const urlForKayttajaId = R.compose(Maybe.of, R.concat(`${kayttajaApiUrl}/`));

export const locationParts = R.compose(R.reject(R.isEmpty), R.split('/'));

export const kayttajaFuture = R.curry((fetch, locationParts) => {
  const [_, id] = locationParts;

  return R.compose(R.map(Fetch.fetchUrl(fetch)), urlForKayttajaId)(id);
});

export const yritysFuture = R.curry((fetch, locationParts) => {
  const [_, id] = locationParts;

  return R.compose(R.map(Fetch.fetchUrl(fetch)), urlForYritysId)(id);
});

export const parseResource = R.curry((fetch, location) =>
  R.compose(
    R.cond([
      [R.compose(R.equals('yritys'), R.head), yritysFuture(fetch)],
      [R.compose(R.equals('kayttaja'), R.head), kayttajaFuture(fetch)],
      [R.T, Maybe.None]
    ]),
    locationParts
  )(location)
);
