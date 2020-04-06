import * as R from 'ramda';

import * as Future from '@Utility/future-utils';
import * as Fetch from '@Utility/fetch-utils';

export const currentKayttajaApi = `/api/private/whoami`;

export const currentKayttajaFuture = fetch =>
  Fetch.fetchUrl(fetch, currentKayttajaApi);

export const kayttajaApi = `/api/private/kayttajat`;

export const kayttajaForId = id => `${kayttajaApi}/${id}`;

export const laatijaForKayttajaId = id => `${kayttajaForId(id)}/laatija`;

export const kayttajaFuture = R.curry((fetch, id) =>
  R.compose(Fetch.fetchUrl(fetch), kayttajaForId)(id)
);

export const kayttajanLaatijaFuture = R.curry((fetch, id) =>
  R.compose(Fetch.fetchUrl(fetch), laatijaForKayttajaId)(id)
);

export const putKayttajanLaatijaFuture = R.curry((fetch, id, laatija) =>
  Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', kayttajaForId(id)))(
    laatija
  )
);

export const kayttajaAndLaatijaFuture = R.curry((fetch, id) =>
  R.compose(
    R.map(([kayttaja, laatija]) => R.assoc('laatija', laatija, kayttaja)),
    R.converge(Future.both, [
      kayttajaFuture(fetch),
      kayttajanLaatijaFuture(fetch)
    ])
  )(id)
);

export const kayttajaHasAccessToResource = R.curry((roolit, kayttaja) =>
  R.compose(R.applyTo(roolit), R.includes, R.prop('rooli'))(kayttaja)
);
