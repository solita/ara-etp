import * as R from 'ramda';
import * as Future from '@Utility/future-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as Kayttajat from '@Utility/kayttajat';

import * as yritysApi from '@Component/Yritys/yritys-api';
import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';
import * as viestiApi from '@Component/viesti/viesti-api';

const withFallback = R.curry((fallback, future) => ({
  fallback,
  future: Future.chainRej(R.always(Future.resolve(fallback)), future)
}));

const withSelfFallback = R.converge(withFallback, [R.identity, Future.resolve]);

export const parseLocation = R.curry(
  (i18n, idTranslate, store, location, whoami) =>
    R.compose(
      R.evolve({
        fallback: R.compose(R.uniq, R.prepend(rootCrumb(i18n, whoami))),
        future: R.map(R.compose(R.uniq, R.prepend(rootCrumb(i18n, whoami))))
      }),
      parseCrumb(i18n, idTranslate, store, whoami),
      R.split('/'),
      R.tail
    )(location)
);

export const parseCrumb = R.curry(
  (i18n, idTranslate, store, whoami, location) =>
    R.cond([
      [
        R.compose(R.equals('yritys'), R.head),
        R.compose(yritysCrumb(i18n, idTranslate, store, whoami), R.tail)
      ],
      [
        R.compose(R.equals('kayttaja'), R.head),
        R.compose(kayttajaCrumb(i18n, idTranslate, store, whoami), R.tail)
      ],
      [
        R.compose(R.equals('laatija'), R.head),
        R.compose(laatijaCrumb(i18n, idTranslate, store, whoami), R.tail)
      ],
      [
        R.compose(R.equals('energiatodistus'), R.head),
        R.compose(energiatodistusCrumb(i18n), R.tail)
      ],
      [
        R.compose(R.equals('viesti'), R.head),
        R.compose(viestiCrumb(i18n, idTranslate, store), R.tail)
      ],
      [R.T, R.always(withSelfFallback([]))]
    ])(location)
);

export const etKetjuCrumb = R.curry((i18n, etId) => ({
  url: `#/energiatodistus/${etId}`,
  label: `${i18n('navigation.et')} ${etId}`
}));

export const viestiCrumbFuture = R.curry((i18n, store, id) =>
  R.compose(
    R.map(ketju =>
      R.flatten([
        { url: '#/viesti/all', label: i18n('navigation.viesti') },
        R.compose(
          R.map(etKetjuCrumb(i18n)),
          Array.of,
          R.prop('energiatodistus-id')
        )(ketju),
        {
          url: `#/viesti/${ketju.id}`,
          label: `${i18n('navigation.viestiketju')} ${ketju.id}`
        }
      ])
    ),
    R.map(R.tap(store.updateKetju)),
    R.chain(viestiApi.ketju),
    Future.after(500)
  )(id)
);

export const viestiCrumb = R.curry((i18n, idTranslate, store, [id, ...rest]) =>
  R.cond([
    [
      R.equals('all'),
      R.always(
        withSelfFallback([
          { url: '#/viesti/all', label: i18n('navigation.viesti') }
        ])
      )
    ],
    [
      R.equals('new'),
      R.always(
        withSelfFallback([
          { url: '#/viesti/all', label: i18n('navigation.viesti') },
          { url: '#/viesti/new', label: i18n('navigation.uusi-viesti') }
        ])
      )
    ],
    [
      R.T,
      R.always(
        withFallback(
          [
            { url: '#/viesti/all', label: i18n('navigation.viesti') },
            {
              url: `#/viesti/${id}`,
              label: `${i18n('navigation.viestiketju')} ${id}`
            }
          ],
          R.ifElse(
            R.hasPath(['viesti', parseInt(id, 10)]),
            R.compose(
              Future.resolve,
              Maybe.orSome([
                { url: '#/viesti/all', label: i18n('navigation.viesti') },
                {
                  url: `#/viesti/${id}`,
                  label: `${i18n('navigation.viestiketju')} ${id}`
                }
              ]),
              R.map(etId => [
                { url: '#/viesti/all', label: i18n('navigation.viesti') },
                etKetjuCrumb(i18n, etId),
                {
                  url: `#/viesti/${id}`,
                  label: `${i18n('navigation.viestiketju')} ${id}`
                }
              ]),
              R.path(['viesti', parseInt(id, 10)])
            ),
            R.always(viestiCrumbFuture(i18n, store, id))
          )(idTranslate)
        )
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

export const energiatodistusCrumb = R.curry((i18n, [versio, id, ...rest]) =>
  R.cond([
    [R.equals('all'), R.always(withSelfFallback([energiatodistukset(i18n)]))],
    [
      R.T,
      R.always(
        withSelfFallback(
          R.flatten([
            energiatodistukset(i18n),
            {
              url: `#/energiatodistus/${versio}/${id}`,
              label: `${i18n('navigation.et')} ${id}`
            },
            energiatodistusExtra(i18n, [versio, id, ...rest])
          ])
        )
      )
    ]
  ])(versio)
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

export const laatijaCrumbFuture = R.curry((i18n, store, whoami, id, ...rest) =>
  R.compose(
    R.map(kayttaja =>
      R.flatten([
        laatijatForSelfRole(i18n, whoami),
        kayttajaNimiCrumb(i18n, whoami, kayttaja),
        laatijaExtra(i18n, [id, ...rest])
      ])
    ),
    R.map(R.tap(store.updateKayttaja)),
    R.chain(kayttajaApi.getKayttajaById),
    Future.after(500)
  )(id)
);

export const laatijaCrumb = R.curry(
  (i18n, idTranslate, store, whoami, [id, ...rest]) =>
    R.cond([
      [R.equals('all'), R.always(withSelfFallback([laatijat(i18n)]))],
      [
        R.T,
        id =>
          withFallback(
            kayttajaIdCrumb(i18n, whoami, id),
            R.ifElse(
              R.hasPath(['kayttaja', parseInt(id, 10)]),
              R.compose(
                R.map(kayttaja =>
                  R.flatten([
                    laatijatForSelfRole(i18n, whoami),
                    kayttajaNimiCrumb(i18n, whoami, kayttaja),
                    laatijaExtra(i18n, [id, ...rest])
                  ])
                ),
                Future.resolve,
                R.path(['kayttaja', parseInt(id, 10)])
              ),
              R.always(laatijaCrumbFuture(i18n, store, whoami, id, ...rest))
            )(idTranslate)
          )
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

export const kayttajaCrumbFuture = R.curry((i18n, store, whoami, id) =>
  R.compose(
    R.map(kayttaja =>
      R.flatten([
        laatijatForTargetRole(i18n, whoami, kayttaja),
        kayttajaNimiCrumb(i18n, whoami, kayttaja)
      ])
    ),
    R.map(R.tap(store.updateKayttaja)),
    R.chain(kayttajaApi.getKayttajaById),
    Future.after(500)
  )(id)
);

export const kayttajaCrumb = R.curry(
  (i18n, idTranslate, store, whoami, [id, ...rest]) =>
    R.cond([
      [R.equals('all'), R.always(withSelfFallback([laatijat(i18n)]))],
      [
        R.T,
        id =>
          withFallback(
            kayttajaIdCrumb(i18n, whoami, id),
            R.ifElse(
              R.hasPath(['kayttaja', parseInt(id, 10)]),
              R.compose(
                R.map(kayttaja =>
                  R.flatten([
                    laatijatForTargetRole(i18n, whoami, kayttaja),
                    kayttajaNimiCrumb(i18n, whoami, kayttaja)
                  ])
                ),
                Future.resolve,
                R.path(['kayttaja', parseInt(id, 10)])
              ),
              R.always(kayttajaCrumbFuture(i18n, store, whoami, id))
            )(idTranslate)
          )
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

export const yritysCrumbFuture = R.curry((i18n, store, whoami, id, ...rest) =>
  R.compose(
    R.map(nimi => yritysNimiCrumb(i18n, whoami, nimi, id, ...rest)),
    R.map(R.prop('nimi')),
    R.map(R.tap(store.updateYritys)),
    R.chain(yritysApi.getYritysById),
    Future.after(500)
  )(id)
);

export const newYritysCrumb = R.curry((i18n, whoami) => [
  yritykset(i18n, whoami),
  { url: '#/yritys/new', label: i18n('navigation.new-yritys') }
]);

export const yritysCrumb = R.curry(
  (i18n, idTranslate, store, whoami, [id, ...rest]) =>
    R.cond([
      [R.equals('all'), R.always(withSelfFallback([yritykset(i18n, whoami)]))],
      [
        R.equals('new'),
        R.always(withSelfFallback(newYritysCrumb(i18n, whoami)))
      ],
      [
        R.T,
        id =>
          withFallback(
            yritysIdCrumb(i18n, whoami, id, ...rest),
            R.ifElse(
              R.hasPath(['yritys', parseInt(id, 10)]),
              R.compose(
                R.map(nimi => yritysNimiCrumb(i18n, whoami, nimi, id, ...rest)),
                Future.resolve,
                R.path(['yritys', parseInt(id, 10)])
              ),
              R.compose(
                R.apply(yritysCrumbFuture(i18n, store, whoami)),
                R.always([id, ...rest])
              )
            )(idTranslate)
          )
      ]
    ])(id)
);

export const rootCrumb = R.curry((i18n, whoami) =>
  R.cond([
    [
      R.either(Kayttajat.isLaatija, Kayttajat.isPaakayttaja),
      R.always(energiatodistukset(i18n))
    ],
    [
      Kayttajat.isPatevyydentoteaja,
      R.always({
        url: '#/laatija/laatijoidentuonti',
        label: i18n('navigation.laatijoidentuonti')
      })
    ]
  ])(whoami)
);
