import * as R from 'ramda';

const isInState = R.propEq(R.__, 'tila-id');

export const isProposal = isInState(0);
export const isAccepted = isInState(1);
export const isDeleted = isInState(2);
