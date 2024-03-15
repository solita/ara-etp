import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

export const other = R.propEq(Maybe.Some(2), 'ilmoituspaikka-id');
