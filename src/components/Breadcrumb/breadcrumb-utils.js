import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

import * as Navigation from '@Utility/navigation';
import * as RUtils from '@Utility/ramda-utils';

import * as KayttajaUtils from '@Component/Kayttaja/kayttaja-utils';

const createCrumb = R.curry((url, label) => ({ url, label }));

const labelLens = R.lensProp('label');

const routeReservedKeywords = [
  'all',
  'new',
  'allekirjoitus',
  'liitteet',
  'viestit',
  'muutoshistoria',
  'laatijoidentuonti',
  'yritykset'
];

export const translateReservedKeywordLabel = R.curry(
  (i18n, keyword, breadcrumb) =>
    R.when(
      R.always(R.includes(keyword, routeReservedKeywords)),
      R.over(labelLens, i18n),
      breadcrumb
    )
);

export const yritysCrumb = R.curry((idTranslate, i18n, id) =>
  R.compose(
    createCrumb(`#/yritys/${id}`),
    Maybe.orSome(`${i18n('yritys.yritys')} ${id}`),
    Maybe.fromNull,
    R.path(['yritys', id])
  )(idTranslate)
);

export const laatijanYrityksetCrumb = R.curry((i18n, user) =>
  R.compose(
    createCrumb(R.__, i18n('navigation.yritykset')),
    u => `#/laatija/${R.prop('id', u)}/yritykset`
  )(user)
);

export const parseYritys = R.curry((idTranslate, i18n, user, locationParts) => {
  const [id] = locationParts;

  return R.compose(
    R.unless(
      R.always(R.equals('all', id)),
      R.prepend(laatijanYrityksetCrumb(i18n, user))
    ),
    Array.of,
    translateReservedKeywordLabel(i18n, id),
    yritysCrumb(idTranslate, i18n)
  )(id);
});

export const energiatodistusRootActionCrumb = R.curry((i18n, keyword) =>
  R.compose(
    createCrumb(R.__, i18n('navigation.energiatodistukset')),
    R.concat('#/energiatodistus/')
  )(keyword)
);

export const singleEnergiatodistusCrumb = R.curry(
  (idTranslate, i18n, version, id) =>
    R.compose(
      translateReservedKeywordLabel(i18n, id),
      createCrumb(`#/energiatodistus/${version}/${id}`),
      Maybe.orSome(`${i18n('navigation.et')} ${id}`),
      Maybe.fromNull,
      R.path(['energiatodistus', id])
    )(idTranslate)
);

const energiatodistusActionLabels = {
  allekirjoitus: 'navigation.allekirjoitus',
  liitteet: 'navigation.liitteet',
  viestit: 'navigation.viestit',
  muutoshistoria: 'navigation.muutoshistoria'
};

export const singleEnergiatodistusActionCrumb = R.curry(
  (i18n, version, id, keyword) =>
    R.compose(
      Maybe.orSome(
        createCrumb(`#/energiatodistus/${version}/${id}/${keyword}`, keyword)
      ),
      R.map(translateReservedKeywordLabel(i18n, keyword)),
      R.lift(createCrumb(`#/energiatodistus/${version}/${id}/${keyword}`)),
      Maybe.fromNull,
      R.prop(keyword)
    )(energiatodistusActionLabels)
);

export const parseEnergiatodistus = R.curry(
  (idTranslate, i18n, locationParts) => {
    const [root, id, action] = R.compose(
      RUtils.fillAndTake(3, Maybe.None),
      R.map(Maybe.fromNull)
    )(locationParts);

    const crumbParts = [
      R.lift(energiatodistusRootActionCrumb(i18n))(root),
      R.lift(singleEnergiatodistusCrumb(idTranslate, i18n))(root, id),
      R.lift((root, id, action) =>
        R.compose(
          R.prepend(singleEnergiatodistusCrumb(idTranslate, i18n, root, id)),
          Array.of,
          singleEnergiatodistusActionCrumb(i18n)
        )(root, id, action)
      )(root, id, action)
    ];

    return R.flatten(
      R.uniq([
        energiatodistusRootActionCrumb(i18n, 'all'),
        R.compose(Maybe.get, R.last, R.reject(Maybe.isNone))(crumbParts)
      ])
    );
  }
);

export const isCurrentUser = R.curry((id, user) =>
  R.compose(R.equals(parseInt(id, 10)), R.prop('id'))(user)
);

export const parseKayttaja = R.curry(
  (idTranslate, i18n, user, locationParts) => {
    const [id] = locationParts;

    if (isCurrentUser(id, user)) {
      return createCrumb(`#/kayttaja/${id}`, i18n('navigation.omattiedot'));
    }

    return R.compose(
      R.unless(
        R.always(R.equals('all', id)),
        R.prepend(createCrumb(`#/kayttaja/all`, i18n('navigation.kayttajat')))
      ),
      Array.of,
      translateReservedKeywordLabel(i18n, id),
      createCrumb(`#/kayttaja/${id}`),
      Maybe.orSome(`${i18n('navigation.kayttaja')} ${id}`),
      Maybe.fromNull,
      R.path(['kayttaja', id])
    )(idTranslate);
  }
);

export const parseLaatijaRootActionCrumb = R.curry((i18n, action) => {
  const actionLabels = {
    all: 'navigation.laatijat',
    laatijoidentuonti: 'navigation.laatijoidentuonti'
  };

  return R.compose(
    translateReservedKeywordLabel(i18n, action),
    createCrumb(`#/laatija/${action}`),
    Maybe.orSome(action),
    Maybe.fromNull,
    R.prop(action)
  )(actionLabels);
});

export const parseSingleLaatijaActionCrumb = R.curry((i18n, id, action) => {
  const actionLabels = {
    yritykset: 'navigation.yritykset'
  };

  return R.compose(
    translateReservedKeywordLabel(i18n, action),
    createCrumb(`#/laatija/${id}/${action}`),
    Maybe.orSome(action),
    Maybe.fromNull,
    R.prop(action)
  )(actionLabels);
});

export const parseLaatija = R.curry((i18n, locationParts) => {
  const [root, action] = R.compose(
    RUtils.fillAndTake(2, Maybe.None),
    R.map(Maybe.fromNull)
  )(locationParts);

  const crumbParts = [
    R.lift(parseLaatijaRootActionCrumb(i18n))(root),
    R.lift(parseSingleLaatijaActionCrumb(i18n))(root, action)
  ];

  return R.compose(Maybe.get, R.last, R.reject(Maybe.isNone))(crumbParts);
});

export const parseAction = R.curry((i18n, action) => {
  const crumbPart = {
    label: i18n(`navigation.${action}`),
    url: `#/${action}`
  };

  return crumbPart;
});

export const roleBasedRootCrumb = R.curry((i18n, user) => {
  return R.compose(
    R.cond([
      [
        R.equals(KayttajaUtils.laatijaRole),
        R.always(
          createCrumb(
            '#/energiatodistus/all',
            i18n('navigation.energiatodistukset')
          )
        )
      ],
      [
        R.equals(KayttajaUtils.patevyydentoteajaRole),
        R.always(
          createCrumb(
            '#/laatija/laatijoidentuonti',
            i18n('navigation.laatijoidentuonti')
          )
        )
      ],
      [
        R.equals(KayttajaUtils.paakayttajaRole),
        R.always(createCrumb('#/tyojono', i18n('navigation.tyojono')))
      ]
    ]),
    R.prop('rooli')
  )(user);
});

export const breadcrumbParse = R.curry((idTranslate, location, i18n, user) =>
  R.compose(
    R.uniq,
    R.flatten,
    R.prepend(roleBasedRootCrumb(i18n, user)),
    Array.of,
    R.cond([
      [
        R.compose(R.equals('yritys'), R.head),
        R.compose(parseYritys(idTranslate, i18n, user), R.tail)
      ],
      [
        R.compose(R.equals('energiatodistus'), R.head),
        R.compose(parseEnergiatodistus(idTranslate, i18n), R.tail)
      ],
      [
        R.compose(R.equals('kayttaja'), R.head),
        R.compose(parseKayttaja(idTranslate, i18n, user), R.tail)
      ],
      [
        R.compose(R.equals('laatija'), R.head),
        R.compose(parseLaatija(i18n), R.tail)
      ],
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
        parseAction(i18n)
      ],
      [R.T, R.always({ label: '', url: '' })]
    ]),
    Navigation.locationParts
  )(location)
);
