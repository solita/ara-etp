import { expect, describe, it } from '@jest/globals';
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
      expect('/api/private/yritykset/1').toEqual(api.url.yritys(1));
    });
  });

  describe('deserialize', () => {
    it('should wrap verkkolaskuosoite into Some', () => {
      const yritys = { verkkolaskuosoite: '003712345671' };
      const deserializedYritys = api.deserialize(yritys);

      expect('003712345671').toEqual(
        Maybe.getOrElse('', deserializedYritys.verkkolaskuosoite)
      );
    });

    it('should wrap null verkkolaskuosoite into None', () => {
      const yritys = { verkkolaskuosoite: null };
      const deserializedYritys = api.deserialize(yritys);

      expect('').toEqual(
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

      expect('003712345671').toEqual(serializedYritys.verkkolaskuosoite);
    });

    it('should unwrap None verkkolaskuosoite into null', () => {
      const yritys = {
        verkkolaskuosoite: Maybe.None()
      };
      const serializedYritys = api.serialize(yritys);

      expect(null).toEqual(serializedYritys.verkkolaskuosoite);
    });

    it('should remove id-property', () => {
      const yritys = { id: 1 };
      const serializedYritys = api.serialize(yritys);

      expect('id' in serializedYritys).toBeFalsy();
    });
  });

  describe('validateYritys', () => {
    it('should run given validations for yritys', () => {
      const schema = YritysUtils.schema(Either.Right('FI'));

      const yritys = {
        nimi: '',
        verkkolaskuosoite: Maybe.Some('003712345671')
      };

      const expected = {
        nimi: false,
        verkkolaskuosoite: true
      };

      expect(
        R.map(Either.isRight, validation.validateModelObject(schema, yritys))
      ).toEqual(expected);
    });
    it('should run validate true for Nones', () => {
      const schema = YritysUtils.schema(Either.Right('FI'));

      const yritys = {
        nimi: '',
        verkkolaskuosoite: Maybe.None()
      };

      const expected = {
        nimi: false,
        verkkolaskuosoite: true
      };

      expect(
        R.map(Either.isRight, validation.validateModelObject(schema, yritys))
      ).toEqual(expected);
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
          expect(expected).toEqual(response);
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
          expect(404).toEqual(response.status);
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
          expect(expected).toEqual(value);
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
          expect(404).toEqual(reject.status);
          done();
        },
        _ => {},
        api.postYritys(fetch, {})
      );
    });
  });
});
