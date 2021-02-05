import * as dfns from 'date-fns';
import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as Validation from '@Utility/validation';

export const formatTimeInstant = time => dfns.format(time, 'd.M.yyyy H:mm:ss');
export const formatDateInstant = date => dfns.format(date, 'd.M.yyyy');
export const formatHoursMinutes = time => dfns.format(time, 'H:mm');

export const numberFormat = Intl.NumberFormat('fi-FI').format;
export const percentFormat = Intl.NumberFormat('fi-FI', { style: 'percent' })
  .format;

export const optionalString = Maybe.orSome('');
export const optionalNumber = R.compose(Maybe.orSome(''), R.map(numberFormat));
export const optionalYear = R.compose(Maybe.orSome(''), R.map(R.identity));

/**
 * Format start time instant (UTC) as a start date.
 * Start date is a date in Europe/Helsinki timezone when
 * the particular period is started.
 */
export const inclusiveStartDate = Intl.DateTimeFormat('fi-FI', {
  timeZone: 'Europe/Helsinki',
  dateStyle: 'medium'
}).format;

/**
 * Format end time instant as a inclusive end date (the last valid date).
 * Here we assume that Europe/Helsinki timezone is always
 * somewhere between -9 to +13
 */
export const inclusiveEndDate = endTimeInstant =>
  Intl.DateTimeFormat('fi-FI', { timeZone: 'UTC', dateStyle: 'medium' }).format(
    dfns.subHours(endTimeInstant, 10)
  );

export const iban = R.compose(R.join(' '), R.splitEvery(4));

export const verkkolaskuosoite = R.when(Validation.isIBAN, iban);
