import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

const locales = ['fi', 'sv'];

export const shortLocale = R.compose(R.head, R.split('-'));

export const isSV = R.compose(R.equals('sv'), shortLocale);

export const prop = R.curry((name, locale, item) =>
  R.prop(
    name +
      '-' +
      R.unless(R.includes(R.__, locales), R.always('fi'))(shortLocale(locale)),
    item
  )
);

export const label = prop('label');

export const path = R.curry((locale, path) =>
  R.adjust(-1, R.concat(R.__, '-' + locale), path)
);

const uniqueViolationKey = R.compose(
  R.map(R.concat('unique-violations.')),
  R.map(R.prop('constraint')),
  R.filter(R.propEq('unique-violation', 'type')),
  Maybe.fromNull,
  R.prop('body')
);

export const uniqueViolationMessage = (i18n, response, defaultKey) =>
  Maybe.fold(
    i18n(defaultKey),
    key => R.replace('{value}', response.body.value, i18n(key)),
    uniqueViolationKey(response)
  );

export const labelForId = R.curry((locale, items) =>
  R.compose(Maybe.orSome(''), R.map(label(locale)), Maybe.findById(R.__, items))
);
