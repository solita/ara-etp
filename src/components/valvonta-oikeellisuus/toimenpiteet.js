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
export const isDialogType = R.complement(R.equals(type.audit.report));
const isDocumentTemplateType = R.includes(R.__, [3, 5, 6, 7, 9, 10]);

export const hasDeadline = R.propSatisfies(isDeadlineType, 'type-id');
export const hasDocumentTemplate = R.propSatisfies(
  isDocumentTemplateType,
  'type-id'
);

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

export const emptyValvontamuistio = _ => ({
  'publish-time': Maybe.None(),
  'type-id': type.audit.report,
  'deadline-date': Either.Right(defaultDeadline(type.audit.report)),
  document: Maybe.None(),
  'template-id': Maybe.None(),
  /*'vakavuusluokka-id': Maybe.None(),
  virheet: []*/
});

export const isDraft = R.compose(Maybe.isNone, R.prop('publish-time'));

export const templates = templatesByType => R.compose(
  R.defaultTo([]),
  R.prop(R.__, templatesByType),
  R.prop('type-id'));
