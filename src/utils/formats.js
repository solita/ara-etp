import * as dfns from 'date-fns';

export const formatTimeInstant = time => dfns.format(time, 'd.M.yyyy HH:mm:ss')