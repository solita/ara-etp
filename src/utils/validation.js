import * as R from 'ramda';

export function ytunnusChecksum(ytunnus) {
  const digits = R.map(parseInt, R.slice(0, 7, ytunnus));
  const sum = R.reduce(R.add, 0, R.zipWith(R.multiply, digits, [7, 9, 10, 5, 8, 4, 2]));
  const remainder = sum % 11;
  return remainder == 0 ? 0 : 11 - remainder;
}

export function validYtunnus(ytunnus) {
  const checksum = ytunnusChecksum(ytunnus);
  return R.length(ytunnus) === 9 &&
      checksum !== 10 &&
      ytunnus[7] === '-' &&
      parseInt(ytunnus[8]) === checksum;
}