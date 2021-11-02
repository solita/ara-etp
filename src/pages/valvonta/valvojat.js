import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

export const isSelf = R.curry((whoami, id) => whoami.id === id);

export const format = R.curry((selfLabel, valvojat, whoami, id) =>
  R.ifElse(
    isSelf(whoami),
    R.always(selfLabel),
    R.compose(
      Maybe.fold('', valvoja => `${valvoja.etunimi} ${valvoja.sukunimi}`),
      id => Maybe.find(R.propEq('id', id), valvojat)
    )
  )(id)
);
