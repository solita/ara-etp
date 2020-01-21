import { expect } from 'chai';
import { exampleFn } from './example-utils';

describe('Example-Suite', () => {
  describe('Example-function test', () => {
    it('should return identity', () => {
      const input = 1;

      expect(exampleFn(input)).to.eql(input);
    });
  });
});