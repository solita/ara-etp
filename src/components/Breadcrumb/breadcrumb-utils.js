import * as R from 'ramda';
import * as Future from '@Utility/future-utils';
import * as Kayttajat from '@Utility/kayttajat';

import * as yritysApi from '@Component/Yritys/yritys-api';
import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';
import * as viestiApi from '@Component/viesti/viesti-api';

export const parseLocation = R.curry((i18n, location) =>
  R.chain(
    whoami =>
      R.map(
        R.compose(R.uniq, R.prepend(rootCrumb(i18n, whoami))),
        parseCrumb(i18n, whoami, R.compose(R.split('/'), R.tail)(location))
      ),
    kayttajaApi.whoami
  )
);

export const parseCrumb = R.curry((i18n, whoami, location) =>
  R.cond([
    [
      R.compose(R.equals('yritys'), R.head),
      R.compose(yritysCrumb(i18n, whoami), R.tail)
    ],
    [
      R.compose(R.equals('kayttaja'), R.head),
      R.compose(kayttajaCrumb(i18n, whoami), R.tail)
    ],
    [
      R.compose(R.equals('laatija'), R.head),
      R.compose(laatijaCrumb(i18n, whoami), R.tail)
    ],
    [
      R.compose(R.equals('energiatodistus'), R.head),
      R.compose(energiatodistusCrumb(i18n), R.tail)
    ],
    [
      R.compose(R.equals('viesti'), R.head),
      R.compose(viestiCrumb(i18n), R.tail)
    ],
    [R.T, R.always(Future.resolve([]))]
  ])(location)
);

export const viestiCrumb = R.curry((i18n, [id, ...rest]) =>
  R.cond([
    [
      R.equals('all'),
      R.always(
        Future.resolve([
          { url: '#/viesti/all', label: i18n('navigation.viesti') }
        ])
      )
    ],
    [
      R.equals('new'),
      R.always(
        Future.resolve([
          { url: '#/viesti/all', label: i18n('navigation.viesti') },
          { url: '#/viesti/new', label: i18n('navigation.uusi-viesti') }
        ])
      )
    ],
    [
      R.T,
      R.compose(
        R.map(ketju =>
          R.flatten([
            { url: '#/viesti/all', label: i18n('navigation.viesti') },
            ketju['energiatodistus-id']
              ? [
                  {
                    url: `#/energiatodistus/${ketju['energiatodistus-id']}`,
                    label: `${i18n('navigation.et')} ${
                      ketju['energiatodistus-id']
                    }`
                  }
                ]
              : [],
            {
              url: `#/viesti/${ketju.id}`,
              label: `${i18n('navigation.viestiketju')} ${ketju.id}`
            }
          ])
        ),
        viestiApi.ketju
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
    [R.equals('all'), R.always(Future.resolve([energiatodistukset(i18n)]))],
    [
      R.T,
      R.always(
        Future.resolve(
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

export const laatijaCrumb = R.curry((i18n, whoami, [id, ...rest]) =>
  R.cond([
    [R.equals('all'), R.always(Future.resolve([laatijat(i18n)]))],
    [
      R.T,
      R.compose(
        R.map(kayttaja =>
          R.flatten([
            Kayttajat.isPaakayttaja(whoami) ||
            Kayttajat.isPatevyydentoteaja(whoami)
              ? laatijat(i18n)
              : [],
            R.propEq('id', whoami, kayttaja)
              ? {
                  url: `#/kayttaja/${kayttaja.id}`,
                  label: i18n('navigation.omattiedot')
                }
              : {
                  url: `#/kayttaja/${kayttaja.id}`,
                  label: `${kayttaja.etunimi} ${kayttaja.sukunimi}`
                },
            laatijaExtra(i18n, [id, ...rest])
          ])
        ),
        kayttajaApi.getKayttajaById
      )
    ]
  ])(id)
);

export const kayttajaCrumb = R.curry((i18n, whoami, [id, ...rest]) =>
  R.cond([
    [R.equals('all'), R.always(Future.resolve([laatijat(i18n)]))],
    [
      R.T,
      R.compose(
        R.map(kayttaja =>
          R.flatten([
            R.either(
              Kayttajat.isPaakayttaja,
              Kayttajat.isPatevyydentoteaja
            )(whoami) && Kayttajat.isLaatija(kayttaja)
              ? laatijat(i18n)
              : [],
            R.equals(whoami.id, parseInt(id, 10))
              ? {
                  url: `#/kayttaja/${id}`,
                  label: i18n('navigation.omattiedot')
                }
              : {
                  url: `#/kayttaja/${kayttaja.id}`,
                  label: `${kayttaja.etunimi} ${kayttaja.sukunimi}`
                }
          ])
        ),
        kayttajaApi.getKayttajaById
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

export const yritysCrumb = R.curry((i18n, whoami, [id, ...rest]) =>
  R.cond([
    [R.equals('all'), R.always(Future.resolve([yritykset(i18n, whoami)]))],
    [
      R.equals('new'),
      R.always(
        Future.resolve([
          yritykset(i18n, whoami),
          { url: '#/yritys/new', label: i18n('navigation.new-yritys') }
        ])
      )
    ],
    [
      R.T,
      R.compose(
        R.map(yritys =>
          R.flatten([
            yritykset(i18n, whoami),
            { url: `#/yritys/${id}`, label: R.prop('nimi', yritys) },
            yritysExtra(i18n, [id, ...rest])
          ])
        ),
        yritysApi.getYritysById
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
