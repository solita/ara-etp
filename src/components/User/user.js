import { writable } from 'svelte/store';
import * as R from 'ramda';

import * as Either from '../../utils/either-utils';
import * as Future from '../../utils/future-utils';
import * as UserUtils from './user-utils';

export const currentUser = writable();

export const fetchUser = R.compose(
  Future.promise,
  Future.coalesce(Either.Left, Either.Right),
  UserUtils.userFuture(fetch)
);
