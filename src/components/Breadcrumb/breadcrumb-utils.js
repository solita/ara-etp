import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

export const locationParts = R.compose(R.reject(R.isEmpty), R.split('/'));

export const laatijanYrityksetCrumb = R.curry((i18n, user) =>
  R.map(
    u => ({
      label: i18n('navigation.yritykset'),
      url: `#/laatija/${R.prop('id', u)}/yritykset`
    }),
    user
  )
);

export const parseYritys = R.curry((idTranslate, i18n, user, locationParts) => {
  const [prefix, id] = locationParts;
  const label = R.compose(
    Maybe.orSome(`${i18n('yritys.yritys')} ${id}`),
    Maybe.fromNull,
    R.path([prefix, id])
  )(idTranslate);

  const crumbPart = {
    label: i18n(label),
    url: `#/${prefix}/${id}`
  };

  return R.compose(
    Maybe.orSome(crumbPart),
    R.map(R.compose(R.append(crumbPart), Array.of)),
    laatijanYrityksetCrumb
  )(i18n, user);
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

export const isCurrentUser = R.curry((id, user) =>
  R.compose(R.equals(parseInt(id, 10)), R.prop('id'))(user)
);

export const shouldPrependKayttajat = R.curry((id, user) =>
  R.not(
    R.either(
      R.equals('all'),
      R.compose(Maybe.isSome, R.filter(R.__, user), isCurrentUser)
    )(id)
  )
);

export const parseKayttaja = R.curry(
  (idTranslate, i18n, user, locationParts) => {
    const [prefix, id] = locationParts;

    const label = R.compose(
      Maybe.orSome(
        R.compose(
          Maybe.orSome(`${i18n('navigation.kayttaja')} ${id}`),
          Maybe.fromNull,
          R.path([prefix, id])
        )(idTranslate)
      ),
      R.map(R.always('navigation.omattiedot')),
      R.filter(isCurrentUser(id))
    )(user);

    const crumbPart = {
      label: i18n(label),
      url: `#/${prefix}/${id}`
    };

    return R.when(
      R.always(shouldPrependKayttajat(id, user)),
      R.prepend({ label: i18n('navigation.kayttajat'), url: `#/${prefix}/all` })
    )([crumbPart]);
  }
);

export const parseLaatijaRootActionCrumb = R.curry((i18n, prefix, action) => {
  const actionLabels = {
    all: 'navigation.laatijat',
    laatijoidentuonti: 'navigation.laatijoidentuonti'
  };

  return {
    label: R.compose(i18n, R.defaultTo(''), R.prop(R.__, actionLabels))(action),
    url: `#/${prefix}/${action}`
  };
});

export const parseSingleLaatijaActionCrumb = R.curry(
  (i18n, prefix, id, action) => {
    const actionLabels = {
      yritykset: 'navigation.yritykset'
    };

    const crumbPart = {
      label: `${R.compose(
        i18n,
        R.defaultTo(''),
        R.prop(R.__, actionLabels)
      )(action)}`,
      url: `#/${prefix}/${id}/${action}`
    };

    return crumbPart;
  }
);

export const parseLaatija = R.curry((i18n, _, locationParts) => {
  const [prefix, root, action] = R.compose(
    R.take(3),
    R.concat(R.__, R.times(Maybe.None, 3)),
    R.map(Maybe.fromNull)
  )(locationParts);

  const crumbParts = [
    R.lift(parseLaatijaRootActionCrumb(i18n))(prefix, root),
    R.lift(parseSingleLaatijaActionCrumb(i18n))(prefix, root, action)
  ];

  return R.compose(Maybe.get, R.last, R.reject(Maybe.isNone))(crumbParts);
});

export const parseAction = R.curry((i18n, _, [action]) => {
  const crumbPart = {
    label: i18n(`navigation.${action}`),
    url: `#/${action}`
  };

  return crumbPart;
});

export const breadcrumbParse = R.curry((idTranslate, location, i18n, user) =>
  R.compose(
    R.cond([
      [
        R.compose(R.equals('yritys'), R.head),
        parseYritys(idTranslate, i18n, user)
      ],
      [
        R.compose(R.equals('energiatodistus'), R.head),
        parseEnergiatodistus(i18n, user)
      ],
      [
        R.compose(R.equals('kayttaja'), R.head),
        parseKayttaja(idTranslate, i18n, user)
      ],
      [R.compose(R.equals('laatija'), R.head), parseLaatija(i18n, user)],
      [
        R.compose(
          R.includes(R.__, [
            'halytykset',
            'kaytonvalvonta',
            'tyojono',
            'viestit',
            'myinfo'
          ]),
          R.head
        ),
        parseAction(i18n, user)
      ],
      [R.T, R.always({ label: '', url: '' })]
    ]),
    locationParts
  )(location)
);
