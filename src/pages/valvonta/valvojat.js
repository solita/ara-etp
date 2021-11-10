import * as Maybe from '@Utility/maybe-utils';
import * as Kayttajat from '@Utility/kayttajat';

export const isSelfInValvonta = (whoami, valvonta) =>
  Maybe.fold(false, Kayttajat.isSelf(whoami), valvonta['valvoja-id']);
