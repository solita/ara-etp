import * as dfns from 'date-fns';

export const formatTimeInstant = time => dfns.format(time, 'yyyy.MM.dd HH:mm:ss')