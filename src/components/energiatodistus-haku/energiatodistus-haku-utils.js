import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as parsers from '@Utility/parsers';
import * as Either from '@Utility/either-utils';

import { OPERATOR_TYPES } from './schema';

export const defaultQueryItem = R.always({
  conjunction: 'and',
  block: ['', '', '']
});

export const blockToQueryParameter = R.curry(
  (schema, [operation, key, ...values]) => {
    if (R.filter(value => value !== '', values).length === 0)
      return Maybe.None();

    return R.compose(
      R.map(s =>
        R.compose(
          R.map(R.over(R.lensIndex(1), R.concat('energiatodistus.'))),
          R.apply(s.operation.format(s.operation.serverCommand, key))
        )(values)
      ),
      R.chain(
        Maybe.nullReturning(
          R.find(R.pathEq(['operation', 'browserCommand'], operation))
        )
      ),
      Maybe.fromNull,
      R.prop(key)
    )(schema);
  }
);

export const convertWhereToQuery = R.curry((schema, where) =>
  R.compose(
    R.filter(R.length),
    R.map(
      R.compose(
        R.reduce(R.concat, []),
        R.map(Maybe.get),
        R.filter(Maybe.isSome),
        R.map(blockToQueryParameter(schema))
      )
    )
  )(where)
);

export const deserializeConjuntionBlockPair = ([conjunction, block]) => ({
  conjunction,
  block
});

export const deserializeAndBlocks = R.converge(R.concat, [
  R.compose(Array.of, R.pair('or'), R.head),
  R.compose(R.map(R.pair('and')), R.tail)
]);

export const deserializeWhere = R.curry((schema, where) =>
  R.compose(
    Either.orSome(''),
    R.map(R.set(R.compose(R.lensIndex(0), R.lensProp('conjunction')), 'and')),
    where =>
      Either.fromTry(() =>
        R.filter(
          R.compose(R.has(R.__, schema), R.nth(1), R.prop('block')),
          where
        )
      ),
    R.map(deserializeConjuntionBlockPair),
    R.unnest,
    R.map(deserializeAndBlocks)
  )(where)
);

export const removeQueryItem = R.curry((index, queryItems) =>
  R.compose(
    R.set(R.compose(R.lensIndex(0), R.lensProp('conjunction')), 'and'),
    R.remove(index, 1)
  )(queryItems)
);

export const parseValueByType = R.curry((type, value) => {
  switch (type) {
    case OPERATOR_TYPES.NUMBER:
    case OPERATOR_TYPES.VERSIO:
    case OPERATOR_TYPES.TILA:
      return parsers.parseNumber(value);
    case OPERATOR_TYPES.BOOLEAN:
      return Either.Right(value === 'true');
    default:
      return Either.Right(value);
  }
});

export const formObject = form => {
  const fd = new FormData(form);
  const ds = {};
  fd.forEach((value, key) => (ds[key] = value));
  return ds;
};

export const groupHakuForm = R.compose(
  R.map(R.map(R.tail)),
  R.values,
  R.groupBy(R.head),
  R.map(R.compose(R.flatten, R.over(R.lensIndex(0), R.split('_')))),
  R.toPairs
);

export const findValueByMatchingHead = R.curry((value, arr) =>
  R.compose(
    R.map(R.last),
    Maybe.fromNull,
    R.find(R.compose(R.equals(value), R.head))
  )(arr)
);

export const hakuCriteriaFromGroupedInput = inputs => {
  const type = Maybe.orSome('', findValueByMatchingHead('type', inputs));
  const conjunction = findValueByMatchingHead('conjunction', inputs);
  const key = findValueByMatchingHead('key', inputs);
  const operation = findValueByMatchingHead('operation', inputs);
  const values = R.compose(
    R.map(R.compose(parseValueByType(type), R.last)),
    R.sortBy(R.nth(1)),
    R.filter(R.compose(R.equals('value'), R.head))
  )(inputs);

  return {
    conjunction: Maybe.orSome('and', conjunction),
    block: R.all(Either.isRight)
      ? [
          Maybe.orSome('', operation),
          Maybe.orSome('', key),
          ...R.map(Either.orSome(''), values)
        ]
      : []
  };
};

export const parseHakuForm = form => {
  const formdata = formObject(form);
  const groupedInputs = groupHakuForm(formdata);

  return groupedInputs;
};

export const searchItems = R.map(hakuCriteriaFromGroupedInput);

export const searchString = searchItems =>
  JSON.stringify(
    R.reduce(
      (acc, item) => {
        if (item.length === 0) return acc;
        if (item.conjunction === 'and') {
          return R.over(R.lensIndex(acc.length - 1), R.append(item.block), acc);
        }

        return R.append([item.block], acc);
      },
      [[]],
      searchItems
    )
  );

export const isValidBlock = R.curry((schema, [operator, key, ...values]) => {
  if (!R.all(R.length, [operator, key])) {
    return false;
  }

  // Implement rest soon.
  return true;
});
