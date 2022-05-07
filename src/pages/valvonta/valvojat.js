import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as Kayttajat from '@Utility/kayttajat';

export const isSelfInValvonta = (whoami, valvonta) =>
  Maybe.fold(false, Kayttajat.isSelf(whoami), valvonta['valvoja-id']);

export const filterActive =
  R.filter(R.allPass([
    R.complement(R.prop('passivoitu')),
    R.prop('valvoja'),
    R.propSatisfies(Kayttajat.isPaakayttajaRole, 'rooli-id')]));
