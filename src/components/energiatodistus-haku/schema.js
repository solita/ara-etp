import * as R from 'ramda';

export const OPERATOR_TYPES = Object.freeze({
  STRING: 'STRING',
  NUMBER: 'NUMBER',
  DATESINGLE: 'DATESINGLE',
  BOOLEAN: 'BOOLEAN'
});

const eq = {
  browserCommand: '=',
  serverCommand: '=',
  format: R.identity
};
const gt = {
  browserCommand: '>',
  serverCommand: '>',
  format: R.identity
};
const gte = {
  browserCommand: '>=',
  serverCommand: '>=',
  format: R.identity
};
const lt = {
  browserCommand: '<',
  serverCommand: '<',
  format: R.identity
};
const lte = {
  browserCommand: '<=',
  serverCommand: '<=',
  format: R.identity
};

const contains = {
  browserCommand: 'sisaltaa',
  serverCommand: 'like',
  format: arg => `%${arg}%`
};

const singleNumberOperation = R.curry((operation, key) => ({
  operation,
  key,
  argumentNumber: 1,
  defaultValues: [0],
  type: OPERATOR_TYPES.NUMBER
}));

const numberEquals = singleNumberOperation(eq);

const numberGreaterThan = singleNumberOperation(gt);

const numberGreaterThanOrEqual = singleNumberOperation(gte);

const numberLessThan = singleNumberOperation(lt);

const numberLessThanOrEqual = singleNumberOperation(lte);

const containsString = key => ({
  operation: contains,
  key,
  argumentNumber: 1,
  defaultValues: [''],
  type: OPERATOR_TYPES.STRING
});

const numberComparisons = [
  numberEquals,
  numberGreaterThan,
  numberGreaterThanOrEqual,
  numberLessThan,
  numberLessThanOrEqual
];

const schema = {
  id: R.map(R.applyTo('id'), numberComparisons),
  'korvattu-energiatodistus-id': [containsString('korvattu-energiatodistus-id')]
  // R.map(
  //   R.applyTo('korvattu-energiatodistus-id'),
  //   numberComparisons
  // )
};

export const laatijaSchema = R.pick(
  ['id', 'korvattu-energiatodistus-id'],
  schema
);
