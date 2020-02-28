import * as R from 'ramda';
import * as Fetch from '../../utils/fetch-utils';
import * as Maybe from '../../utils/maybe-utils';
import * as Future from '../../utils/future-utils';
import * as validation from '../../utils/validation';

const yritysApi = `/api/yritykset`;

export const urlForYritysId = id => `${yritysApi}/${id}`;

export const yritysDeserialize = R.evolve({
  verkkolaskuosoite: Maybe.fromNull,
  wwwosoite: Maybe.fromNull
});

export const yritysSerialize = R.compose(
  R.evolve({
    verkkolaskuosoite: Maybe.fold(null, R.identity),
    wwwosoite: Maybe.fold(null, R.identity)
  }),
  R.dissoc('id')
);

export const emptyYritys = () => ({
  ytunnus: '',
  nimi: '',
  jakeluosoite: '',
  postinumero: '',
  postitoimipaikka: '',
  maa: '',
  verkkolaskuosoite: Maybe.None(),
  wwwosoite: Maybe.None()
});

export const formValidators = () => ({
  ytunnus: R.allPass([validation.isFilled, validation.isValidYtunnus]),
  nimi: validation.isFilled,
  wwwosoite: R.anyPass([R.isEmpty, validation.isUrl]),
  jakeluosoite: validation.isFilled,
  postinumero: validation.isPostinumero,
  postitoimipaikka: validation.isFilled,
  maa: validation.isFilled,
  verkkolaskuosoite: R.always(true)
});

export const formTransformers = () => ({
  ytunnus: R.trim,
  nimi: R.trim,
  wwwosoite: R.trim,
  jakeluosoite: R.trim,
  postinumero: R.trim,
  postitoimipaikka: R.trim,
  maa: R.trim,
  verkkolaskuosoite: R.trim
});

export const validateYritysForm = R.curry((validators, yritys) =>
  R.compose(
    R.evolve(R.__, yritys),
    R.assoc('wwwosoite', Maybe.fold(true, validators.wwwosoite)),
    R.assoc('verkkolaskuosoite', Maybe.fold(true, validators.verkkolaskuosoite))
  )(validators)
);

export const getYritysByIdFuture = R.curry((fetch, id) =>
  R.compose(
    R.map(yritysDeserialize),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    urlForYritysId
  )(id)
);

export const putYritysByIdFuture = R.curry((fetch, id, yritys) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', urlForYritysId(id))),
    yritysSerialize
  )(yritys)
);

export const postYritysFuture = R.curry((fetch, yritys) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', yritysApi)),
    yritysSerialize
  )(yritys)
);
