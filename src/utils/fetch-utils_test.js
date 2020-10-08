import { expect } from 'chai';
import * as Fetch from './fetch-utils';
import * as Future from './future-utils';

describe('FetchUtils', () => {
  describe('toJson', () => {
    it('should call right side of fork with resolve', done => {
      const expected = {
        a: true
      };

      const response = {
        json: () => new Promise((resolve, _) => resolve(expected))
      };

      Future.fork(
        () => {},
        value => {
          expect(value).to.eql(expected);
          done();
        },
        Fetch.toJson(response)
      );
    });

    it('should call left side of fork with reject', done => {
      const expected = {
        a: true
      };

      const response = {
        json: () => new Promise((_, reject) => reject(expected))
      };

      Future.fork(
        value => {
          expect(value).to.eql(expected);
          done();
        },
        () => {},
        Fetch.toJson(response)
      );
    });

    it('should return call left side of fork with throw', done => {
      const expected = {
        a: true
      };

      const response = {
        json: () =>
          new Promise(_ => {
            throw expected;
          })
      };

      Future.fork(
        value => {
          expect(value).to.eql(expected);
          done();
        },
        () => {},
        Fetch.toJson(response)
      );
    });
  });

  describe('rejectWithInvalidResponse', () => {
    it('should resolve itself when ok', done => {
      const response = {
        ok: true
      };

      const expected = response;

      Future.fork(
        () => {},
        response => {
          expect(response).to.eql(expected);
          done();
        },
        Fetch.rejectWithInvalidResponse(response)
      );
    });
  });
});
