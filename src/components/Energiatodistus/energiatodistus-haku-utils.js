import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

import NumberOperatorInput from '@Component/Energiatodistus/querybuilder/query-inputs/number-operator-input';
import OperatorInput from '@Component/Energiatodistus/querybuilder/query-inputs/operator-input';
import BooleanInput from '@Component/Energiatodistus/querybuilder/query-inputs/boolean-input';
import DateInput from '@Component/Energiatodistus/querybuilder/query-inputs/date-input';

const gt = {
  operation: '>',
  command: R.curry((first, second) => ['>', first, second])
};
const gte = {
  operation: '>=',
  command: R.curry((first, second) => ['>=', first, second])
};
const lt = {
  operation: '<',
  command: R.curry((first, second) => ['<', first, second])
};
const lte = {
  operation: '<=',
  command: R.curry((first, second) => ['<=', first, second])
};
const eq = {
  operation: '=',
  command: R.curry((first, second) => ['=', first, second])
};

const contains = {
  operation: 'sisaltaa',
  command: R.curry((first, second) => ['like', first, `%${second}%`])
};

const allComparisons = [eq, gt, gte, lt, lte];

const operations = [...allComparisons, contains];

const kriteeri = (
  key,
  operators,
  defaultOperator,
  defaultValue,
  component
) => ({
  key,
  operators: R.map(R.over(R.lensProp('command'), R.applyTo(key)), operators),
  defaultOperator: R.over(
    R.lensProp('command'),
    R.applyTo(key),
    defaultOperator
  ),
  defaultValue,
  component
});

const korvattuEnergiatodistusIdKriteeri = kriteeri(
  'korvattu-energiatodistus-id',
  allComparisons,
  eq,
  '',
  OperatorInput
);

export const idKriteeri = kriteeri(
  'id',
  allComparisons,
  eq,
  '',
  NumberOperatorInput
);

const allekirjoitusaikaKriteeri = kriteeri(
  'allekirjoitusaika',
  allComparisons,
  eq,
  '',
  DateInput
);

// const viimeinenvoimassaoloKriteeri = kriteeri('viimeinenvoimassaolo', []);

export const perustiedot = () => ({
  nimi: kriteeri('perustiedot.nimi', [eq, contains], eq, '', OperatorInput),
  rakennustunnus: kriteeri(
    'perustiedot.rakennustunnus',
    allComparisons,
    eq,
    '',
    OperatorInput
  ),
  kiinteistotunnus: kriteeri(
    'perustiedot.kiinteistotunnus',
    allComparisons,
    eq,
    '',
    OperatorInput
  ),
  // 'katuosoite-fi': kriteeri('perustiedot.katuosoite-fi', []),
  // 'katuosoite-sv': kriteeri('perustiedot.katuosoite-sv', []),
  postinumero: kriteeri(
    'perustiedot.postinumero',
    allComparisons,
    eq,
    '',
    OperatorInput
  ),
  'onko-julkinen-rakennus': kriteeri(
    'perustiedot.onko-julkinen-rakennus',
    [eq],
    eq,
    'true',
    BooleanInput
  ),
  uudisrakennus: kriteeri(
    'perustiedot.uudisrakennus',
    [eq],
    eq,
    '',
    OperatorInput
  )
  // laatimisvaihe: kriteeri('perustiedot.laatimisvaihe', []),
  // havainnointikaynti: kriteeri('perustiedot.havainnointikaynti', []),
  // valmistumisvuosi: kriteeri('perustiedot.valmistumisvuosi', []),
  // rakennusluokka: kriteeri('rakennusluokka', []),
  // 'rakennuksen-kayttotarkoitusluokka': kriteeri(
  //   'rakennuksen-kayttotarkoitusluokka',
  //   []
  // )
});

export const defaultKriteeriQueryItem = () => ({
  conjunction: Maybe.None(),
  block: ['=', 'id', 0]
});

export const defaultKriteeri = () => [[['=', 'id', 0]]];

export const laatijaKriteerit = () => [
  idKriteeri,
  allekirjoitusaikaKriteeri,
  ...R.values(perustiedot())
];

export const findOperation = operation =>
  R.compose(
    Maybe.fromNull,
    R.find(R.compose(R.equals(operation), R.prop('operation')))
  )(operations);

export const blockToQueryParameter = ([operation, key, ...values]) =>
  R.compose(
    R.map(op => R.apply(op.command, [key, ...values])),
    findOperation
  )(operation);

export const convertWhereToQuery = R.compose(
  R.filter(R.length),
  R.map(
    R.compose(
      R.map(Maybe.get),
      R.filter(Maybe.isSome),
      R.map(blockToQueryParameter)
    )
  )
);

export const deserializeBlock = ([conjunction, block]) => ({
  conjunction,
  block
});

export const deserializeAndBlocks = R.converge(R.concat, [
  R.take(1),
  R.compose(R.map(R.pair(Maybe.Some('and'))), R.tail)
]);

export const deserializeOrBlocks = R.converge(R.concat, [
  R.take(1),
  R.compose(R.map(R.over(R.lensIndex(0), R.pair(Maybe.Some('or')))), R.tail)
]);

export const deserializeWhere = R.compose(
  R.map(deserializeBlock),
  R.over(R.lensIndex(0), R.pair(Maybe.None())),
  R.unnest,
  deserializeOrBlocks,
  R.map(deserializeAndBlocks)
);

export const serializeWhere = R.reduce(
  (acc, { conjunction, block }) => {
    if (Maybe.exists(R.equals('or'), conjunction)) {
      return R.append([block], acc);
    }

    const lastLens = R.lensIndex(R.length(acc) - 1);

    return R.over(lastLens, R.append(block), acc);
  },
  [[]]
);

export const removeQueryItem = R.curry((index, queryItems) =>
  R.compose(
    R.set(R.compose(R.lensIndex(0), R.lensProp('conjunction')), Maybe.None()),
    R.remove(index, 1)
  )(queryItems)
);

export const appendDefaultQueryItem = R.append(
  R.assoc('conjunction', Maybe.Some('and'), defaultKriteeriQueryItem())
);
