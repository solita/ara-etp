import * as R from 'ramda';
import NumberOperatorInput from '@Component/Energiatodistus/querybuilder/query-inputs/number-operator-input';
import OperatorInput from '@Component/Energiatodistus/querybuilder/query-inputs/operator-input';
import BooleanInput from '@Component/Energiatodistus/querybuilder/query-inputs/boolean-input';
import DateInput from '@Component/Energiatodistus/querybuilder/query-inputs/date-input';

const gt = {
  operation: '>',
  command: R.curry((first, second) => `> ${first} ${second}`)
};
const gte = {
  operation: '>=',
  command: R.curry((first, second) => `>= ${first} ${second}`)
};
const lt = {
  operation: '<',
  command: R.curry((first, second) => `< ${first} ${second}`)
};
const lte = {
  operation: '<=',
  command: R.curry((first, second) => `<= ${first} ${second}`)
};
const eq = {
  operation: '=',
  command: R.curry((first, second) => `= ${first} ${second}`)
};

const contains = {
  operation: 'sisaltaa',
  command: R.curry((first, second) => `like ${first} %${second}%`)
};

const allComparisons = [eq, gt, gte, lt, lte];

export const operations = () => [...allComparisons, contains];

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

export let defaultKriteeriBlock = () => ['=', 'id', ''];

export let defaultKriteeri = () => [[['=', 'id', '']]];

export const laatijaKriteerit = () => [
  idKriteeri,
  allekirjoitusaikaKriteeri,
  ...R.values(perustiedot())
];
