import * as R from 'ramda';
import * as Deep from '@Utility/deep-objects';
import * as Maybe from '@Utility/maybe-utils';
import * as dfns from 'date-fns';
import * as Either from '@Utility/either-utils';

export const type = {
  case: 0,
  rfi: {
    // tietopyyntö
    request: 1,
    order: 2,
    warning: 3
  },
  decision: {
    // päätökset
    order: 4 // käskypäätös
  },
  closed: 5,
  'court-hearing': 6,
  'decision-order': {
    'hearing-letter': 7,
    'actual-decision': 8,
    'notice-first-mailing': 9,
    'notice-second-mailing': 10,
    'notice-bailiff': 11,
    'waiting-for-deadline': 12
  },
  'penalty-decision': {
    'hearing-letter': 14,
    'notice-first-mailing': 16
  }
};

const types = R.invertObj(Deep.treeFlat('-', R.F, type));

export const typeKey = id => types[id];

export const isType = R.propEq('type-id');

const isDeadlineType = R.includes(
  R.__,
  [1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12, 14, 16]
);
export const hasDeadline = R.propSatisfies(isDeadlineType, 'type-id');

export const isCloseCase = isType(type.closed);
export const isAuditCase = R.complement(isType(type.closed));
export const isAuditCaseToimenpideType = R.propSatisfies(
  R.includes(R.__, R.range(1, 22)),
  'id'
);

const defaultDeadlineForTypeId = typeId => {
  switch (typeId) {
    case R.path(['decision-order', 'hearing-letter'], type):
    case R.path(['penalty-decision', 'hearing-letter'], type):
      return Maybe.Some(dfns.addWeeks(new Date(), 2));
    case R.path(['decision-order', 'notice-bailiff'], type):
    case R.path(['decision-order', 'waiting-for-deadline'], type):
      return Maybe.Some(dfns.addDays(new Date(), 30));
    case R.path(['decision-order', 'actual-decision'], type):
    case R.path(['decision-order', 'notice-first-mailing'], type):
    case R.path(['decision-order', 'notice-second-mailing'], type):
    case R.path(['penalty-decision', 'notice-first-mailing'], type):
      return Maybe.Some(dfns.addDays(new Date(), 37));
    default:
      return Maybe.Some(dfns.addMonths(new Date(), 1));
  }
};

export const defaultDeadline = typeId =>
  isDeadlineType(typeId) ? defaultDeadlineForTypeId(typeId) : Maybe.None();

export const i18nKey = (toimenpide, key) =>
  R.join('.', [
    'valvonta.kaytto.toimenpide',
    typeKey(toimenpide['type-id']),
    key
  ]);

const defaultTemplateId = (typeId, templatesByType) => {
  const templates = R.defaultTo([], templatesByType[typeId]);
  return R.length(templates) === 1
    ? Maybe.Some(R.head(templates).id)
    : Maybe.None();
};

export const emptyToimenpide = (
  typeId,
  templatesByType,
  {
    fine = 800,
    departmentHeadTitleFi = null,
    departmentHeadTitleSv = null,
    departmentHeadName = null,
    osapuoliIds = []
  } = {}
) => {
  const toimenpide = {
    'type-id': typeId,
    'publish-time': Maybe.None(),
    'deadline-date': Either.Right(defaultDeadline(typeId)),
    'template-id': defaultTemplateId(typeId, templatesByType),
    description: Maybe.None()
  };

  switch (typeId) {
    case R.path(['decision-order', 'hearing-letter'], type):
    case R.path(['penalty-decision', 'hearing-letter'], type):
      return R.assocPath(
        ['type-specific-data', 'fine'],
        Maybe.Some(fine),
        toimenpide
      );

    case R.path(['decision-order', 'actual-decision'], type):
      return R.assoc(
        'type-specific-data',
        {
          fine: Maybe.Some(fine),
          'recipient-answered': false,
          'answer-commentary-fi': Maybe.None(),
          'answer-commentary-sv': Maybe.None(),
          'statement-fi': Maybe.None(),
          'statement-sv': Maybe.None(),
          'osapuoli-specific-data': R.map(
            osapuoliId => ({
              'osapuoli-id': osapuoliId,
              'hallinto-oikeus-id': Maybe.None(),
              document: true
            }),
            osapuoliIds
          ),
          'department-head-title-fi': Maybe.fromNull(departmentHeadTitleFi),
          'department-head-title-sv': Maybe.fromNull(departmentHeadTitleSv),
          'department-head-name': Maybe.fromNull(departmentHeadName)
        },
        toimenpide
      );
    case R.path(['decision-order', 'notice-bailiff'], type):
      return R.assoc(
        'type-specific-data',
        {
          'osapuoli-specific-data': R.map(
            osapuoliId => ({
              'osapuoli-id': osapuoliId,
              'karajaoikeus-id': Maybe.None(),
              'haastemies-email': Maybe.None(),
              document: true
            }),
            osapuoliIds
          )
        },
        toimenpide
      );
    default:
      return toimenpide;
  }
};

export const isDraft = R.compose(Maybe.isNone, R.prop('publish-time'));

export const hasTemplate = R.compose(Maybe.isSome, R.prop('template-id'));

export const templates = templatesByType =>
  R.compose(R.defaultTo([]), R.prop(R.__, templatesByType), R.prop('type-id'));

export const time = R.converge(Maybe.orSome, [
  R.prop('create-time'),
  R.prop('publish-time')
]);

export const sendTiedoksi = isType(type.rfi.request);

/**
 * Takes an array of toimenpidetype IDs and a toimenpide object
 * and checks if its type id is in the array
 * @param {number[]} toimenpideTypeIds
 * @param {Object} toimenpide
 * @returns {boolean} is toimenpidetype of the given toimenpide one in the array
 */
export const isToimenpideOfGivenTypes = R.curry(
  (toimenpideTypeIds, toimenpide) =>
    R.compose(
      R.includes(R.__, toimenpideTypeIds),
      R.prop('type-id')
    )(toimenpide)
);

/**
 * Given a key, return a function that takes an array of objects
 * and returns an array of ids of objects where the given key has value true
 * @param {string} key
 * @return {Function}
 */
const findIdsOfObjectsWhereGivenKeyHasValueTrue = key =>
  R.compose(R.map(R.prop('id')), R.filter(R.propEq(key, true)));

/**
 * Given an array of toimenpidetype objects of form {id: Number, 'manually-deliverable: Boolean},
 * return an array of toimenpidetype IDs of the manually deliverable toimenpidetypes
 * @param {Object[]} toimenpideTypes
 * @returns {number[]} Toimenpide type IDs of manuallly deliverable types
 */
export const manuallyDeliverableToimenpideTypes =
  findIdsOfObjectsWhereGivenKeyHasValueTrue('manually-deliverable');

/**
 * Given an array of toimenpidetype objects of form {id: Number, 'allow-comments: Boolean},
 * return an array of toimenpidetype IDs of the toimenpidetypes that allow comments
 * @param {Object[]} toimenpideTypes
 * @returns {number[]} Toimenpide type IDs of types that allow comments
 */
export const toimenpideTypesThatAllowComments =
  findIdsOfObjectsWhereGivenKeyHasValueTrue('allow-comments');

export const hasFine = toimenpide =>
  R.hasPath(['type-specific-data', 'fine'], toimenpide);

export const isDecisionOrderActualDecision = isType(
  R.path(['decision-order', 'actual-decision'], type)
);

export const isNoticeBailiff = isType(
  R.path(['decision-order', 'notice-bailiff'], type)
);

/**
 * These toimenpide types have a osapuoli specific boolean field document
 */
export const hasOptionalDocument = R.anyPass([
  isDecisionOrderActualDecision,
  isNoticeBailiff
]);

export const isDecisionOrderHearingLetter = isType(
  R.path(['decision-order', 'hearing-letter'], type)
);

export const isPenaltyDecisionHearingLetter = isType(
  R.path(['penalty-decision', 'hearing-letter'], type)
);

/**
 * Given an array of toimenpide objects, returns the fine found using the toimenpidetype predicate function parameter
 * @param {Function} toimenpidetypePredicate
 * @param {Object[]} toimenpiteet
 * @return {number}
 */
export const findFineFromToimenpiteet = R.compose(
  R.path(['type-specific-data', 'fine']),
  R.head,
  R.sort((a, b) =>
    dfns.compareDesc(R.prop('create-time', a), R.prop('create-time', b))
  ),
  R.filter
);

/**
 * Checks if käskypäätös / varsinainen päätös toimenpide has osapuoli-specific-data field
 * document set to true for the given osapuoliId
 * @param toimenpide
 * @param osapuoliId
 * @return {boolean}
 */
export const documentExistsForOsapuoli = (toimenpide, osapuoliId) => {
  return R.compose(
    R.prop('document'),
    R.find(R.propEq('osapuoli-id', osapuoliId)),
    R.path(['type-specific-data', 'osapuoli-specific-data'])
  )(toimenpide);
};

export const courtDataIndexForOsapuoli = (toimenpide, osapuoliId) =>
  R.findIndex(
    R.propEq('osapuoli-id', osapuoliId),
    R.path(['type-specific-data', 'osapuoli-specific-data'], toimenpide)
  );
