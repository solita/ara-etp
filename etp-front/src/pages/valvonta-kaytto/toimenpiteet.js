import * as R from 'ramda';
import * as Deep from '@Utility/deep-objects';
import * as Maybe from '@Utility/maybe-utils';
import * as dfns from 'date-fns';
import * as Either from '@Utility/either-utils';
import * as Osapuolet from '@Pages/valvonta-kaytto/osapuolet';

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
    'actual-decision': 15,
    'notice-first-mailing': 16,
    'notice-second-mailing': 17,
    'notice-bailiff': 18,
    'waiting-for-deadline': 19
  },
  'penalty-list-delivery-in-progress': 21,
  reopen: 22
};

const types = R.invertObj(Deep.treeFlat('-', R.F, type));

export const typeKey = id => types[id];

export const isType = R.propEq('type-id');

const isDeadlineType = R.includes(
  R.__,
  [1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12, 14, 15, 16, 17, 18, 19]
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
    case R.path(['penalty-decision', 'notice-bailiff'], type):
    case R.path(['penalty-decision', 'waiting-for-deadline'], type):
      return Maybe.Some(dfns.addDays(new Date(), 30));
    case R.path(['decision-order', 'actual-decision'], type):
    case R.path(['penalty-decision', 'actual-decision'], type):
    case R.path(['decision-order', 'notice-first-mailing'], type):
    case R.path(['decision-order', 'notice-second-mailing'], type):
    case R.path(['penalty-decision', 'notice-first-mailing'], type):
    case R.path(['penalty-decision', 'notice-second-mailing'], type):
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
    osapuolis = [],
    defaultStatementFi = null,
    defaultStatementSv = null
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
          'osapuoli-specific-data': R.map(
            osapuoli => ({
              osapuoli: {
                id: R.prop('id', osapuoli),
                type: Osapuolet.getOsapuoliType(osapuoli)
              },
              'recipient-answered': false,
              'answer-commentary-fi': Maybe.None(),
              'answer-commentary-sv': Maybe.None(),
              'statement-fi': Maybe.None(),
              'statement-sv': Maybe.None(),
              'hallinto-oikeus-id': Maybe.None(),
              document: true
            }),
            osapuolis
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
            osapuoli => ({
              osapuoli: {
                id: R.prop('id', osapuoli),
                type: Osapuolet.getOsapuoliType(osapuoli)
              },
              'karajaoikeus-id': Maybe.None(),
              'haastemies-email': Maybe.None(),
              document: true
            }),
            osapuolis
          )
        },
        toimenpide
      );

    case R.path(['penalty-decision', 'hearing-letter'], type):
      return R.assoc(
        'type-specific-data',
        {
          fine: Maybe.Some(fine),
          'osapuoli-specific-data': R.map(
            osapuoli => ({
              osapuoli: {
                id: R.prop('id', osapuoli),
                type: Osapuolet.getOsapuoliType(osapuoli)
              },
              document: true
            }),
            osapuolis
          )
        },
        toimenpide
      );

    case R.path(['penalty-decision', 'actual-decision'], type):
      return R.assoc(
        'type-specific-data',
        {
          fine: Maybe.Some(fine),
          'osapuoli-specific-data': R.map(osapuoli => {
            const nameForSwedishStatement =
              R.prop('sukunimi', osapuoli) || R.prop('nimi', osapuoli);

            return {
              osapuoli: {
                id: R.prop('id', osapuoli),
                type: Osapuolet.getOsapuoliType(osapuoli)
              },
              'recipient-answered': false,
              'answer-commentary-fi': Maybe.None(),
              'answer-commentary-sv': Maybe.None(),
              'statement-fi': Maybe.fromNull(defaultStatementFi),
              'statement-sv': R.map(
                R.replace('%s', nameForSwedishStatement),
                Maybe.fromNull(defaultStatementSv)
              ),
              'hallinto-oikeus-id': Maybe.None(),
              document: true
            };
          }, osapuolis),
          'department-head-title-fi': Maybe.fromNull(departmentHeadTitleFi),
          'department-head-title-sv': Maybe.fromNull(departmentHeadTitleSv),
          'department-head-name': Maybe.fromNull(departmentHeadName)
        },
        toimenpide
      );

    case R.path(['penalty-decision', 'notice-bailiff'], type):
      return R.assoc(
        'type-specific-data',
        {
          'osapuoli-specific-data': R.map(
            osapuoli => ({
              osapuoli: {
                id: R.prop('id', osapuoli),
                type: Osapuolet.getOsapuoliType(osapuoli)
              },
              'hallinto-oikeus-id': Maybe.None(),
              'karajaoikeus-id': Maybe.None(),
              'haastemies-email': Maybe.None(),
              document: true
            }),
            osapuolis
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

export const isOrder = isType(R.path(['rfi', 'order'], type));

export const isDecisionOrderActualDecision = isType(
  R.path(['decision-order', 'actual-decision'], type)
);

export const isDecisionOrderNoticeBailiff = isType(
  R.path(['decision-order', 'notice-bailiff'], type)
);
export const isPenaltyDecisionNoticeBailiff = isType(
  R.path(['penalty-decision', 'notice-bailiff'], type)
);

export const isNoticeBailiff = R.anyPass([
  isPenaltyDecisionNoticeBailiff,
  isDecisionOrderNoticeBailiff
]);

export const isDecisionOrderHearingLetter = isType(
  R.path(['decision-order', 'hearing-letter'], type)
);

export const isPenaltyDecisionHearingLetter = isType(
  R.path(['penalty-decision', 'hearing-letter'], type)
);

export const isPenaltyDecisionActualDecision = isType(
  R.path(['penalty-decision', 'actual-decision'], type)
);

export const hasCourtAttachment = R.anyPass([
  isDecisionOrderActualDecision,
  isPenaltyDecisionActualDecision,
  isPenaltyDecisionNoticeBailiff
]);

/**
 * These toimenpide types have a osapuoli specific boolean field document
 */
export const hasOptionalDocument = R.anyPass([
  isDecisionOrderActualDecision,
  isNoticeBailiff,
  isPenaltyDecisionHearingLetter,
  isPenaltyDecisionActualDecision
]);

export const showNormalOsapuoliTable = R.complement(
  R.anyPass([
    isDecisionOrderActualDecision,
    isPenaltyDecisionHearingLetter,
    isPenaltyDecisionActualDecision,
    isNoticeBailiff
  ])
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
 * @param osapuoliId
 * @param {('henkilo'|'yritys')} osapuoliType
 */
export const findOsapuoli = (osapuoliId, osapuoliType) =>
  R.allPass([R.propEq('id', osapuoliId), R.propEq('type', osapuoliType)]);

/**
 * Checks if käskypäätös / varsinainen päätös toimenpide has osapuoli-specific-data field
 * document set to true for the given osapuoliId
 * @param toimenpide
 * @param osapuoliId
 * @param {('henkilo'|'yritys')} osapuoliType
 * @return {boolean}
 */
export const documentExistsForOsapuoli = (
  toimenpide,
  osapuoliId,
  osapuoliType
) => {
  return R.compose(
    R.prop('document'),
    R.find(
      R.compose(findOsapuoli(osapuoliId, osapuoliType), R.prop('osapuoli'))
    ),
    R.path(['type-specific-data', 'osapuoli-specific-data'])
  )(toimenpide);
};

/**
 * Returns the index where the data for the osapuoli with the given osapuoliId
 * and type has their data in the osapuoli-specific-data
 * @param toimenpide
 * @param osapuoliId
 * @param {('henkilo'|'yritys')} osapuoliType
 * @return {number}
 */
export const osapuoliSpecificDataIndexForOsapuoli = (
  toimenpide,
  osapuoliId,
  osapuoliType
) =>
  R.findIndex(
    R.compose(findOsapuoli(osapuoliId, osapuoliType), R.prop('osapuoli')),
    R.path(['type-specific-data', 'osapuoli-specific-data'], toimenpide)
  );

/**
 * Takes a toimenpide object and returns it with only the osapuoli-specific-data for the
 * given osapuoli
 * @param {Object} toimenpide
 * @param {number} osapuoliId
 * @param {('henkilo'|'yritys')} osapuoliType
 * @returns {Object} toimenpide with only the osapuoli-specific-data for the given osapuoli
 */
export const toimenpideForOsapuoli = (toimenpide, osapuoliId, osapuoliType) =>
  R.over(
    R.lensPath(['type-specific-data', 'osapuoli-specific-data']),
    R.filter(
      R.compose(findOsapuoli(osapuoliId, osapuoliType), R.prop('osapuoli'))
    ),
    toimenpide
  );

export const didRecipientAnswer = (toimenpide, osapuoli) =>
  R.path(
    [
      'type-specific-data',
      'osapuoli-specific-data',
      osapuoliSpecificDataIndexForOsapuoli(
        toimenpide,
        osapuoli.id,
        Osapuolet.getOsapuoliType(osapuoli)
      ),
      'recipient-answered'
    ],
    toimenpide
  );

/**
 * To be used with toimenpide objects retrieved from the backend which, at least for the time being,
 * have not been deserialized to contain Maybes in toimenpide-specific-data
 *
 * Check whether given osapuoli has hallinto-oikeus-id in the toimenpide object
 * @param toimenpide
 * @param osapuoli
 * @return {boolean}
 */
export const osapuoliHasHallintoOikeus = (toimenpide, osapuoli) =>
  !R.isNil(
    R.path(
      [
        'type-specific-data',
        'osapuoli-specific-data',
        osapuoliSpecificDataIndexForOsapuoli(
          toimenpide,
          osapuoli.id,
          Osapuolet.getOsapuoliType(osapuoli)
        ),
        'hallinto-oikeus-id'
      ],
      toimenpide
    )
  );

/**
 * Filter toimenpidetypes based on what are allowed transition from
 * the toimenpidetype of the current toimenpide
 * @param currentToimenpide type-id of the current toimenpide
 * @param toimenpidetypes All available toimenpidetypes
 */
export const filterAvailableToimenpidetypes = R.curry(
  (currentToimenpide, toimenpidetypes) => {
    let allowedToimenpidetypes = [];
    switch (currentToimenpide) {
      case type.case:
        allowedToimenpidetypes = [type.rfi.order];
        break;

      case type.rfi.order:
        allowedToimenpidetypes = [type.rfi.order, type.rfi.warning];
        break;

      case type.rfi.warning:
        allowedToimenpidetypes = [
          type.rfi.order,
          type.rfi.warning,
          type['decision-order']['hearing-letter']
        ];
        break;

      case type['decision-order']['hearing-letter']:
        allowedToimenpidetypes = [
          type['decision-order']['hearing-letter'],
          type['decision-order']['actual-decision']
        ];
        break;

      case type['decision-order']['actual-decision']:
        allowedToimenpidetypes = [
          type['decision-order']['actual-decision'],
          type['decision-order']['notice-first-mailing']
        ];
        break;

      case type['decision-order']['notice-first-mailing']:
        allowedToimenpidetypes = [
          type['decision-order']['notice-first-mailing'],
          type['decision-order']['notice-second-mailing']
        ];
        break;

      case type['decision-order']['notice-second-mailing']:
        allowedToimenpidetypes = [
          type['decision-order']['notice-second-mailing'],
          type['decision-order']['notice-bailiff'],
          type['decision-order']['waiting-for-deadline']
        ];
        break;

      case type['decision-order']['notice-bailiff']:
        allowedToimenpidetypes = [
          type['decision-order']['waiting-for-deadline']
        ];
        break;

      case type['decision-order']['waiting-for-deadline']:
        allowedToimenpidetypes = [
          type['court-hearing'],
          type['penalty-decision']['hearing-letter']
        ];
        break;

      case type['court-hearing']:
        allowedToimenpidetypes = [
          type['penalty-decision']['hearing-letter'],
          type['penalty-list-delivery-in-progress']
        ];
        break;

      case type['penalty-decision']['hearing-letter']:
        allowedToimenpidetypes = [
          type['penalty-decision']['hearing-letter'],
          type['penalty-decision']['actual-decision']
        ];
        break;

      case type['penalty-decision']['actual-decision']:
        allowedToimenpidetypes = [
          type['penalty-decision']['actual-decision'],
          type['penalty-decision']['notice-first-mailing']
        ];
        break;

      case type['penalty-decision']['notice-first-mailing']:
        allowedToimenpidetypes = [
          type['penalty-decision']['notice-first-mailing'],
          type['penalty-decision']['notice-second-mailing'],
          type['penalty-decision']['waiting-for-deadline']
        ];
        break;

      case type['penalty-decision']['notice-second-mailing']:
        allowedToimenpidetypes = [
          type['penalty-decision']['notice-second-mailing'],
          type['penalty-decision']['notice-bailiff'],
          type['penalty-decision']['waiting-for-deadline']
        ];
        break;

      case type['penalty-decision']['notice-bailiff']:
        allowedToimenpidetypes = [
          type['penalty-decision']['waiting-for-deadline']
        ];
        break;

      case type['penalty-decision']['waiting-for-deadline']:
        allowedToimenpidetypes = [
          type['court-hearing'],
          type['penalty-list-delivery-in-progress']
        ];
        break;

      case type['penalty-list-delivery-in-progress']:
        allowedToimenpidetypes = [type.closed];
        break;

      case type.decision.order:
        allowedToimenpidetypes = [type['court-hearing']];
        break;
    }

    // Valvonnan lopetus is always allowed
    allowedToimenpidetypes.push(type.closed);

    return R.filter(
      R.compose(R.includes(R.__, allowedToimenpidetypes), R.prop('id')),
      toimenpidetypes
    );
  }
);
