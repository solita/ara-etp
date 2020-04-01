import { assert } from 'chai';
import * as Future from '@Utility/future-utils';
import * as KayttajaUtils from './kayttaja-utils';

describe('KayttajaUtils-suite: ', () => {
  describe('kayttajaForId', () => {
    it('should return proper url for given id', () => {
      const id = 1234;
      const expected = '/api/private/kayttajat/1234';

      assert.equal(expected, KayttajaUtils.kayttajaForId(id));
    });
  });

  describe('laatijaForKayttajaId', () => {
    it('should return proper url for given id', () => {
      const id = 1234;
      const expected = '/api/private/kayttajat/1234/laatija';

      assert.equal(expected, KayttajaUtils.laatijaForKayttajaId(id));
    });
  });

  describe('kayttajaFuture', () => {
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
        KayttajaUtils.kayttajaFuture(fetch, 1234)
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
        KayttajaUtils.kayttajaFuture(fetch, 1234)
      );
    });
  });

  describe('kayttajanLaatijaFuture', () => {
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
          assert.deepEqual(expected, laatija);
          done();
        },
        KayttajaUtils.kayttajanLaatijaFuture(fetch, 1234)
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
        KayttajaUtils.kayttajanLaatijaFuture(fetch, 1234)
      );
    });
  });

  describe('kayttajaAndLaatijaFuture', () => {
    it('should return proper kayttaja and laatija', done => {
      const expectedKayttaja = {
        id: 1,
        nimi: 'Paavo Pesusieni'
      };

      const expectedLaatija = {
        id: 1,
        nimi: 'Laatija'
      };

      const expected = { ...expectedKayttaja, laatija: expectedLaatija };

      const kayttajaResponse = {
        status: 200,
        ok: true,
        json: () => new Promise((resolve, _) => resolve(expectedKayttaja))
      };

      const laatijaResponse = {
        status: 200,
        ok: true,
        json: () => new Promise((resolve, _) => resolve(expectedLaatija))
      };

      const fetch = url => {
        if (url === '/api/private/kayttajat/1234') {
          return new Promise((resolve, _) => resolve(kayttajaResponse));
        }
        return new Promise((resolve, _) => resolve(laatijaResponse));
      };

      Future.fork(
        _ => {},
        kayttajaAndLaatija => {
          assert.deepEqual(expected, kayttajaAndLaatija);
          done();
        },
        KayttajaUtils.kayttajaAndLaatijaFuture(fetch, 1234)
      );
    });

    it('should reject when kayttaja rejects', done => {
      const expectedLaatija = {
        id: 1,
        nimi: 'Laatija'
      };

      const kayttajaResponse = {
        status: 404,
        ok: false
      };

      const laatijaResponse = {
        status: 200,
        ok: true,
        json: () => new Promise((resolve, _) => resolve(expectedLaatija))
      };

      const fetch = url => {
        if (url === '/api/private/kayttajat/1234') {
          return new Promise((_, reject) => reject(kayttajaResponse));
        }
        return new Promise((resolve, _) => resolve(laatijaResponse));
      };

      Future.fork(
        reject => {
          assert.equal(404, reject.status);
          done();
        },
        _ => {},
        KayttajaUtils.kayttajaAndLaatijaFuture(fetch, 1234)
      );
    });

    it('should reject when laatija rejects', done => {
      const expectedKayttaja = {
        id: 1,
        nimi: 'Paavo Pesusieni'
      };

      const kayttajaResponse = {
        status: 200,
        ok: true,
        json: () => new Promise((resolve, _) => resolve(expectedKayttaja))
      };

      const laatijaResponse = {
        status: 404,
        ok: false
      };

      const fetch = url => {
        if (url === '/api/private/kayttajat/1234') {
          return new Promise((resolve, _) => resolve(kayttajaResponse));
        }
        return new Promise((_, reject) => reject(laatijaResponse));
      };

      Future.fork(
        reject => {
          assert.equal(404, reject.status);
          done();
        },
        _ => {},
        KayttajaUtils.kayttajaAndLaatijaFuture(fetch, 1234)
      );
    });
  });
});
