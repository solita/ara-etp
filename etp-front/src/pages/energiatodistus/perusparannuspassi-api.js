import * as R from 'ramda';

import * as Either from '@Utility/either-utils';
import * as Future from '@Utility/future-utils';
import * as Fetch from '@/utils/fetch-utils.js';
import * as Maybe from '@Utility/maybe-utils.js';

import * as empty from '@Pages/energiatodistus/empty.js';
import * as deep from '@/utils/deep-objects.js';
import * as schema from './schema.js';

export const addPerusparannuspassi = R.curry((fetch, energiatodistusId) =>
  R.compose(
    R.map(deserialize),
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

// This is somewhat different route than with energiatodistus. With energiatodistus inputtable values out of schema
// are put into empty energiatodistus and then the empty is merged when deserializing.
const deserializeToimenpideEhdotukset = toimenpiteet =>
  R.concat(
    toimenpiteet,
    R.repeat(Maybe.None(), R.max(6 - R.length(toimenpiteet), 0))
  );

export const getPerusparannuspassi = R.curry((fetch, pppId) =>
  R.compose(
    R.map(deserialize),
    Fetch.responseAsJson,
    Future.encaseP(
      Fetch.getFetch(fetch)
    )
  )(`/api/private/perusparannuspassit/2026/${pppId}`)
);

export const putPerusparannuspassi = R.curry((fetch, pppId, perusparannuspassi) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(
      Fetch.fetchWithMethod(
        fetch,
        'put',
        `/api/private/perusparannuspassit/2026/${pppId}`
      )
    ),
    serialize
  )(perusparannuspassi)
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
      valid: Maybe.get,
      toimenpiteet: {
        'toimenpide-ehdotukset': deserializeToimenpideEhdotukset
      }
    },
    4
  )
};

const serializeToimenpideEhdotukset = R.compose(
  R.filter(R.isNotNil),
  R.map(Maybe.orSome(null))
);

const serializer = {
  vaiheet: R.repeat(
    {
      toimenpiteet: {
        'toimenpide-ehdotukset': serializeToimenpideEhdotukset
      }
    },
    4
  )
};

const transformationFromSchema = name =>
  R.compose(
    deep.filter(R.is(Function), R.complement(R.isNil)),
    deep.map(R.propSatisfies(R.is(Array), 'validators'), R.prop(name))
  )(schema.perusparannuspassi);

export const deserialize = R.compose(
  R.evolve(transformationFromSchema('deserialize')),
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
  R.omit(['id', 'tila-id', 'laatija-id']),
  R.evolve(transformationFromSchema('serialize')),
  R.evolve(serializer)
);
