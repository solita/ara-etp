import * as Fetch from '@Utility/fetch-utils';

export const laskutuskielet = Fetch.cached(fetch, '/laskutuskielet/');
export const verkkolaskuoperaattorit = Fetch.cached(
  fetch,
  '/verkkolaskuoperaattorit/'
);
