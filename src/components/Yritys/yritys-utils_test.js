import { assert } from 'chai';
import * as R from 'ramda';
import * as YritysUtils from './yritys-utils';
import * as api from './yritys-api';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';
import * as Future from '@Utility/future-utils';
import * as validation from '@Utility/validation';

describe('Yritys api and utils tests:', () => {
  describe('urlForYritysId', () => {
    it('should return proper url for given id', () => {
      assert.equal('/api/private/yritykset/1', api.url.yritys(1));
    });
  });

  describe('deserialize', () => {
    it('should wrap verkkolaskuosoite into Some', () => {
      const yritys = { verkkolaskuosoite: '003712345671' };
      const deserializedYritys = api.deserialize(yritys);

      assert.equal(
        '003712345671',
        Maybe.getOrElse('', deserializedYritys.verkkolaskuosoite)
      );
    });

    it('should wrap null verkkolaskuosoite into None', () => {
      const yritys = { verkkolaskuosoite: null };
      const deserializedYritys = api.deserialize(yritys);

      assert.equal(
        '',
        Maybe.getOrElse('', deserializedYritys.verkkolaskuosoite)
      );
    });
  });

  describe('serialize', () => {
    it('should unwrap Some verkkolaskuosoite into raw value', () => {
      const yritys = {
        verkkolaskuosoite: Maybe.of('003712345671')
      };
      const serializedYritys = api.serialize(yritys);

      assert.equal('003712345671', serializedYritys.verkkolaskuosoite);
    });

    it('should unwrap None verkkolaskuosoite into null', () => {
      const yritys = {
        verkkolaskuosoite: Maybe.None()
      };
      const serializedYritys = api.serialize(yritys);

      assert.equal(null, serializedYritys.verkkolaskuosoite);
    });

    it('should remove id-property', () => {
      const yritys = { id: 1 };
      const serializedYritys = api.serialize(yritys);

      assert.notProperty(serializedYritys, 'id');
    });
  });

  describe('validateYritys', () => {
    it('should run given validations for yritys', () => {
      const schema = YritysUtils.formSchema();

      const yritys = {
        nimi: '',
        verkkolaskuosoite: Maybe.Some('003712345671')
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

  describe.skip('getYritysIdByFuture', () => {
    it('should call right on succesful request', done => {
      const expected = {
        id: 1,
        nimi: 'test',
        verkkolaskuosoite: Maybe.Some('003712345671')
      };

      const response = {
        status: 200,
        ok: true,
        json: () =>
          new Promise((resolve, _) =>
            resolve({
              id: 1,
              nimi: 'test',
              verkkolaskuosoite: '003712345671'
            })
          )
      };

      const fetch = (url, options) =>
        new Promise((resolve, _) => resolve(response));

      Future.fork(
        _ => {},
        value => {
          assert.deepEqual(expected, value);
          done();
        },
        api.getYritysById(fetch, 1)
      );
    });

    it('should call left on failed request', done => {
      const response = {
        status: 404,
        ok: false
      };

      const fetch = (url, options) =>
        new Promise((_, reject) => reject(response));

      Future.fork(
        reject => {
          assert.equal(404, reject.status);
          done();
        },
        _ => {},
        api.getYritysById(fetch, 1)
      );
    });
  });

  describe('putYritysByIdFuture', () => {
    it('should call right on succesful request', done => {
      const expected = {
        status: 200,
        ok: true
      };

      const fetch = (url, options) =>
        new Promise((resolve, _) => resolve(expected));

      Future.fork(
        _ => {},
        response => {
          assert.deepEqual(expected, response);
          done();
        },
        api.putYritysById(fetch, 1, {})
      );
    });

    it('should call left on failed request', done => {
      const response = {
        status: 404,
        ok: false
      };

      const fetch = (url, options) =>
        new Promise((_, reject) => reject(response));

      Future.fork(
        response => {
          assert.equal(404, response.status);
          done();
        },
        _ => {},
        api.putYritysById(fetch, 1, {})
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

      const fetch = (url, options) =>
        new Promise((resolve, _) => resolve(response));

      Future.fork(
        _ => {},
        value => {
          assert.deepEqual(expected, value);
          done();
        },
        api.postYritys(fetch, {})
      );
    });

    it('should call left on failed request', done => {
      const response = {
        status: 404,
        ok: false,
        json: () => new Promise((resolve, _) => resolve({ id: 1 }))
      };

      const fetch = (url, options) =>
        new Promise((_, reject) => reject(response));

      Future.fork(
        reject => {
          assert.equal(404, reject.status);
          done();
        },
        _ => {},
        api.postYritys(fetch, {})
      );
    });
  });
});
