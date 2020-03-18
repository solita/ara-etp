import * as R from 'ramda';
import * as Future from '@Utility/future-utils';
import * as Fetch from '@Utility/fetch-utils';
import * as Either from '@Utility/either-utils';

import * as Validation from '@Utility/validation';

export const laatijaApi = `api/private/laatijat`;

export const urlForLaatijaId = id => `${laatijaApi}/${id}`;

export const formSchema = () => ({
  henkilotunnus: [],
  etunimi: [
    Validation.isRequired,
    Validation.minLengthConstraint(2),
    Validation.maxLengthConstraint(200)
  ],
  sukunimi: [
    Validation.isRequired,
    Validation.minLengthConstraint(2),
    Validation.maxLengthConstraint(200)
  ],
  email: [
    Validation.isRequired,
    Validation.minLengthConstraint(2),
    Validation.maxLengthConstraint(200)
  ],
  puhelin: [
    Validation.isRequired,
    Validation.minLengthConstraint(2),
    Validation.maxLengthConstraint(200)
  ],
  jakeluosoite: [
    Validation.isRequired,
    Validation.minLengthConstraint(2),
    Validation.maxLengthConstraint(200)
  ],
  postinumero: [Validation.isRequired, Validation.isPostinumero],
  postitoimipaikka: []
});

export const formParsers = () => ({
  henkilotunnus: R.trim,
  etunimi: R.trim,
  sukunimi: R.trim,
  email: R.trim,
  puhelin: R.trim,
  jakeluosoite: R.trim,
  postinumero: R.trim,
  postitoimipaikka: R.trim
});

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

export const getyLaatijaByIdFuture = R.curry((fetch, id) =>
  R.compose(
    R.map(deserialize),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    urlForLaatijaId
  )(id)
);

export const putLaatijaByIdFuture = R.curry((fetch, id, laatija) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', urlForLaatijaId(id))),
    serialize
  )(laatija)
);

export const postLaatijatFuture = R.curry((fetch, laatijat) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', laatijaApi)),
    R.map(serialize)
  )(laatijat)
);
