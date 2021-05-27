import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as Kayttajat from '@Utility/kayttajat';

export const parseLocation = R.curry((i18n, idTranslate, location, whoami) =>
  R.compose(
    R.uniq,
    R.prepend(rootCrumb(i18n, whoami)),
    parseCrumb(i18n, idTranslate, whoami),
    R.split('/'),
    R.tail
  )(location)
);

export const parseCrumb = R.curry((i18n, idTranslate, whoami, location) =>
  R.cond([
    [
      R.compose(R.equals('yritys'), R.head),
      R.compose(yritysCrumb(i18n, idTranslate, whoami), R.tail)
    ],
    [
      R.compose(R.equals('kayttaja'), R.head),
      R.compose(kayttajaCrumb(i18n, idTranslate, whoami), R.tail)
    ],
    [
      R.compose(R.equals('laatija'), R.head),
      R.compose(laatijaCrumb(i18n, idTranslate, whoami), R.tail)
    ],
    [
      R.compose(R.equals('energiatodistus'), R.head),
      R.compose(energiatodistusCrumb(i18n), R.tail)
    ],
    [
      R.compose(R.equals('viesti'), R.head),
      R.compose(viestiCrumb(i18n, idTranslate), R.tail)
    ],
    [
      R.compose(R.equals('valvonta'), R.head),
      R.compose(valvontaCrumb(i18n), R.tail)
    ],
    [R.T, R.always([])]
  ])(location)
);

export const valvontaCrumb = R.curry((i18n, [type, ...rest]) =>
  R.cond([
    [R.equals('oikeellisuus'), oikeellisuusCrumb(i18n, [...rest])],
    [R.T, R.always([])]
  ])(type)
);

export const oikeellisuusCrumb = R.curry((i18n, [version, id], _) =>
  R.cond([
    [
      R.equals('all'),
      R.always([
        {
          url: '#/valvonta/oikeellisuus/all',
          label: i18n('navigation.valvonta.oikeellisuus')
        }
      ])
    ],
    [
      R.T,
      R.always([
        energiatodistukset(i18n),
        {
          url: `#/energiatodistus/${version}/${id}`,
          label: `${i18n('navigation.et')} ${id}`
        },
        {
          url: `#/valvonta/oikeellisuus/${version}/${id}`,
          label: `${i18n('navigation.valvonta.valvonta')}`
        }
      ])
    ]
  ])(version)
);

export const etKetjuCrumb = R.curry((i18n, etId) => [
  energiatodistukset(i18n),
  {
    url: `#/energiatodistus/${etId}`,
    label: `${i18n('navigation.et')} ${etId}`
  }
]);

export const viestiCrumb = R.curry((i18n, idTranslate, [id, ...rest]) =>
  R.cond([
    [
      R.equals('all'),
      R.always([{ url: '#/viesti/all', label: i18n('navigation.viesti') }])
    ],
    [
      R.equals('new'),
      R.always([
        { url: '#/viesti/all', label: i18n('navigation.viesti') },
        { url: '#/viesti/new', label: i18n('navigation.uusi-viesti') }
      ])
    ],
    [
      R.T,
      R.always(
        R.ifElse(
          R.hasPath(['viesti', parseInt(id, 10)]),
          R.compose(
            ketju => [
              { url: '#/viesti/all', label: i18n('navigation.viesti') },
              ...R.compose(
                Maybe.orSome([]),
                R.map(R.compose(Array.of, etKetjuCrumb(i18n))),
                R.prop('energiatodistus-id')
              )(ketju),
              {
                url: `#/viesti/${ketju.id}`,
                label: ketju.subject
              }
            ],
            R.path(['viesti', parseInt(id, 10)])
          ),
          R.always([
            { url: '#/viesti/all', label: i18n('navigation.viesti') },
            {
              url: `#/viesti/${id}`,
              label: `${i18n('navigation.viestiketju')} ${id}`
            }
          ])
        )(idTranslate)
      )
    ]
  ])(id)
);

export const energiatodistukset = i18n => ({
  url: '#/energiatodistus/all',
  label: i18n('navigation.energiatodistukset')
});

export const energiatodistusExtra = R.curry((i18n, [versio, id, extra]) =>
  R.equals('liitteet', extra)
    ? [
        {
          url: `#/energiatodistus/${versio}/${id}/liitteet`,
          label: i18n('navigation.liitteet')
        }
      ]
    : []
);

export const newEnergiatodistusCrumb = R.curry((i18n, versio) => [
  energiatodistukset(i18n),
  {
    url: `#/energiatodistus/${versio}/new`,
    label: i18n('navigation.uusi-energiatodistus')
  }
]);

export const energiatodistusCrumb = R.curry((i18n, [versio, id, ...rest]) =>
  R.cond([
    [R.always(R.equals('all', versio)), R.always([energiatodistukset(i18n)])],
    [R.equals('new'), R.always(newEnergiatodistusCrumb(i18n, versio))],
    [
      R.T,
      R.always(
        R.flatten([
          energiatodistukset(i18n),
          {
            url: `#/energiatodistus/${versio}/${id}`,
            label: `${i18n('navigation.et')} ${id}`
          },
          energiatodistusExtra(i18n, [versio, id, ...rest])
        ])
      )
    ]
  ])(id)
);

export const laatijat = i18n => ({
  url: '#/laatija/all',
  label: i18n('navigation.laatijat')
});

export const laatijaExtra = R.curry((i18n, [id, extra]) =>
  R.cond([
    [
      R.equals('yritykset'),
      R.always([
        {
          url: `#/laatija/${id}/yritykset`,
          label: i18n('navigation.yritykset')
        }
      ])
    ],
    [R.T, R.always([])]
  ])(extra)
);

export const laatijatForSelfRole = R.curry((i18n, whoami) =>
  Kayttajat.isPaakayttaja(whoami) || Kayttajat.isPatevyydentoteaja(whoami)
    ? laatijat(i18n)
    : []
);

export const laatijaCrumb = R.curry(
  (i18n, idTranslate, whoami, [id, ...rest]) =>
    R.cond([
      [R.equals('laatijoidentuonti'), R.always([])],
      [R.equals('all'), R.always([laatijat(i18n)])],
      [
        R.T,
        id =>
          R.ifElse(
            R.hasPath(['kayttaja', parseInt(id, 10)]),
            R.compose(
              kayttaja =>
                R.flatten([
                  laatijatForSelfRole(i18n, whoami),
                  kayttajaNimiCrumb(i18n, whoami, kayttaja),
                  laatijaExtra(i18n, [id, ...rest])
                ]),
              R.path(['kayttaja', parseInt(id, 10)])
            ),
            R.always(
              R.flatten([
                laatijatForSelfRole(i18n, whoami),
                kayttajaIdCrumb(i18n, whoami, id),
                laatijaExtra(i18n, [id, ...rest])
              ])
            )
          )(idTranslate)
      ]
    ])(id)
);

export const laatijatForTargetRole = R.curry((i18n, whoami, kayttaja) =>
  R.either(Kayttajat.isPaakayttaja, Kayttajat.isPatevyydentoteaja)(whoami) &&
  Kayttajat.isLaatija(kayttaja)
    ? laatijat(i18n)
    : []
);

export const kayttajaIdCrumb = R.curry((i18n, whoami, id) => [
  R.equals(whoami.id, parseInt(id, 10))
    ? {
        url: `#/kayttaja/${id}`,
        label: i18n('navigation.omattiedot')
      }
    : {
        url: `#/kayttaja/${id}`,
        label: `${i18n('navigation.kayttaja')} ${id}`
      }
]);

export const kayttajaNimiCrumb = R.curry((i18n, whoami, kayttaja) =>
  R.eqProps('id', whoami, kayttaja)
    ? {
        url: `#/kayttaja/${kayttaja.id}`,
        label: i18n('navigation.omattiedot')
      }
    : {
        url: `#/kayttaja/${kayttaja.id}`,
        label: `${kayttaja.etunimi} ${kayttaja.sukunimi}`
      }
);

export const kayttajaCrumb = R.curry(
  (i18n, idTranslate, whoami, [id, ...rest]) =>
    R.cond([
      [R.equals('all'), R.always([laatijat(i18n)])],
      [
        R.T,
        id =>
          R.ifElse(
            R.hasPath(['kayttaja', parseInt(id, 10)]),
            R.compose(
              kayttaja =>
                R.flatten([
                  laatijatForTargetRole(i18n, whoami, kayttaja),
                  kayttajaNimiCrumb(i18n, whoami, kayttaja)
                ]),
              R.path(['kayttaja', parseInt(id, 10)])
            ),
            R.always(R.flatten([kayttajaIdCrumb(i18n, whoami, id)]))
          )(idTranslate)
      ]
    ])(id)
);

export const yritykset = R.curry((i18n, whoami) => ({
  url: Kayttajat.isPaakayttaja(whoami)
    ? '#/yritys/all'
    : `#/laatija/${whoami.id}/yritykset`,
  label: i18n('navigation.yritykset')
}));

export const yritysExtra = R.curry((i18n, [id, extra]) =>
  R.equals('laatijat', extra)
    ? [
        {
          url: `#/yritys/${id}/laatijat`,
          label: i18n('navigation.yritys-laatijat')
        }
      ]
    : []
);

export const yritysIdCrumb = R.curry((i18n, whoami, id, ...rest) =>
  R.flatten([
    yritykset(i18n, whoami),
    { url: `#/yritys/${id}`, label: `${i18n('navigation.yritys')} ${id}` },
    yritysExtra(i18n, [id, ...rest])
  ])
);

export const yritysNimiCrumb = R.curry((i18n, whoami, nimi, id, ...rest) =>
  R.flatten([
    yritykset(i18n, whoami),
    { url: `#/yritys/${id}`, label: nimi },
    yritysExtra(i18n, [id, ...rest])
  ])
);

export const newYritysCrumb = R.curry((i18n, whoami) => [
  yritykset(i18n, whoami),
  { url: '#/yritys/new', label: i18n('navigation.new-yritys') }
]);

export const yritysCrumb = R.curry((i18n, idTranslate, whoami, [id, ...rest]) =>
  R.cond([
    [R.equals('all'), R.always([yritykset(i18n, whoami)])],
    [
      R.equals('new'),
      R.always([
        ...(Kayttajat.isLaatija(whoami)
          ? [
              {
                url: `#/kayttaja/${whoami.id}`,
                label: i18n('navigation.omattiedot')
              }
            ]
          : []),
        ...newYritysCrumb(i18n, whoami)
      ])
    ],
    [
      R.T,
      id => [
        ...(Kayttajat.isLaatija(whoami)
          ? [
              {
                url: `#/kayttaja/${whoami.id}`,
                label: i18n('navigation.omattiedot')
              }
            ]
          : []),
        ...R.ifElse(
          R.hasPath(['yritys', parseInt(id, 10)]),
          R.compose(
            yritys => yritysNimiCrumb(i18n, whoami, yritys, id, ...rest),
            R.path(['yritys', parseInt(id, 10)])
          ),
          R.always(yritysIdCrumb(i18n, whoami, id, ...rest))
        )(idTranslate)
      ]
    ]
  ])(id)
);

export const rootCrumb = R.curry((i18n, whoami) =>
  R.cond([
    [
      R.either(Kayttajat.isLaatija, Kayttajat.isPaakayttaja),
      R.always({
        url: '#/energiatodistus/all',
        label: i18n('navigation.etusivu')
      })
    ],
    [
      Kayttajat.isPatevyydentoteaja,
      R.always({
        url: '#/laatija/laatijoidentuonti',
        label: i18n('navigation.etusivu')
      })
    ]
  ])(whoami)
);
