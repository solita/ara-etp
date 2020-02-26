import * as R from 'ramda';
import * as Fetch from '../../utils/fetch-utils';
import * as Maybe from '../../utils/maybe-utils';
import * as Future from '../../utils/future-utils';
import * as validation from '../../utils/validation';

const yritysApi = `/api/yritykset/`;

export const urlForYritysId = id => `${yritysApi}${id}`;

export const yritysDeserialize = R.evolve({
  verkkolaskuosoite: Maybe.fromNull,
  wwwosoite: Maybe.fromNull
});

export const yritysSerialize = R.evolve({
  verkkolaskuosoite: Maybe.fold(null, R.identity),
  wwwosoite: Maybe.fold(null, R.identity)
});

export const yritysFetchFuture = R.curry((fetch, id) =>
  R.compose(
    R.map(updateYritysAction),
    R.map(yritysDeserialize),
    Fetch.fetchFromUrl(fetch),
    urlForYritysId
  )(id)
);

export const newYritysAction = () => ({
  api: yritysApi,
  method: 'post',
  yritys: {
    ytunnus: '',
    nimi: '',
    jakeluosoite: '',
    postinumero: '',
    postitoimipaikka: '',
    maa: '',
    verkkolaskuosoite: Maybe.None(),
    wwwosoite: Maybe.None()
  }
});

export const updateYritysAction = R.curry(yritys => ({
  api: urlForYritysId(yritys.id),
  method: 'put',
  yritys
}));

export const matchErrorWithAction = R.ifElse(
  R.equals(404),
  R.always(Future.resolve(newYritysAction())),
  Future.reject
);

export const isFilled = R.complement(R.isEmpty);

export const isUrl = R.test(
  /(http(s)?:\/\/.)?(www\.)?[-a-zA-Z0-9@:%._\+~#=]{2,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_\+.~#?&//=]*)/
);

export const maybeValidate = validator => Maybe.fold(true, validator);

export const isPostinumero = R.test(/^\d{5}$/);

export const formValidators = {
  ytunnus: R.allPass([isFilled, validation.isValidYtunnus]),
  nimi: isFilled,
  wwwosoite: R.anyPass([R.isEmpty, isUrl]),
  jakeluosoite: isFilled,
  postinumero: isPostinumero,
  postitoimipaikka: isFilled,
  maa: isFilled,
  verkkolaskuosoite: R.always(true)
};

export const validateYritysForm = R.curry((validators, yritys) =>
  R.compose(
    R.evolve(R.__, yritys),
    R.assoc('wwwosoite', maybeValidate(validators.wwwosoite)),
    R.assoc('verkkolaskuosoite', maybeValidate(validators.verkkolaskuosoite))
  )(validators)
);
