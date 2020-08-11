import * as R from 'ramda';

export const OPERATOR_TYPES = Object.freeze({
  STRING: 'STRING',
  NUMBER: 'NUMBER',
  DATESINGLE: 'DATESINGLE',
  BOOLEAN: 'BOOLEAN'
});

const eq = {
  operation: '=',
  command: '=',
  format: R.identity,
  parse: parseFloat
};
const gt = {
  operation: '>',
  command: '>',
  format: R.identity,
  parse: parseFloat
};
const gte = {
  operation: '>=',
  command: '>=',
  format: R.identity,
  parse: parseFloat
};
const lt = {
  operation: '<',
  command: '<',
  format: R.identity,
  parse: parseFloat
};
const lte = {
  operation: '<=',
  command: '<=',
  format: R.identity,
  parse: parseFloat
};

const contains = {
  operation: 'sisaltaa',
  command: 'like',
  format: arg => `%${arg}%`
};

const singleNumberOperation = R.curry((operation, key) => ({
  operation,
  key,
  argumentNumber: 1,
  defaultValue: 0,
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
  defaultValue: '',
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
  id: R.map(R.applyTo('id'), numberComparisons)
};

const laatijaSchema = R.pick(['id'], schema);
