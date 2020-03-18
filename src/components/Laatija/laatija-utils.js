import * as R from 'ramda';
import * as Future from '@Utility/future-utils';
import * as Fetch from '@Utility/fetch-utils';
import * as Either from '@Utility/either-utils';

export const laatijaApi = `api/private/laatijat`;

export const urlForLaatijaId = id => `${laatijaApi}/${id}`;

export const laatijaFuture = R.curry((fetch, id) =>
  R.compose(
    R.map(deserialize),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    urlForLaatijaId
  )(id)
);

export const deserialize = R.evolve({
  maa: Either.Right
});

export const serialize = R.compose(
  R.evolve({ maa: Either.right }),
  R.dissoc('id')
);

export const emptyLaatija = () =>
  deserialize({
    jakeluosoite: '',
    'muut-toimintaalueet': [0],
    'patevyys-voimassaoloaika': {
      start: '2020-03-17',
      end: '2020-03-17'
    },
    puhelin: '',
    sukunimi: '',
    maa: '',
    patevyys: 0,
    postitoimipaikka: '',
    postinumero: '',
    ensitallennus: true,
    'julkinen-email': true,
    email: '',
    'patevyys-voimassa': true,
    henkilotunnus: '',
    toimintaalue: 0,
    etunimi: '',
    'julkinen-puhelin': true
  });
