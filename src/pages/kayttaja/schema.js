import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as Validation from '@Utility/validation';

const VirtuID = {
  localid: Validation.RequiredString(2, 200),
  organisaatio: Validation.RequiredString(2, 200)
};

const VirtuIDValidator = {
  predicate: Validation.isValidForm(VirtuID),
  label: R.applyTo('kayttaja.messages.invalid-virtu-id')
};

export const Kayttaja = {
  rooli: [Validation.isSome],
  henkilotunnus: [Validation.liftValidator(Validation.henkilotunnusValidator)],
  etunimi: Validation.RequiredString(2, 200),
  sukunimi: Validation.RequiredString(2, 200),
  email: [...Validation.RequiredString(2, 200), Validation.emailValidator],
  puhelin: Validation.RequiredString(2, 200),
  virtu: [Validation.liftValidator(VirtuIDValidator)],
  organisaatio: Validation.LimitedString(0, 200),
  'api-key': [Validation.liftValidator(Validation.apiPasswordValidator)]
};

export const virtuSchema = kayttaja =>
  Maybe.isSome(kayttaja.virtu) ? VirtuID : R.map(R.always([]), VirtuID);

export const aineistolupa = {
  'voimassaolo-paattymisaika': Validation.isPaivamaara,
  'ip-address': Validation.LimitedString(15)
};
