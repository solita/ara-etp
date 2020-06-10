import { assert } from 'chai';
import * as R from 'ramda';
import * as YritysUtils from './yritys-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';
import * as Future from '@Utility/future-utils';
import * as validation from '@Utility/validation';

describe('YritysUtils:', () => {
  describe('urlForYritysId', () => {
    it('should return proper url for given id', () => {
      assert.equal('/api/private/yritykset/1', YritysUtils.urlForYritysId(1));
    });
  });

  describe('deserialize', () => {
    it('should wrap verkkolaskuosoite into Some', () => {
      const yritys = { verkkolaskuosoite: '12345' };
      const deserializedYritys = YritysUtils.deserialize(yritys);

      assert.equal(
        '12345',
        Maybe.getOrElse('', deserializedYritys.verkkolaskuosoite)
      );
    });

    it('should wrap null verkkolaskuosoite into None', () => {
      const yritys = { verkkolaskuosoite: null };
      const deserializedYritys = YritysUtils.deserialize(yritys);

      assert.equal(
        '',
        Maybe.getOrElse('', deserializedYritys.verkkolaskuosoite)
      );
    });
  });

  describe('serialize', () => {
    it('should unwrap Some verkkolaskuosoite into raw value', () => {
      const yritys = {
        verkkolaskuosoite: Maybe.of('12345')
      };
      const serializedYritys = YritysUtils.serialize(yritys);

      assert.equal('12345', serializedYritys.verkkolaskuosoite);
    });

    it('should unwrap None verkkolaskuosoite into null', () => {
      const yritys = {
        verkkolaskuosoite: Maybe.None(),
      };
      const serializedYritys = YritysUtils.serialize(yritys);

      assert.equal(null, serializedYritys.verkkolaskuosoite);
    });

    it('should remove id-property', () => {
      const yritys = { id: 1 };
      const serializedYritys = YritysUtils.serialize(yritys);

      assert.notProperty(serializedYritys, 'id');
    });
  });

  describe('validateYritys', () => {
    it('should run given validations for yritys', () => {
      const schema = YritysUtils.formSchema();

      const yritys = {
        nimi: '',
        verkkolaskuosoite: Maybe.Some('54321')
      };

      const expected = {
        nimi: false,
        verkkolaskuosoite: true
      };

      assert.deepEqual(
        R.map(Either.isRight, validation.validateModelObject(schema, yritys)),
        expected
      );
    });
    it('should run validate true for Nones', () => {
      const schema = YritysUtils.formSchema();

      const yritys = {
        nimi: '',
        verkkolaskuosoite: Maybe.None()
      };

      const expected = {
        nimi: false,
        verkkolaskuosoite: true
      };

      assert.deepEqual(
        R.map(Either.isRight, validation.validateModelObject(schema, yritys)),
        expected
      );
    });
  });

  describe('getYritysIdByFuture', () => {
    it('should call right on succesful request', done => {
      const expected = {
        id: 1,
        nimi: 'test',
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
              verkkolaskuosoite: '12345'
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
