import subDays from 'date-fns/subDays';
import formatISO from 'date-fns/formatISO';

export const formatApiDate = date => date.toJSON();

export const formatDate = date =>
  Intl.DateTimeFormat('fi-FI', {
    timeZone: 'Europe/Helsinki',
    dateStyle: 'medium'
  }).format(date);

export const formatExclusiveEndDate = date =>
  Intl.DateTimeFormat('fi-FI', {
    timeZone: 'Europe/Helsinki',
    dateStyle: 'medium'
  }).format(subDays(date, 1));

const numberFormatter0 = Intl.NumberFormat('fi', {
  maximumFractionDigits: 0,
  minimumFractionDigits: 0
});
const numberFormatter1 = Intl.NumberFormat('fi', {
  maximumFractionDigits: 1,
  minimumFractionDigits: 1
});
const numberFormatterMax2 = Intl.NumberFormat('fi', {
  maximumFractionDigits: 2
});

export const formatNumber0 = n => numberFormatter0.format(n);

export const formatNumber1 = n => numberFormatter1.format(n);

export const formatNumberMax2 = n => numberFormatterMax2.format(n);

export const formatDateISO = date =>
  !isNaN(date.getTime()) ? formatISO(date, { representation: 'date' }) : '';

export const formatPercent = number => `${number} %`;
