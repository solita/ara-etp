import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

export const isSelf = R.curry((whoami, id) =>
  R.compose(Maybe.exists(R.equals(whoami.id)), Maybe.fromNull)(id)
);

export const format = R.curry((selfLabel, valvojat, whoami, id) =>
  R.ifElse(
    isSelf(whoami),
    R.always(selfLabel),
    R.compose(
      Maybe.orSome('-'),
      R.map(valvoja => `${valvoja.etunimi} ${valvoja.sukunimi}`),
      R.chain(id => Maybe.find(R.propEq('id', id), valvojat)),
      Maybe.fromNull
    )
  )(id)
);
