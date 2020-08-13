import * as R from 'ramda';
import * as RUtils from '@Utility/ramda-utils';
import * as Maybe from '@Utility/maybe-utils';

export const locationParts = R.compose(R.reject(R.isEmpty), R.split('/'));

const linksForLaatija = R.curry((i18n, kayttaja) => [
  {
    label: i18n('navigation.energiatodistukset'),
    href: '#/energiatodistus/all'
  },
  { label: i18n('navigation.viestit'), href: '#/viestit' },
  {
    label: i18n('navigation.yritykset'),
    href: `#/laatija/${kayttaja.id}/yritykset`
  },
  {
    label: i18n('navigation.omattiedot'),
    href: '#/myinfo',
    activePath: `#/kayttaja/${kayttaja.id}`
  }
]);

export const linksForPatevyydentoteaja = R.curry((i18n, kayttaja) => [
  {
    label: i18n('navigation.laatijoidentuonti'),
    href: '#/laatija/laatijoidentuonti'
  },
  { label: i18n('navigation.laatijat'), href: '#/laatija/all' },
  {
    label: i18n('navigation.omattiedot'),
    href: '#/myinfo',
    activePath: `#/kayttaja/${kayttaja.id}`
  }
]);

export const linksForEnergiatodistus = R.curry((i18n, version, id) => [
  {
    label: `${i18n('navigation.et')} ${id}`,
    href: `#/energiatodistus/${version}/${id}`
  },
  {
    label: i18n('navigation.viestit'),
    href: `#/energiatodistus/${version}/${id}/viestit`
  },
  {
    label: i18n('navigation.liitteet'),
    href: `#/energiatodistus/${version}/${id}/liitteet`
  },
  {
    label: i18n('navigation.muutoshistoria'),
    href: `#/energiatodistus/${version}/${id}/muutoshistoria`
  }
]);

export const linksForNewEnergiatodistus = R.curry((i18n, version) => [
  {
    label: `${i18n('navigation.uusi-energiatodistus')}`,
    href: `#/energiatodistus/${version}/new`
  },
  {
    label: i18n('navigation.viestit'),
    disabled: true
  },
  {
    label: i18n('navigation.liitteet'),
    disabled: true
  },
  {
    label: i18n('navigation.muutoshistoria'),
    disabled: true
  }
]);

export const linksForPaakayttaja = R.curry((i18n, kayttaja) => [
  {
    label: i18n('navigation.energiatodistukset'),
    href: '#/energiatodistus/all'
  },
  {
    label: i18n('navigation.laatijat'),
    href: '#/laatija/all'
  },
  { label: i18n('navigation.viestit'), href: '#/viestit' }
]);

const kayttajaLinksMap = Object.freeze({
  0: linksForLaatija,
  1: linksForPatevyydentoteaja,
  2: linksForPaakayttaja
});

export const parseEnergiatodistus = R.curry((i18n, kayttaja, locationParts) => {
  const [version, id] = R.compose(
    RUtils.fillAndTake(2, Maybe.None),
    R.map(Maybe.fromNull)
  )(locationParts);

  if (R.compose(Maybe.isSome, R.filter(R.equals('new')))(id)) {
    return R.compose(
      Maybe.orSome(parseRoot(i18n, kayttaja)),
      R.map(linksForNewEnergiatodistus(i18n))
    )(version);
  }

  return R.compose(
    Maybe.orSome(parseRoot(i18n, kayttaja)),
    R.lift(linksForEnergiatodistus(i18n))
  )(version, id);
});

export const parseRoot = R.curry((i18n, kayttaja) =>
  R.converge(R.apply, [
    R.compose(R.prop(R.__, kayttajaLinksMap), R.prop('rooli')),
    R.append(R.__, [i18n])
  ])(kayttaja)
);

export const navigationParse = R.curry((i18n, kayttaja, location) =>
  R.compose(
    R.flatten,
    Array.of,
    R.cond([
      [
        R.compose(R.equals('energiatodistus'), R.head),
        R.compose(parseEnergiatodistus(i18n, kayttaja), R.tail)
      ],
      [
        R.compose(
          R.includes(R.__, [
            'yritys',
            'kayttaja',
            'laatija',
            'halytykset',
            'kaytonvalvonta',
            'tyojono',
            'viestit',
            'myinfo'
          ]),
          R.head
        ),
        R.always(parseRoot(i18n, kayttaja))
      ],
      [R.T, R.always([{ label: '...', href: '#/' }])]
    ]),
    locationParts
  )(location)
);

export const defaultHeaderMenuLinks = i18n => [
  {
    href: `/api/logout`,
    text: i18n('navigation.kirjaudu-ulos')
  }
];

export const roleBasedHeaderMenuLinks = R.curry((i18n, kayttaja) => {
  if (kayttaja.rooli === 2) {
    return [
      {
        href: `#/kayttaja/all`,
        text: i18n('navigation.kayttajahallinta')
      },
      {
        href: `#/yritys/all`,
        text: i18n('navigation.yritykset')
      },
      {
        href: `#/myinfo`,
        text: i18n('navigation.omattiedot')
      }
    ];
  }

  return [];
});
