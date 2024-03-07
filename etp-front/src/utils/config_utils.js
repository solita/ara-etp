import * as R from 'ramda';

const PRODUCTION_ENV_NAME = 'prod';

export const isProduction = envName => R.equals(envName, PRODUCTION_ENV_NAME);
