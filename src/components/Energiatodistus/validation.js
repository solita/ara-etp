import * as laatimisvaiheet from '@Component/Energiatodistus/laatimisvaiheet';
import * as kielisyys from '@Component/Energiatodistus/kielisyys';
import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';


const requiredCondition = {
  "perustiedot.havainnointikaynti": laatimisvaiheet.isOlemassaOlevaRakennus,
  "perustiedot.rakennustunnus": R.complement(laatimisvaiheet.isRakennuslupa)
};

const predicate = R.compose(
  R.find(R.complement(R.isNil())),
  R.juxt([
    R.prop(R.__, requiredCondition),
    R.ifElse(R.endsWith("-fi"), R.always(kielisyys.fi), R.always(null)),
    R.ifElse(R.endsWith("-sv"), R.always(kielisyys.sv), R.always(null)),
    R.always(R.T)
]));

const requiredConstraints = R.map(R.juxt([predicate, R.identity]));

const assertValue = R.curry((property, value) => {
  if (R.isNil(value) || !Maybe.isMaybe(value)) {
    throw "Required property: " + property +
      " value: " + value +
      " must be in monad Maybe[A] or Either[Maybe[A]].";
  }
});

const isValueMissing = (property, energiatodistus) => R.compose(
  Maybe.isNone,
  R.tap(assertValue(property)),
  R.when(Either.isEither, Either.orSome(Maybe.None())),
  R.path(R.__, energiatodistus),
  R.split('.')
)(property);

export const missingProperties = (requiredProperties, energiatodistus) =>
  R.compose(
    R.map(R.nth(1)),
    R.filter(([predicate, property]) =>
      predicate(energiatodistus) &&
      isValueMissing(property, energiatodistus))
  )(requiredConstraints(requiredProperties))