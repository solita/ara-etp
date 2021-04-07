import * as Maybe from '@Utility/maybe-utils';
import * as R from 'ramda';
import * as Kayttajat from '@Utility/kayttajat';

export const emptyKetju = _ => ({
  vastaanottajat: [],
  'vastaanottajaryhma-id': Maybe.None(),
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

const groups = ['valvojat', 'laatijat'];
export const group = R.compose(R.map(parseInt), R.invertObj)(groups);
export const groupKey = id => groups[id];

export const isValvojatGroup = R.equals(group.valvojat);
export const isLaatijatGroup = R.equals(group.laatijat);

export const isForValvojat = R.propSatisfies(
  isValvojatGroup,
  'vastaanottajaryhma-id'
);
export const isForLaatijat = R.propSatisfies(
  isLaatijatGroup,
  'vastaanottajaryhma-id'
);
