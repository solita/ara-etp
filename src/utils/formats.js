import * as dfns from 'date-fns';
import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

export const formatTimeInstant = time => dfns.format(time, 'd.M.yyyy H:mm:ss');
export const formatDateInstant = date => dfns.format(date, 'd.M.yyyy');

export const numberFormat = Intl.NumberFormat('fi-FI').format;
export const percentFormat = Intl.NumberFormat('fi-FI', { style: 'percent' });

export const optionalString = Maybe.orSome('');
export const optionalNumber = R.compose(Maybe.orSome(''), R.map(numberFormat));
