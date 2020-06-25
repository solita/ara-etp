import * as R from 'ramda';
import * as Fetch from '@Utility/fetch-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';
import * as Future from '@Utility/future-utils';
import * as validation from '@Utility/validation';

const yritysApi = `/api/private/yritykset`;

export const urlForYritysId = id => `${yritysApi}/${id}`;

export const deserialize = R.evolve({
  'vastaanottajan-tarkenne': Maybe.fromNull,
  maa: Either.Right,
  verkkolaskuosoite: Maybe.fromNull,
  verkkolaskuoperaattori: Maybe.fromNull
});

export const serialize = R.compose(
  R.evolve({
    'vastaanottajan-tarkenne': Maybe.orSome(null),
    maa: Either.right,
    verkkolaskuosoite: Maybe.orSome(null),
    verkkolaskuoperaattori: Maybe.orSome(null),
  }),
  R.dissoc('id')
);

export const emptyYritys = () => ({
  ytunnus: '',
  nimi: '',
  'vastaanottajan-tarkenne': Maybe.None(),
  jakeluosoite: '',
  postinumero: '',
  postitoimipaikka: '',
  maa: '',
  laskutuskieli: Maybe.None(),
  verkkolaskuosoite: Maybe.None(),
  verkkolaskuoperaattori: Maybe.None()
});

export const formSchema = () => ({
  ytunnus: [validation.isRequired, validation.ytunnusValidator],
  nimi: [
    validation.isRequired,
    validation.minLengthConstraint(2),
    validation.maxLengthConstraint(200)
  ],
  'vastaanottajan-tarkenne': R.map(validation.liftValidator, [
    validation.isRequired,
    validation.minLengthConstraint(2),
    validation.maxLengthConstraint(200)]),
  jakeluosoite: [validation.isRequired],
  postinumero: [validation.isRequired, validation.postinumeroValidator],
  postitoimipaikka: [
    validation.isRequired,
    validation.minLengthConstraint(2),
    validation.maxLengthConstraint(200)
  ],
  maa: [],
  verkkolaskuosoite: [],
  verkkolaskuoperaattori: [],
});

export const formParsers = () => ({
  ytunnus: R.trim,
  nimi: R.trim,
  'vastaanottajan-tarkenne': R.compose(Maybe.fromEmpty, R.trim),
  jakeluosoite: R.trim,
  postinumero: R.trim,
  postitoimipaikka: R.trim,
  maa: R.trim,
  verkkolaskuosoite: R.compose(Maybe.fromEmpty, R.trim),
  verkkolaskuoperaattori: R.compose(Maybe.fromEmpty, R.trim)
});

export const getYritysByIdFuture = R.curry((fetch, id) =>
  R.compose(
    R.map(deserialize),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    urlForYritysId
  )(id)
);

export const getAllYrityksetFuture = fetch =>
  R.compose(
    R.map(R.map(deserialize)),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch))
  )(yritysApi);

export const putYritysByIdFuture = R.curry((fetch, id, yritys) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', urlForYritysId(id))),
    serialize
  )(yritys)
);

export const postYritysFuture = R.curry((fetch, yritys) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', yritysApi)),
    serialize
  )(yritys)
);
