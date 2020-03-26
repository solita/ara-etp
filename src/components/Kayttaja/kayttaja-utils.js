import * as R from 'ramda';

import * as Future from '@Utility/future-utils';
import * as Fetch from '@Utility/fetch-utils';

export const kayttajaApi = `api/private/kayttajat`;

export const kayttajaForId = id => `${kayttajaApi}/${id}`;

export const laatijaForKayttajaId = id => `${kayttajaForId(id)}/laatija`;

export const kayttajaFuture = R.curry((fetch, id) =>
  R.compose(Fetch.fetchUrl(fetch), kayttajaForId)(id)
);

export const kayttajanLaatijaFuture = R.curry((fetch, id) =>
  R.compose(Fetch.fetchUrl(fetch), laatijaForKayttajaId)(id)
);

export const kayttajaAndLaatijaFuture = R.curry((fetch, id) =>
  R.compose(
    R.map(R.reduce(R.merge, {})),
    R.converge(Future.both, [
      kayttajaFuture(fetch),
      kayttajanLaatijaFuture(fetch)
    ])
  )(id)
);
