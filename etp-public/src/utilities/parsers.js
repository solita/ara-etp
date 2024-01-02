import parseISO from 'date-fns/parseISO';
import parse from 'date-fns/parse';

export const match = (exp, value) => {
  const m = value.match(exp);
  if (m === null) return '';

  return m[0];
};

export const parseInteger = value =>
  parseInt(match(/^\d*$/, (value + '').trim()));

export const parseDate = value => {
  try {
    let date = parseISO(value.trim());
    if (isNaN(date)) date = parse(value, 'dd.MM.yyyy', new Date());
    return date;
  } catch (e) {
    return null;
  }
};

export const parsePercent = (total, str) =>
  ((parseInt(str) / total) * 100).toFixed(0);
