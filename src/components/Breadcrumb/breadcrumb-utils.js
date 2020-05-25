import * as R from 'ramda';

export const locationParts = R.compose(R.reject(R.isEmpty), R.split('/'));

export const parseYritys = R.curry((i18n, _, locationParts) => {
  const [prefix, id] = locationParts;

  const crumbPart = {
    label: `${i18n('yritys.yritys')} ${id}`,
    url: `#/${prefix}/${id}`
  };

  if (R.equals('new', id)) {
    return R.assoc('label', i18n('yritys.uusi-yritys'), crumbPart);
  }

  return crumbPart;
});

export const breadcrumbParse = R.curry((location, i18n, user) =>
  R.compose(
    R.cond([[R.compose(R.equals('yritys'), R.head), parseYritys(i18n, user)]]),
    locationParts
  )(location)
);
