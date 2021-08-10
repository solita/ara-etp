import * as R from 'ramda';
import * as RUtils from '@Utility/ramda-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as Future from '@Utility/future-utils';
import * as Kayttajat from '@Utility/kayttajat';

import * as ViestiApi from '@Pages/viesti/viesti-api';

export const locationParts = R.compose(R.reject(R.isEmpty), R.split('/'));

const linksForLaatija = R.curry((isDev, i18n, whoami) => [
  {
    label: i18n('navigation.energiatodistukset'),
    href: '#/energiatodistus/all'
  },
  {
    label: i18n('navigation.yritykset'),
    href: `#/laatija/${whoami.id}/yritykset`
  },
  ...(isDev
    ? [
        {
          label: i18n('navigation.valvonta.oikeellisuus.all'),
          href: '#/valvonta/oikeellisuus/all'
        }
      ]
    : []),
  {
    label: i18n('navigation.viestit'),
    href: '#/viesti/all',
    badge: R.compose(
      R.chain(R.ifElse(R.equals(0), Future.reject, Future.resolve)),
      R.map(R.prop('count'))
    )(ViestiApi.getKetjutUnread)
  }
]);

export const linksForPatevyydentoteaja = R.curry((isDev, i18n, whoami) => [
  {
    label: i18n('navigation.laatijoidentuonti'),
    href: '#/laatija/laatijoidentuonti'
  },
  { label: i18n('navigation.laatijat'), href: '#/laatija/all' }
]);

export const linksForEnergiatodistus = R.curry(
  (isDev, i18n, whoami, version, id) => [
    {
      label: `${i18n('navigation.et')} ${id}`,
      href: `#/energiatodistus/${version}/${id}`
    },
    ...(!Kayttajat.isLaskuttaja(whoami)
      ? [
          {
            label: i18n('navigation.liitteet'),
            href: `#/energiatodistus/${version}/${id}/liitteet`
          }
        ]
      : []),
    ...(Kayttajat.isPaakayttaja(whoami) || isDev
      ? [
          {
            label: i18n('navigation.valvonta.oikeellisuus.valvonta'),
            href: `#/valvonta/oikeellisuus/${version}/${id}`
          }
        ]
      : []),

    ...(isDev
      ? [
          {
            label: i18n('navigation.energiatodistus-viestit'),
            href: `#/energiatodistus/${version}/${id}/viestit`
          }
        ]
      : []),
    ...(isDev && !Kayttajat.isLaskuttaja(whoami)
      ? [
          {
            label: i18n('navigation.muutoshistoria'),
            href: `#/energiatodistus/${version}/${id}/muutoshistoria`
          }
        ]
      : [])
  ]
);

export const linksForNewEnergiatodistus = R.curry((i18n, version) => [
  {
    label: `${i18n('navigation.uusi-energiatodistus')}`,
    href: `#/energiatodistus/${version}/new`
  },
  // Hidden until implemented
  //{
  //  label: i18n('navigation.viestit'),
  //  disabled: true
  //},
  {
    label: i18n('navigation.liitteet'),
    disabled: true
  },
  {
    label: i18n('navigation.muutoshistoria'),
    disabled: true
  }
]);

export const linksForKayttaja = R.curry((i18n, kayttaja) => {
  return [
    {
      label: `${kayttaja.etunimi} ${kayttaja.sukunimi}`,
      href: `#/kayttaja/${kayttaja.id}`
    },
    {
      label: i18n('navigation.yritykset'),
      href: `#/laatija/${kayttaja.id}/yritykset`
    }
  ];
});

export const parseKayttaja = R.curry(
  (isDev, whoami, i18n, idTranslate, locationParts) => {
    if (
      (Kayttajat.isPatevyydentoteaja(whoami) || Kayttajat.isPaakayttaja) &&
      R.compose(
        R.either(R.equals('laatijoidentuonti'), R.equals('all')),
        R.head
      )(locationParts)
    ) {
      return parseRoot(isDev, i18n, whoami);
    }

    const id = R.head(locationParts);
    const kayttaja = R.compose(
      Maybe.fromNull,
      R.path(['kayttaja', parseInt(id, 10)])
    )(idTranslate);

    if (R.contains('yritykset', locationParts)) {
      return [];
    }

    return R.compose(
      Maybe.orSome([]),
      R.map(linksForKayttaja(i18n)),
      R.filter(Kayttajat.isLaatija)
    )(kayttaja);
  }
);

export const linksForYritys = R.curry((i18n, idTranslate, id) => [
  R.hasPath(['yritys', parseInt(id, 10)], idTranslate)
    ? {
        label: R.path(['yritys', parseInt(id, 10)], idTranslate),
        href: `#/yritys/${id}`
      }
    : {
        label: `${i18n('navigation.yritys')} ${id}`,
        href: `#/yritys/${id}`
      },
  {
    label: i18n('navigation.yritys-laatijat'),
    href: `#/yritys/${id}/laatijat`
  }
]);

export const parseYritys = R.curry(
  (isDev, i18n, whoami, idTranslate, locationParts) => {
    const id = locationParts[0];

    if (R.equals('all', id)) return parseRoot(isDev, i18n, whoami);

    if (R.equals('new', id) || R.contains('laatijat', locationParts)) {
      return [];
    }

    return linksForYritys(i18n, idTranslate, id);
  }
);

export const parseValvontaOikeellisuus = R.curry((isDev, i18n, whoami, locationParts) => {
  if (R.head(locationParts) === 'all') return parseRoot(isDev, i18n, whoami);
  if (R.length(locationParts) > 2) return [];

  return parseEnergiatodistus(isDev, i18n, whoami, locationParts);
});

export const parseValvontaKaytto = R.curry((isDev, i18n, whoami, locationParts) => {
  const id = locationParts[0];
  if (R.equals(id, 'all')) {
    return parseRoot(isDev, i18n, whoami);
  } else if (R.equals(id, 'new')) {
    return [];
  } else {
    return [
      {
        label: R.replace('{id}', id, i18n('navigation.valvonta.kaytto.kohde')),
        href: `#/valvonta/kaytto/${id}/kohde`
      },
      {
        label: i18n('navigation.valvonta.kaytto.valvonta'),
        href: `#/valvonta/kaytto/${id}/valvonta`
      }];
  };

  return parseEnergiatodistus(isDev, i18n, whoami, locationParts);
});

export const linksForPaakayttaja = R.curry((isDev, i18n, whoami) => [
  {
    label: i18n('navigation.energiatodistukset'),
    href: '#/energiatodistus/all'
  },
  {
    label: i18n('navigation.laatijat'),
    href: '#/laatija/all'
  },
  {
    label: i18n('navigation.valvonta.oikeellisuus.all'),
    href: `#/valvonta/oikeellisuus/all?valvoja-id=${whoami.id}&has-valvoja=false`
  },
  ...(isDev
    ? [
        {
          label: i18n('navigation.valvonta.kaytto.all'),
          href: `#/valvonta/kaytto/all?valvoja-id=${whoami.id}&has-valvoja=false`
        }
      ]
    : []),
  {
    label: i18n('navigation.viestit'),
    href: '#/viesti/all',
    badge: R.compose(
      R.chain(R.ifElse(R.equals(0), Future.reject, Future.resolve)),
      R.map(R.prop('count'))
    )(ViestiApi.getKetjutUnread)
  }
]);

export const linksForLaskuttaja = R.curry((isDev, i18n, whoami) => [
  {
    label: i18n('navigation.energiatodistukset'),
    href: '#/energiatodistus/all'
  },
  {
    label: i18n('navigation.laatijat'),
    href: '#/laatija/all'
  },
  {
    label: i18n('navigation.viestit'),
    href: '#/viesti/all',
    badge: R.compose(
      R.chain(R.ifElse(R.equals(0), Future.reject, Future.resolve)),
      R.map(R.prop('count'))
    )(ViestiApi.getKetjutUnread)
  }
]);

const kayttajaLinksMap = Object.freeze({
  0: linksForLaatija,
  1: linksForPatevyydentoteaja,
  2: linksForPaakayttaja,
  3: linksForLaskuttaja
});

export const parseEnergiatodistus = R.curry(
  (isDev, i18n, whoami, locationParts) => {
    const [version, id] = R.compose(
      RUtils.fillAndTake(2, Maybe.None),
      R.map(Maybe.fromNull)
    )(locationParts);

    if (
      R.compose(
        Maybe.isSome,
        R.filter(R.either(R.equals('new'), R.equals('viestit')))
      )(id)
    ) {
      return [];
    }

    return R.compose(
      Maybe.orSome(parseRoot(isDev, i18n, whoami)),
      R.lift(linksForEnergiatodistus(isDev, i18n, whoami))
    )(version, id);
  }
);

export const parseViesti = R.curry((isDev, i18n, whoami, locationParts) => {
  if (R.equals('all', R.head(locationParts))) {
    return parseRoot(isDev, i18n, whoami);
  }

  return [];
});

export const parseRoot = R.curry((isDev, i18n, whoami) =>
  R.converge(R.apply, [
    R.compose(R.prop(R.__, kayttajaLinksMap), R.prop('rooli')),
    R.append(R.__, [isDev, i18n])
  ])(whoami)
);

export const navigationParse = R.curry(
  (isDev, i18n, whoami, location, idTranslate) =>
    R.compose(
      R.flatten,
      Array.of,
      R.cond([
        [
          R.compose(R.equals('energiatodistus'), R.head),
          R.compose(parseEnergiatodistus(isDev, i18n, whoami), R.tail)
        ],
        [
          R.compose(R.equals('yritys'), R.head),
          R.compose(parseYritys(isDev, i18n, whoami, idTranslate), R.tail)
        ],
        [
          R.compose(
            R.either(R.equals('kayttaja'), R.equals('laatija')),
            R.head
          ),
          R.compose(parseKayttaja(isDev, whoami, i18n, idTranslate), R.tail)
        ],
        [
          R.compose(R.equals(['valvonta', 'oikeellisuus']), R.take(2)),
          R.compose(parseValvontaOikeellisuus(isDev, i18n, whoami), R.drop(2))
        ],
        [
          R.compose(R.equals(['valvonta', 'kaytto']), R.take(2)),
          R.compose(parseValvontaKaytto(isDev, i18n, whoami), R.drop(2))
        ],
        [
          R.compose(R.equals('viesti'), R.head),
          R.compose(parseViesti(isDev, i18n, whoami), R.tail)
        ],
        [R.T, R.always([])]
      ]),
      locationParts
    )(location)
);

export const defaultHeaderMenuLinks = i18n => [
  {
    href: `#/myinfo`,
    text: i18n('navigation.omattiedot')
  },
  {
    href: `/api/logout`,
    text: i18n('navigation.kirjaudu-ulos')
  }
];

export const roleBasedHeaderMenuLinks = R.curry((i18n, whoami) => {
  if (Kayttajat.isPaakayttaja(whoami)) {
    return [
      // Hidden until implemented
      // {
      //   href: `#/kayttaja/all`,
      //   text: i18n('navigation.kayttajahallinta')
      // },
      {
        href: `#/yritys/all`,
        text: i18n('navigation.yritykset')
      }
    ];
  }

  if (Kayttajat.isLaskuttaja(whoami)) {
    return [
      {
        href: `#/yritys/all`,
        text: i18n('navigation.yritykset')
      }
    ];
  }

  return [];
});
