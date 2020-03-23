import { expect } from 'chai';
import * as UserUtils from './user-utils';
import * as Future from './future-utils';

describe('UserUtils-suite: ', () => {
  describe('urlForUserId', () => {
    it('should return proper url for given id', () => {
      const id = 1234;
      const expected = '/api/private/kayttajat/1234';

      expect(UserUtils.urlForUserId(id)).to.eql(expected);
    });

    it('should return proper url for current', () => {
      const id = 'current';
      const expected = '/api/private/kayttajat/current';

      expect(UserUtils.urlForUserId(id)).to.eql(expected);
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
