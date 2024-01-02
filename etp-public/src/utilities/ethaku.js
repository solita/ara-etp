import * as parsers from '@/utilities/parsers';
import * as formats from '@/utilities/formats';

import isValid from 'date-fns/isValid';
import isDate from 'date-fns/isDate';
import isAfter from 'date-fns/isAfter';
import isBefore from 'date-fns/isBefore';
import isEqual from 'date-fns/isEqual';

const optionalNumber = value =>
  value === '' || !isNaN(parsers.parseInteger(value));

const optionalRange = (min, max) => value => {
  if (value === '') return true;
  if (min > max) return true;
  const v = parsers.parseInteger(value);

  return v >= min && v <= max;
};

const optionalString = max => value => value.length <= max;
const optionalDateBetween = (min, max) => value => {
  const minP = parsers.parseDate(min);
  const maxP = parsers.parseDate(max);
  const valueP = parsers.parseDate(value);

  if (!isValid(valueP)) return true;

  if (isValid(minP) && !(isAfter(valueP, minP) || isEqual(valueP, minP))) {
    return false;
  }

  if (isValid(maxP) && !(isBefore(valueP, maxP) || isEqual(valueP, maxP))) {
    return false;
  }

  return true;
};

const optionalRakennustunnus = value =>
  value.length === 0 || value.toUpperCase().match(/^1\d{8}[A-Z0-9]{1}$/);

export const defaultSearchModel = () => ({
  id: '',
  versio: '0',
  'perustiedot.kayttotarkoitus_in': [],
  'perustiedot.kayttotarkoitus': '',
  'perustiedot.nimi': '',
  'perustiedot.rakennustunnus': '',
  'perustiedot.valmistumisvuosi_min': '',
  'perustiedot.valmistumisvuosi_max': '',
  allekirjoitusaika_min: '',
  allekirjoitusaika_max: '',
  'voimassaolo-paattymisaika_min': '',
  'voimassaolo-paattymisaika_max': '',
  kayttotarkoitusluokka: '',
  alakayttotarkoitusluokka: '',
  'tulokset.e-luku_min': '',
  'tulokset.e-luku_max': '',
  'tulokset.e-luokka_in': [],
  'lahtotiedot.lammitetty-nettoala_min': '',
  'lahtotiedot.lammitetty-nettoala_max': ''
});

export const parseModel = () => ({
  id: parsers.parseInteger,
  versio: parsers.parseInteger,
  'perustiedot.valmistumisvuosi_min': parsers.parseInteger,
  'perustiedot.valmistumisvuosi_max': parsers.parseInteger,
  allekirjoitusaika_min: parsers.parseDate,
  allekirjoitusaika_max: parsers.parseDate,
  'voimassaolo-paattymisaika_min': parsers.parseDate,
  'voimassaolo-paattymisaika_max': parsers.parseDate,
  'tulokset.e-luku_min': parsers.parseInteger,
  'tulokset.e-luku_max': parsers.parseInteger,
  'lahtotiedot.lammitetty-nettoala_min': parsers.parseInteger,
  'lahtotiedot.lammitetty-nettoala_max': parsers.parseInteger
});

export const validationModel = () => ({
  id: optionalNumber,
  versio: () => true,
  'perustiedot.kayttotarkoitus_in': () => true,
  'perustiedot.nimi': optionalString(100),
  'perustiedot.rakennustunnus': optionalRakennustunnus,
  'perustiedot.valmistumisvuosi_min': optionalRange,
  'perustiedot.valmistumisvuosi_max': optionalRange,
  allekirjoitusaika_min: optionalDateBetween,
  allekirjoitusaika_max: optionalDateBetween,
  'voimassaolo-paattymisaika_min': optionalDateBetween,
  'voimassaolo-paattymisaika_max': optionalDateBetween,
  kayttotarkoitusluokka: () => true,
  alakayttotarkoitusluokka: () => true,
  'tulokset.e-luku_min': optionalRange,
  'tulokset.e-luku_max': optionalRange,
  'tulokset.e-luokka_in': () => true,
  'lahtotiedot.lammitetty-nettoala_min': optionalRange,
  'lahtotiedot.lammitetty-nettoala_max': optionalRange
});

export const includeInSearch = (key, value) => {
  const numberValue = parseInt(value);
  switch (key) {
    case 'versio':
      return !isNaN(numberValue) && numberValue !== 0;
    case 'id':
    case 'perustiedot.valmistumisvuosi':
    case 'tulokset.e-luku':
    case 'lahtotiedot.lammitetty-nettoala':
      return !isNaN(numberValue) && numberValue > 0;
    default:
      return value.length > 0;
  }
};

export const deserializeWhere = (model, where) => {
  let res;
  try {
    res = JSON.parse(where);
  } catch (e) {
    res = [[]];
  }

  const [and] = res;

  return and
    .map(([op, key, value]) => {
      let postfix = '';
      switch (op) {
        case 'in':
          postfix = '_in';
          break;
        case '>=':
          postfix = '_min';
          break;
        case '<=':
          postfix = '_max';
          break;
      }

      return { [`${key}${postfix}`]: op !== 'in' ? value + '' : value };
    })
    .reduce((acc, item) => ({ ...acc, ...item }), {});
};

const isValidDate = date => !isNaN(date.getTime());

const eq = (key, model) => ['=', key, model[key]];
const icontains = (key, model) => ['icontains', key, model[key]];
const lte = (key, model) => ['<=', key, model[`${key}_max`]];
const gte = (key, model) => ['>=', key, model[`${key}_min`]];
const lteDate = (key, model) => [
  '<=',
  key,
  isValidDate(model[`${key}_max`])
    ? formats.formatApiDate(model[`${key}_max`])
    : ''
];
const gteDate = (key, model) => [
  '>=',
  key,
  isValidDate(model[`${key}_min`])
    ? formats.formatApiDate(model[`${key}_min`])
    : ''
];

const valueIn = (key, model) =>
  model[`${key}_in`].length ? [['in', key, model[`${key}_in`]]] : [];

export const where = (tarkennettu, model) => [
  eq('id', model),
  ...(tarkennettu
    ? [
        eq('versio', model),
        ...valueIn('perustiedot.kayttotarkoitus', model),
        eq('perustiedot.kayttotarkoitus', model),
        icontains('perustiedot.nimi', model),
        eq('perustiedot.rakennustunnus', model),
        gte('perustiedot.valmistumisvuosi', model),
        lte('perustiedot.valmistumisvuosi', model),
        gteDate('allekirjoitusaika', model),
        lteDate('allekirjoitusaika', model),
        gteDate('voimassaolo-paattymisaika', model),
        lteDate('voimassaolo-paattymisaika', model),
        gte('tulokset.e-luku', model),
        lte('tulokset.e-luku', model),
        ...valueIn('tulokset.e-luokka', model),
        gte('lahtotiedot.lammitetty-nettoala', model),
        lte('lahtotiedot.lammitetty-nettoala', model)
      ]
    : [])
];

export const whereQuery = where => [
  where.filter(item => {
    const [value, key] = [...item].reverse();
    return includeInSearch(key, value);
  })
];
