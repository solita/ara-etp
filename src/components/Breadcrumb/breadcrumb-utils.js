import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

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

export const energiatodistusRootActionCrumb = R.curry(
  (i18n, prefix, action) => ({
    label: i18n('energiatodistus.breadcrumb.energiatodistus'),
    url: `#/${prefix}/${action}`
  })
);

export const singleEnergiatodistusCrumb = R.curry(
  (i18n, prefix, version, id) => {
    const crumbPart = {
      label: `${i18n('navigation.et')} ${id}`,
      url: `#/${prefix}/${version}/${id}`
    };
    if (R.equals('new', id)) {
      return R.assoc(
        'label',
        `${i18n('energiatodistus.breadcrumb.uusi-energiatodistus')} ${version}`,
        crumbPart
      );
    }

    return crumbPart;
  }
);

export const singleEnergiatodistusActionCrumb = R.curry(
  (i18n, prefix, version, id, action) => {
    const actionLabels = {
      allekirjoitus: 'navigation.allekirjoitus',
      liitteet: 'navigation.liitteet',
      viestit: 'navigation.viestit',
      muutoshistoria: 'navigation.muutoshistoria'
    };

    const crumbPart = {
      label: `${R.compose(
        i18n,
        R.defaultTo(''),
        R.prop(R.__, actionLabels)
      )(action)}`,
      url: `#/${prefix}/${version}/${id}/${action}`
    };

    return R.compose(
      R.prepend(singleEnergiatodistusCrumb(i18n, prefix, version, id)),
      Array.of
    )(crumbPart);
  }
);

export const parseEnergiatodistus = R.curry((i18n, _, locationParts) => {
  const [prefix, root, id, action] = R.compose(
    R.take(4),
    R.concat(R.__, R.times(Maybe.None, 4)),
    R.map(Maybe.fromNull)
  )(locationParts);

  const crumbParts = [
    R.lift(energiatodistusRootActionCrumb(i18n))(prefix, root),
    R.lift(singleEnergiatodistusCrumb(i18n))(prefix, root, id),
    R.lift(singleEnergiatodistusActionCrumb(i18n))(prefix, root, id, action)
  ];

  return R.compose(Maybe.get, R.last, R.reject(Maybe.isNone))(crumbParts);
});

export const breadcrumbParse = R.curry((location, i18n, user) =>
  R.compose(
    R.cond([
      [R.compose(R.equals('yritys'), R.head), parseYritys(i18n, user)],
      [
        R.compose(R.equals('energiatodistus'), R.head),
        parseEnergiatodistus(i18n, user)
      ]
    ]),
    locationParts
  )(location)
);
