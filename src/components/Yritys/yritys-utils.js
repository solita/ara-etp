import * as R from 'ramda';
import * as Fetch from '../../utils/fetch-utils';
import * as Maybe from '../../utils/maybe-utils';
import * as Future from '../../utils/future-utils';

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
