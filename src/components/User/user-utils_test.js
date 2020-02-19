import { expect } from 'chai';
import * as UserUtils from './user-utils';
import * as Future from '../../utils/future-utils';

describe('UserUtils-suite: ', () => {
  describe('urlForUserId', () => {
    it('should return proper url for given id', () => {
      const id = 1234;
      const expected = '/api/users/1234';

      expect(UserUtils.urlForUserId(id)).to.eql(expected);
    });

    it('should return proper url for current', () => {
      const id = 'current';
      const expected = '/api/users/current';

      expect(UserUtils.urlForUserId(id)).to.eql(expected);
    });
  });

  describe('responseAsJson', () => {
    it('should call right side of fork with resolve', done => {
      const expected = {
        a: true
      };

      const response = {
        json: () => new Promise((resolve, reject) => resolve(expected))
      };

      Future.fork(
        () => {},
        value => {
          expect(value).to.eql(expected);
          done();
        },
        UserUtils.responseAsJson(response)
      );
    });

    it('should call left side of fork with reject', done => {
      const expected = {
        a: true
      };

      const response = {
        json: () => new Promise((resolve, reject) => reject(expected))
      };

      Future.fork(
        value => {
          expect(value).to.eql(expected);
          done();
        },
        () => {},
        UserUtils.responseAsJson(response)
      );
    });

    it('should return call left side of fork with throw', done => {
      const expected = {
        a: true
      };

      const response = {
        json: () =>
          new Promise((resolve, reject) => {
            throw expected;
          })
      };

      Future.fork(
        value => {
          expect(value).to.eql(expected);
          done();
        },
        () => {},
        UserUtils.responseAsJson(response)
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
        UserUtils.rejectWithInvalidResponse(response)
      );
    });

    it('should reject itself when not ok', done => {
      const response = {
        ok: false
      };

      const expected = response;

      Future.fork(
        response => {
          expect(response).to.eql(expected);
          done();
        },
        () => {},
        UserUtils.rejectWithInvalidResponse(response)
      );
    });
  });

  describe('userFuture', () => {
    it('should call right with resolved response', done => {
      const expected = 'resolved value';
      const id = '1234';

      const response = {
        ok: true,
        json: () => new Promise((resolve, reject) => resolve(expected))
      };

      const fetch = _ => new Promise((resolve, reject) => resolve(response));

      Future.fork(
        () => {},
        value => {
          expect(value).to.eql(expected);
          done();
        },
        UserUtils.userFuture(fetch, id)
      );
    });

    it('should call left with rejected response', done => {
      const expected = 'resolved value';
      const id = '1234';

      const response = {
        ok: true,
        json: () => new Promise((resolve, reject) => resolve(id))
      };

      const fetch = _ => new Promise((resolve, reject) => reject(expected));

      Future.fork(
        value => {
          expect(value).to.eql(expected);
          done();
        },
        () => {},
        UserUtils.userFuture(fetch, id)
      );
    });
  });
});
