import { assert } from 'chai';
import * as R from 'ramda';
import * as YritysUtils from './yritys-utils';
import * as Maybe from '../../utils/maybe-utils';
import * as Future from '../../utils/future-utils';

describe('YritysUtils:', () => {
  describe('urlForYritysId', () => {
    it('should return proper url for given id', () => {
      assert.equal('/api/yritykset/1', YritysUtils.urlForYritysId(1));
    });
  });

  describe('deserialize', () => {
    it('should wrap verkkolaskuosoite and wwwosoite into Somes', () => {
      const yritys = { verkkolaskuosoite: '12345', wwwosoite: '54321' };
      const deserializedYritys = YritysUtils.deserialize(yritys);

      assert.equal(
        '12345',
        Maybe.getOrElse('', deserializedYritys.verkkolaskuosoite)
      );
      assert.equal('54321', Maybe.getOrElse('', deserializedYritys.wwwosoite));
    });

    it('should wrap null verkkolaskuosoite and wwwosoite into Nones', () => {
      const yritys = { verkkolaskuosoite: null, wwwosoite: null };
      const deserializedYritys = YritysUtils.deserialize(yritys);

      assert.equal(
        '',
        Maybe.getOrElse('', deserializedYritys.verkkolaskuosoite)
      );
      assert.equal('', Maybe.getOrElse('', deserializedYritys.wwwosoite));
    });
  });

  describe('serialize', () => {
    it('should unwrap Some verkkolaskuosoite and wwwosoite into raw values', () => {
      const yritys = {
        verkkolaskuosoite: Maybe.of('12345'),
        wwwosoite: Maybe.of('54321')
      };
      const serializedYritys = YritysUtils.serialize(yritys);

      assert.equal('12345', serializedYritys.verkkolaskuosoite);
      assert.equal('54321', serializedYritys.wwwosoite);
    });

    it('should unwrap None verkkolaskuosoite and wwwosoite into nulls', () => {
      const yritys = {
        verkkolaskuosoite: Maybe.None(),
        wwwosoite: Maybe.None()
      };
      const serializedYritys = YritysUtils.serialize(yritys);

      assert.equal(null, serializedYritys.verkkolaskuosoite);
      assert.equal(null, serializedYritys.wwwosoite);
    });

    it('should remove id-property', () => {
      const yritys = { id: 1 };
      const serializedYritys = YritysUtils.serialize(yritys);

      assert.notProperty(serializedYritys, 'id');
    });
  });

  describe('validateYritys', () => {
    it('should run given validations for yritys', () => {
      const validators = {
        nimi: item => item.length > 0,
        wwwosoite: item => item.length > 0,
        verkkolaskuosoite: item => item.length > 0
      };

      const yritys = {
        nimi: '',
        wwwosoite: Maybe.Some('12345'),
        verkkolaskuosoite: Maybe.Some('54321')
      };

      const expected = {
        nimi: false,
        wwwosoite: true,
        verkkolaskuosoite: true
      };

      assert.deepEqual(
        expected,
        YritysUtils.validateYritys(validators, yritys)
      );
    });
    it('should run validate true for Nones', () => {
      const validators = {
        nimi: item => item.length > 0,
        wwwosoite: item => item.length > 0,
        verkkolaskuosoite: item => item.length > 0
      };

      const yritys = {
        nimi: '',
        wwwosoite: Maybe.None(),
        verkkolaskuosoite: Maybe.None()
      };

      const expected = {
        nimi: false,
        wwwosoite: true,
        verkkolaskuosoite: true
      };

      assert.deepEqual(
        expected,
        YritysUtils.validateYritys(validators, yritys)
      );
    });
  });

  describe('getYritysIdByFuture', () => {
    it('should call right on succesful request', done => {
      const expected = {
        id: 1,
        nimi: 'test',
        wwwosoite: Maybe.None(),
        verkkolaskuosoite: Maybe.Some('12345')
      };

      const response = {
        status: 200,
        ok: true,
        json: () =>
          new Promise((resolve, _) =>
            resolve({
              id: 1,
              nimi: 'test',
              verkkolaskuosoite: '12345',
              wwwosoite: null
            })
          )
      };

      const fetch = R.curry(
        (url, options) => new Promise((resolve, _) => resolve(response))
      );

      Future.fork(
        _ => {},
        value => {
          assert.deepEqual(expected, value);
          done();
        },
        YritysUtils.getYritysByIdFuture(fetch, 1)
      );
    });

    it('should call left on failed request', done => {
      const response = {
        status: 404,
        ok: false
      };

      const fetch = R.curry(
        (url, options) => new Promise((_, reject) => reject(response))
      );

      Future.fork(
        reject => {
          assert.equal(404, reject.status);
          done();
        },
        _ => {},
        YritysUtils.getYritysByIdFuture(fetch, 1)
      );
    });
  });

  describe('putYritysByIdFuture', () => {
    it('should call right on succesful request', done => {
      const expected = {
        status: 200,
        ok: true
      };

      const fetch = R.curry(
        (url, options) => new Promise((resolve, _) => resolve(expected))
      );

      Future.fork(
        _ => {},
        response => {
          assert.deepEqual(expected, response);
          done();
        },
        YritysUtils.putYritysByIdFuture(fetch, 1, {})
      );
    });

    it('should call left on failed request', done => {
      const response = {
        status: 404,
        ok: false
      };

      const fetch = R.curry(
        (url, options) => new Promise((_, reject) => reject(response))
      );

      Future.fork(
        response => {
          assert.equal(404, response.status);
          done();
        },
        _ => {},
        YritysUtils.putYritysByIdFuture(fetch, 1, {})
      );
    });
  });

  describe('postYritysFuture', () => {
    it('should call right on succesful request', done => {
      const expected = {
        id: 1
      };

      const response = {
        status: 200,
        ok: true,
        json: () => new Promise((resolve, _) => resolve({ id: 1 }))
      };

      const fetch = R.curry(
        (url, options) => new Promise((resolve, _) => resolve(response))
      );

      Future.fork(
        _ => {},
        value => {
          assert.deepEqual(expected, value);
          done();
        },
        YritysUtils.postYritysFuture(fetch, {})
      );
    });

    it('should call left on failed request', done => {
      const response = {
        status: 404,
        ok: false,
        json: () => new Promise((resolve, _) => resolve({ id: 1 }))
      };

      const fetch = R.curry(
        (url, options) => new Promise((_, reject) => reject(response))
      );

      Future.fork(
        reject => {
          assert.equal(404, reject.status);
          done();
        },
        _ => {},
        YritysUtils.postYritysFuture(fetch, {})
      );
    });
  });
});
