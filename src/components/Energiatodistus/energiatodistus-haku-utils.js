import * as R from 'ramda';
import OperatorInput from '@Component/Energiatodistus/querybuilder/query-inputs/operator-input';

const gt = {
  label: '>',
  command: R.curry((first, second) => `> ${first} ${second}`)
};
const gte = {
  label: '>=',
  command: R.curry((first, second) => `>= ${first} ${second}`)
};
const lt = {
  label: '<',
  command: R.curry((first, second) => `< ${first} ${second}`)
};
const lte = {
  label: '<=',
  command: R.curry((first, second) => `<= ${first} ${second}`)
};
const eq = {
  label: '=',
  command: R.curry((first, second) => `= ${first} ${second}`)
};

const contains = {
  label: 'sisaltaa',
  command: R.curry((first, second) => `like ${first} %${second}%`)
};

const allComparisons = [eq, gt, gte, lt, lte];

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
  'perustiedot.id',
  allComparisons,
  eq,
  '',
  OperatorInput
);

// const allekirjoitusaikaKriteeri = kriteeri('allekirjoitusaika', []);

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
    '',
    OperatorInput
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

export let defaultKriteeri = () =>
  JSON.stringify([[['=', 'perustiedot.id', '']]]);

export const laatijaKriteerit = () => [idKriteeri, ...R.values(perustiedot())];
