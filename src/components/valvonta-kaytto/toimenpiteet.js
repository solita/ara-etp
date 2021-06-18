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
  closed: 5
};

const types = R.invertObj(Deep.treeFlat('-', R.F, type));

export const typeKey = id => types[id];

export const isType = R.propEq('type-id');

const isDeadlineType = R.includes(R.__, [1,2,3]);
export const hasDeadline = R.propSatisfies(isDeadlineType, 'type-id');

export const isAuditCase = R.complement(isType(type.closed));
export const isAuditCaseToimenpideType = R.propSatisfies(
  R.includes(R.__, [1, 2, 3, 4, 5]),
  'id'
);

const defaultDeadline = typeId =>
  isDeadlineType(typeId)
    ? Maybe.Some(dfns.addMonths(new Date(), 1))
    : Maybe.None();

export const i18nKey = (toimenpide, key) =>
  R.join('.', [
    'valvonta.kaytto.toimenpide',
    typeKey(toimenpide['type-id']),
    key
  ]);

export const emptyToimenpide = typeId => ({
  'type-id': typeId,
  'publish-time': Maybe.None(),
  'deadline-date': Either.Right(defaultDeadline(typeId)),
  'template-id': Maybe.None(),
  description: Maybe.None()
});

export const isDraft = R.compose(Maybe.isNone, R.prop('publish-time'));

export const hasTemplate = R.compose(Maybe.isSome, R.prop('template-id'));

export const templates = templatesByType =>
  R.compose(R.defaultTo([]), R.prop(R.__, templatesByType), R.prop('type-id'));

export const time = R.converge(Maybe.orSome, [R.prop('create-time'), R.prop('publish-time')])