import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

export const addNoSelection = items => R.prepend(Maybe.None(), R.map(Maybe.Some, items))