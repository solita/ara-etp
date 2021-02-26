import { assert } from 'chai';
import * as Future from '@Utility/future-utils';
import * as api from './kayttaja-api';
import * as R from 'ramda';

describe('Kayttaja-api-suite: ', () => {
  describe('kayttajaForId', () => {
    it('should return proper url for given id', () => {
      const id = 1234;
      const expected = 'api/private/kayttajat/1234';

      assert.equal(api.url.id(id), expected);
    });
  });

  describe('laatijaForId', () => {
    it('should return proper url for given id', () => {
      const id = 1234;
      const expected = 'api/private/kayttajat/1234/laatija';

      assert.equal(api.url.laatija(id), expected);
    });
  });

  describe.skip('kayttajaFuture', () => {
    it('should return proper kayttaja', done => {
      const expected = {
        id: 1,
        nimi: 'Paavo Pesusieni'
      };

      const response = {
        status: 200,
        ok: true,
        json: () => new Promise((resolve, _) => resolve(expected))
      };

      const fetch = url => new Promise((resolve, _) => resolve(response));

      Future.fork(
        _ => {},
        kayttaja => {
          assert.deepEqual(expected, kayttaja);
          done();
        },
        api.getKayttajaById(fetch, 1234)
      );
    });

    it('should fail when rejected', done => {
      const response = {
        status: 404,
        ok: false
      };

      const fetch = url => new Promise((_, reject) => reject(response));

      Future.fork(
        reject => {
          assert.equal(404, reject.status);
          done();
        },
        _ => {},
        api.getKayttajaById(fetch, 1234)
      );
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
          assert.deepEqual(R.pick(['id', 'nimi'], laatija), expected);
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
          assert.equal(400, reject.status);
          done();
        },
        _ => {},
        api.getLaatijaById(fetch, 1234)
      );
    });
  });
});
