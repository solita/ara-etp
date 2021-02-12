import * as Future from '@Utility/future-utils';
import * as Fetch from '@Utility/fetch-utils';

export const getVersion = Future.cache(
  Fetch.getJson(fetch, `version.json?${Date.now()}`)
);
