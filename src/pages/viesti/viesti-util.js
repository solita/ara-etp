import * as Maybe from '@Utility/maybe-utils';
import * as R from 'ramda';
import * as Kayttajat from '@Utility/kayttajat';

export const isKasittelija = R.anyPass([
  Kayttajat.isPaakayttaja,
  Kayttajat.isLaskuttaja
]);

export const isAllowedToSendToEveryone = isKasittelija;

export const defaultKetju = (energiatodistus, whoami) => ({
  vastaanottajat: isAllowedToSendToEveryone(whoami)
    ? Maybe.fold(
        [],
        R.compose(Maybe.toArray, R.prop('laatija-id')),
        energiatodistus
      )
    : [],
  'vastaanottajaryhma-id': isAllowedToSendToEveryone(whoami)
    ? Maybe.None()
    : Maybe.Some(0),
  'energiatodistus-id': R.map(R.prop('id'), energiatodistus),
  subject: '',
  body: ''
});

export const emptyKetju = _ => ({
  vastaanottajat: [],
  'vastaanottajaryhma-id': Maybe.None(),
  'energiatodistus-id': Maybe.None(),
  subject: '',
  body: ''
});

const valvojaName = i18n => valvoja =>
  i18n('viesti.valvoja') + ' (' + valvoja.etunimi + ')';
const systemName = i18n => R.always(i18n('viesti.system'));

export const formatUser = i18n =>
  R.cond([
    [
      R.propSatisfies(Kayttajat.isPaakayttajaRole, 'rooli-id'),
      valvojaName(i18n)
    ],
    [R.propSatisfies(Kayttajat.isSystemRole, 'rooli-id'), systemName(i18n)],
    [R.T, Kayttajat.fullName]
  ]);

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

export const findKetjuVastaanottajaryhma = (vastaanottajaryhmat, ketju) =>
  Maybe.find(
    R.propEq('id', R.prop('vastaanottajaryhma-id', ketju)),
    vastaanottajaryhmat
  );

export const fullName = people =>
  R.compose(
    R.join(' '),
    R.juxt([R.prop('etunimi'), R.prop('sukunimi')]),
    R.find(R.__, people),
    R.propEq('id')
  );

export const hasUnreadViesti = R.compose(
  R.not,
  R.isNil,
  R.find(R.propSatisfies(Maybe.isNone, 'read-time'))
);
