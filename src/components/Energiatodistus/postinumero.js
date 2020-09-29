import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as Validation from '@Utility/validation';
import * as Parsers from '@Utility/parsers';
import * as LocaleUtils from '@Language/locale-utils';

export const fullLabel = locale => R.compose(
  R.join(' '),
  R.juxt([
    pn => R.toString(pn.id).padStart(5, '0'),
    LocaleUtils.label(locale)]));

export const formatPostinumero = (postinumerot, locale) => postinumero => R.compose(
  Maybe.orSome(postinumero),
  R.map(fullLabel(locale)),
  Maybe.findById(R.__, postinumerot),
  parseInt
)(postinumero);

const parsePostinumero = R.compose(
  R.map(R.slice(0, 5)),
  Parsers.optionalString
);

const validatePostinumero = postinumerot => R.compose(
  Maybe.isSome,
  Maybe.findById(R.__, postinumerot),
  nro => parseInt(nro)
);

export const Type = (postinumerot) => ({
  parse: parsePostinumero,
  validators: [Validation.liftValidator({
    predicate: validatePostinumero(postinumerot),
    label: R.applyTo('validation.postinumero-not-found')
  })]
})