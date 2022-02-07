import * as R from 'ramda';
import * as Either from '@Utility/either-utils';

export const fromLaatijaForm = R.evolve({ toteamispaivamaara: Either.right });

export const toLaatijaForm = R.evolve({ toteamispaivamaara: Either.Right });
