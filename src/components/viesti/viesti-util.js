import * as Maybe from '@Utility/maybe-utils';
import * as R from 'ramda';
import * as Kayttajat from '@Utility/kayttajat';

export const emptyKetju = _ => ({
  kayttajat: [],
  'kayttajarooli-id': Maybe.None(),
  'kayttajaryhma-id': Maybe.None(),
  'energiatodistus-id': Maybe.None(),
  subject: '',
  body: ''
});

export const formatSender = R.curry((i18n, viesti) =>
  R.ifElse(
    R.propSatisfies(Kayttajat.isPaakayttajaRole, 'rooli-id'),
    from => i18n('viesti.valvoja') + ' (' + from.etunimi + ')',
    Kayttajat.fullName
  )(viesti)
);

export const isSelfSent = R.curry((viesti, currentUser) =>
  R.propEq('id', R.path(['from', 'id'], viesti), currentUser)
);
