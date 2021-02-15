import * as Fetch from '@Utility/fetch-utils';

export const countries = Fetch.cached(fetch, '/countries/');
export const toimintaalueet = Fetch.cached(fetch, '/toimintaalueet/');
