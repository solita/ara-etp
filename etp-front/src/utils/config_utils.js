import * as R from 'ramda';

const PRODUCTION_ENV_NAME = 'prod';

export const isProduction = envName => R.equals(envName, PRODUCTION_ENV_NAME);

export const isEtp2026Enabled = config => R.propEq(true, 'isEtp2026', config);
