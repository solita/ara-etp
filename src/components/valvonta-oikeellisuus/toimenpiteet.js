import * as R from 'ramda';
import * as Deep from '@Utility/deep-objects';
import * as Maybe from '@Utility/maybe-utils';
import * as dfns from 'date-fns';
import * as Either from '@Utility/either-utils';
import * as Locales from '@Language/locale-utils';

export const type = {
  verified: 0,
  anomaly: 1, // kevyt valvontamenettely

  // raskas valvontamenettely - asia avataan ashaan
  case: 2,
  rfi: {
    // tietopyyntö
    request: 3,
    reply: 4,
    order: 5,
    warning: 6
  },
  audit: {
    // valvonnan käsittely - auditoinnin tulokset + laatijan vastaus
    report: 7,
    reply: 8,
    order: 9,
    warning: 10
  },
  decision: {
    // päätökset
    prohibition: 11 // kieltopäätös
  },
  closed: 12
};

const types = R.invertObj(Deep.treeFlat('-', R.F, type));

export const typeKey = id => types[id];

export const isType = R.propEq('type-id');

const isDeadlineType = R.includes(R.__, [3, 5, 6, 7, 9, 10]);
export const isDialogType = R.includes(R.__, [0, 1, 2, 3, 5, 6, 9, 10, 11]);
const isResponseType = R.includes(R.__, [4, 8]);

export const hasDeadline = R.propSatisfies(isDeadlineType, 'type-id');
export const isResponse = R.propSatisfies(isResponseType, 'type-id');

export const isAuditCase = R.propSatisfies(R.gt(R.__, type.anomaly), 'type-id');
export const isAuditCaseToimenpideType = R.propSatisfies(
  R.includes(R.__, [3, 5, 6, 7, 9, 10, 11, 12]),
  'id'
);

export const isVerified = isType(type.verified);
export const isAnomaly = isType(type.anomaly);

export const defaultDeadline = typeId =>
  isDeadlineType(typeId)
    ? Maybe.Some(dfns.addMonths(new Date(), 1))
    : Maybe.None();

export const i18nKey = (toimenpide, key) =>
  R.join('.', [
    'valvonta.oikeellisuus.toimenpide',
    typeKey(toimenpide['type-id']),
    key
  ]);

export const emptyToimenpide = typeId => ({
  'type-id': typeId,
  'publish-time': Maybe.None(),
  'deadline-date': Either.Right(defaultDeadline(typeId)),
  'template-id': Maybe.None(),
  document: Maybe.None()
});

export const isDraft = R.compose(Maybe.isNone, R.prop('publish-time'));

export const templates = templatesByType => R.compose(
  R.defaultTo([]),
  R.prop(R.__, templatesByType),
  R.prop('type-id'));

const responseTypes = {
  'rfi-request': type.rfi.reply,
  'rfi-order': type.rfi.reply,
  'rfi-warning': type.rfi.reply,
  'audit-report': type.audit.reply,
  'audit-order': type.audit.reply,
  'audit-warning': type.audit.reply,
}

export const responseTypeFor = R.compose(
  Maybe.fromNull,
  typeId => responseTypes[typeKey(typeId)],
  R.prop('type-id'));