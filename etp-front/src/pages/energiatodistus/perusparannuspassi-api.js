import * as R from 'ramda';

import * as Either from '@Utility/either-utils';
import * as Future from '@Utility/future-utils';
import * as Fetch from '@/utils/fetch-utils.js';
import * as Maybe from '@Utility/maybe-utils';

import * as empty from '@Pages/energiatodistus/empty.js';
import * as deep from '@/utils/deep-objects.js';

export const addPerusparannuspassi = R.curry((fetch, energiatodistusId) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(
      Fetch.fetchWithMethod(
        fetch,
        'post',
        '/api/private/perusparannuspassit/2026'
      )
    ),
    serialize,
    R.assoc('valid', true),
    empty.perusparannuspassi
  )(energiatodistusId)
);

const deserializer = {
  id: Maybe.get,
  valid: Maybe.get,
  'energiatodistus-id': Maybe.get,
  'laatija-id': Maybe.get,
  'passin-perustiedot': {
    'tayttaa-aplus-vaatimukset': Maybe.get,
    'tayttaa-a0-vaatimukset': Maybe.get
  },
  vaiheet: R.repeat(
    {
      'vaihe-nro': Maybe.get,
      valid: Maybe.get
    },
    4
  )
};

export const deserialize = R.compose(
  R.evolve(deserializer),
  deep.map(R.F, Maybe.fromNull)
);

export const serialize = R.compose(
  deep.map(
    Either.isEither,
    R.ifElse(
      R.allPass([R.complement(R.isNil), Either.isEither]),
      Either.orSome(null),
      R.identity
    )
  ),
  deep.map(
    Maybe.isMaybe,
    R.ifElse(
      R.allPass([R.complement(R.isNil), Maybe.isMaybe]),
      Maybe.orSome(null),
      R.identity
    )
  ),
  R.omit(['id', 'tila-id', 'laatija-id'])
);
