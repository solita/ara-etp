import * as R from 'ramda';

const locales = ['fi', 'sv'];

export const shortLocale = R.compose(R.head, R.split('-'));

export const label = R.curry((locale, item) =>
  R.prop(
    `label-${R.unless(
      R.includes(R.__, locales),
      R.always('fi')
    )(shortLocale(locale))}`,
    item
  )
);
