import { expect, describe, it } from '@jest/globals';
import * as Future from '@Utility/future-utils';
import * as api from './kayttaja-api';
import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';

const aineistot = [
  {
    id: 1,
    'label-fi': 'Pankit',
    'label-sv': 'Banker',
    valid: true
  },
  {
    id: 2,
    'label-fi': 'Tilastokeskus',
    'label-sv': 'Statistikcentralen',
    valid: true
  },
  {
    id: 3,
    'label-fi': 'Anonymisoitu',
    'label-sv': 'Anonymiserad',
    valid: true
  }
];

describe('Kayttaja-api-suite: ', () => {
  describe('kayttajaForId', () => {
    it('should return proper url for given id', () => {
      const id = 1234;
      const expected = 'api/private/kayttajat/1234';

      expect(api.url.id(id)).toEqual(expected);
    });
  });

  describe('laatijaForId', () => {
    it('should return proper url for given id', () => {
      const id = 1234;
      const expected = 'api/private/kayttajat/1234/laatija';

      expect(api.url.laatija(id)).toEqual(expected);
    });
  });

  describe('laatijaFuture', () => {
    it('should return proper laatija', done => {
      const expected = {
        id: 1,
        nimi: 'Laatija'
      };

      const response = {
        status: 200,
        ok: true,
        json: () => new Promise((resolve, _) => resolve(expected))
      };

      const fetch = url => new Promise((resolve, _) => resolve(response));

      Future.fork(
        _ => {},
        laatija => {
          expect(R.pick(['id', 'nimi'], laatija)).toEqual(expected);
          done();
        },
        api.getLaatijaById(fetch, 1234)
      );
    });

    it('should fail when rejected', done => {
      const response = {
        status: 400,
        ok: false
      };

      const fetch = url => new Promise((_, reject) => reject(response));

      Future.fork(
        reject => {
          expect(400).toEqual(reject.status);
          done();
        },
        _ => {},
        api.getLaatijaById(fetch, 1234)
      );
    });
  });

  describe('Käyttäjän aineistot', () => {
    it('Should serialize so that aineisto with a None valid-until is omitted', done => {
      const localModel = [
        {
          'aineisto-id': 1,
          'valid-until': Either.Right(
            Maybe.Some(new Date('2023-12-09T09:41:37.509347Z'))
          ),
          'ip-address': '127.0.0.1'
        },
        {
          'aineisto-id': 2,
          'valid-until': Either.Right(
            Maybe.Some(new Date('2023-12-16T12:22:04.391373Z'))
          ),
          'ip-address': '127.0.0.1'
        },
        {
          'aineisto-id': 3,
          'valid-until': Either.Right(Maybe.None()),
          'ip-address': ''
        }
      ];

      const apiModel = api.serializeKayttajaAineistot(localModel);

      const expected = [
        {
          'aineisto-id': 1,
          'valid-until': '2023-12-09T09:41:37.509Z',
          'ip-address': '127.0.0.1'
        },
        {
          'aineisto-id': 2,
          'valid-until': '2023-12-16T12:22:04.391Z',
          'ip-address': '127.0.0.1'
        }
      ];

      expect(apiModel).toEqual(expected);

      done();
    });
  });
});
