import * as R from 'ramda';
import * as RUtils from '@Utility/ramda-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as Future from '@Utility/future-utils';
import * as Kayttajat from '@Utility/kayttajat';

import * as ViestiApi from '@Component/viesti/viesti-api';

export const locationParts = R.compose(R.reject(R.isEmpty), R.split('/'));

const linksForLaatija = R.curry((isDev, i18n, kayttaja) => [
  {
    label: i18n('navigation.energiatodistukset'),
    href: '#/energiatodistus/all'
  },
  {
    label: i18n('navigation.yritykset'),
    href: `#/laatija/${kayttaja.id}/yritykset`
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

export const linksForPatevyydentoteaja = R.curry((isDev, i18n, kayttaja) => [
  {
    label: i18n('navigation.laatijoidentuonti'),
    href: '#/laatija/laatijoidentuonti'
  },
  { label: i18n('navigation.laatijat'), href: '#/laatija/all' }
]);

export const linksForEnergiatodistus = R.curry((i18n, kayttaja, version, id) =>
  R.when(
    R.always(!Kayttajat.isLaskuttaja(kayttaja)),
    R.append({
      label: i18n('navigation.liitteet'),
      href: `#/energiatodistus/${version}/${id}/liitteet`
    }),
    [
      // Hidden until implemented
      //{
      //  label: i18n('navigation.viestit'),
      //  href: `#/energiatodistus/${version}/${id}/viestit`
      //},
      {
        label: `${i18n('navigation.et')} ${id}`,
        href: `#/energiatodistus/${version}/${id}`
      }
      // Hidden until implemented
      // {
      //  label: i18n('navigation.muutoshistoria'),
      //  href: `#/energiatodistus/${version}/${id}/muutoshistoria`
      // }
    ]
  )
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
  }
  // Hidden until implemented
  //{
  //  label: i18n('navigation.muutoshistoria'),
  //  disabled: true
  //}
]);

export const linksForLaatijaOmatTiedot = R.curry((i18n, id, idTranslate) => [
  {
    label: R.compose(
      Maybe.orSome('...'),
      R.map(kayttaja => `${kayttaja.etunimi} ${kayttaja.sukunimi}`),
      Maybe.fromNull,
      R.path(['kayttaja', id])
    )(idTranslate),
    href: `#/kayttaja/${id}`
  },
  {
    label: i18n('navigation.yritykset'),
    href: `#/laatija/${id}/yritykset`
  }
]);

export const parseKayttaja = R.curry(
  (isDev, i18n, kayttaja, idTranslate, locationParts) => {
    if (Kayttajat.isPatevyydentoteaja(kayttaja)) {
      return linksForPatevyydentoteaja(isDev, i18n, kayttaja);
    }

    if (R.isEmpty(locationParts)) {
      return [];
    }
    const id = locationParts[0];
    if (R.equals('new', id)) {
      return [];
    } else if (R.equals('all', id)) {
      return R.converge(R.apply, [
        R.compose(R.prop(R.__, kayttajaLinksMap), R.prop('rooli')),
        R.append(R.__, [isDev, i18n])
      ])(kayttaja);
    } else if (Kayttajat.isLaatija(kayttaja)) {
      return linksForLaatijaOmatTiedot(i18n, id, idTranslate);
    } else {
      return [];
    }
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
  (i18n, kayttaja, idTranslate, locationParts) => {
    if (R.isEmpty(locationParts)) {
      return [];
    }
    const id = locationParts[0];
    if (R.equals('new', id) || R.equals('all', id)) {
      return [];
    } else {
      return linksForYritys(i18n, idTranslate, id);
    }
  }
);

export const linksForPaakayttaja = R.curry((isDev, i18n, kayttaja) => [
  {
    label: i18n('navigation.energiatodistukset'),
    href: '#/energiatodistus/all'
  },
  {
    label: i18n('navigation.laatijat'),
    href: '#/laatija/all'
  },
    isDev
    ? [
        {
          label: i18n('navigation.valvonta.oikeellisuus'),
          href: '#/valvonta/oikeellisuus/all'
        }
      ]
    : [],
  { label: i18n('navigation.viestit'), href: '#/viesti/all',badge: R.compose(
      R.map(R.prop('count'))
    )(ViestiApi.getKetjutUnread) }
]);

export const linksForLaskuttaja = linksForPaakayttaja;

const kayttajaLinksMap = Object.freeze({
  0: linksForLaatija,
  2: linksForPaakayttaja,
});

export const parseEnergiatodistus = R.curry(
  (isDev, i18n, kayttaja, locationParts) => {
    const [version, id] = R.compose(
      RUtils.fillAndTake(2, Maybe.None),
      R.map(Maybe.fromNull)
    )(locationParts);

    if (R.compose(Maybe.isSome, R.filter(R.equals('new')))(id)) {
      return [];
    }

    return R.compose(
      Maybe.orSome(parseRoot(isDev, i18n, kayttaja)),
      R.lift(linksForEnergiatodistus(i18n, kayttaja))
    )(version, id);
  }
);

export const parseRoot = R.curry((isDev, i18n, kayttaja) =>
  R.converge(R.apply, [
    R.compose(R.prop(R.__, kayttajaLinksMap), R.prop('rooli')),
    R.append(R.__, [isDev, i18n])
  ])(kayttaja)
);

export const navigationParse = R.curry(
  (isDev, i18n, kayttaja, location, idTranslate) =>
    R.compose(
      R.flatten,
      Array.of,
      R.cond([
        [
          R.compose(R.equals('energiatodistus'), R.head),
          R.compose(parseEnergiatodistus(isDev, i18n, kayttaja), R.tail)
        ],
        [
          R.compose(R.equals('yritys'), R.head),
          R.compose(parseYritys(i18n, kayttaja, idTranslate), R.tail)
        ],
        [
          R.compose(
            R.either(R.equals('kayttaja'), R.equals('laatija')),
            R.head
          ),
          R.compose(parseKayttaja(isDev, i18n, kayttaja, idTranslate), R.tail)
        ],
        [
          R.compose(R.equals('viesti'), R.head),
          R.compose(
            R.ifElse(
              R.equals('all'),
              R.always(parseRoot(isDev, i18n, kayttaja)),
              R.always([])
            ),
            R.head,
            R.tail
          )
        ],
        [
          R.compose(R.equals('valvonta'), R.head),
          R.compose(
            R.cond([
              [
                R.compose(R.equals('oikeellisuus'), R.head),
                R.compose(
                  R.ifElse(
                    R.equals('all'),
                    R.always(parseRoot(isDev, i18n, kayttaja)),
                    R.always([])
                  ),
                  R.head,
                  R.tail
                )
              ],
              [R.T, R.always([])]
            ]),
            R.drop(1)
          )
        ],
        [
          R.compose(R.includes(R.__, ['laatija', 'myinfo']), R.head),
          R.always(parseRoot(isDev, i18n, kayttaja))
        ],
        [R.compose(R.equals('ohje'), R.head), R.always([])],
        [R.T, R.always([{ label: '...', href: '#/' }])]
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

export const roleBasedHeaderMenuLinks = R.curry((i18n, kayttaja) => {
  if (Kayttajat.isPaakayttaja(kayttaja)) {
    return [
      {
        href: `#/kayttaja/all`,
        text: i18n('navigation.kayttajahallinta')
      },
      {
        href: `#/yritys/all`,
        text: i18n('navigation.yritykset')
      }
    ];
  }

  if (Kayttajat.isLaskuttaja(kayttaja)) {
    return [
      {
        href: `#/yritys/all`,
        text: i18n('navigation.yritykset')
      }
    ];
  }

  return [];
});
