import * as R from 'ramda';
import * as ET from './energiatodistus-utils';
import * as Maybe from '@Utility/maybe-utils';

const equals = R.curry((getter, a, b) => R.equals(getter(a), getter(b)));

export const isSame = equals(R.prop('id'))

const isKorvattavissa = ET.isTilaInTilat([ET.tila.signed, ET.tila.discarded]);
const isKorvaaja = ET.isTilaInTilat([ET.tila.signed, ET.tila.replaced]);

const isKorvattuInET = (korvattava, energiatodistus) =>
  R.equals(
    Maybe.Some(R.prop('id', korvattava)),
    R.prop('korvattu-energiatodistus-id', energiatodistus))

export const isValidState = (korvattava, energiatodistus) =>
  isKorvattavissa(korvattava) ||
  (isKorvattuInET(korvattava, energiatodistus) && isKorvaaja(energiatodistus))

export const hasOtherKorvaaja = (korvattava, energiatodistus) => R.compose(
  Maybe.isSome,
  R.filter(R.complement(R.equals(energiatodistus.id))),
  R.prop('korvaava-energiatodistus-id')
)(korvattava);

export const isValidLocation = R.allPass([
  equals(R.path(['perustiedot', 'postinumero'])),
  equals(R.path(['perustiedot', 'kayttotarkoitus']))]);