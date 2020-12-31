import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

const locales = ['fi', 'sv'];

export const shortLocale = R.compose(R.head, R.split('-'));

export const isSV = R.compose(R.equals('sv'), shortLocale);

export const label = R.curry((locale, item) =>
  R.prop(
    `label-${R.unless(
      R.includes(R.__, locales),
      R.always('fi')
    )(shortLocale(locale))}`,
    item
  )
);

export const path = R.curry((locale, path) =>
  R.adjust(-1, R.concat(R.__, '-' + locale), path));

const uniqueViolationKey = R.compose(
  R.map(R.concat('unique-violations.')),
  R.map(R.prop('constraint')),
  R.filter(R.propEq('type', 'unique-violation')),
  Maybe.fromNull,
  R.prop('body')
);

export const uniqueViolationMessage = (i18n, response, defaultKey) => {
  const key = Maybe.orSome(defaultKey, uniqueViolationKey(response));
  return R.replace('{value}', response.body.value, i18n(key));
};
