import { assert } from 'chai';
import * as PaginationUtils from './pagination-utils';

describe('PaginationUtils:', () => {
  describe('nearForCurrent', () => {
    it('should return previous, current and next', () => {
      const pageCount = 10;
      const current = 5;
      const expected = [4, 5, 6];

      assert.deepEqual(
        expected,
        PaginationUtils.nearForCurrent(pageCount, current)
      );
    });

    it('should return next for first page', () => {
      const pageCount = 10;
      const current = 1;
      const expected = [2];

      assert.deepEqual(
        expected,
        PaginationUtils.nearForCurrent(pageCount, current)
      );
    });

    it('should return previous of last page', () => {
      const pageCount = 10;
      const current = 10;
      const expected = [9];

      assert.deepEqual(
        expected,
        PaginationUtils.nearForCurrent(pageCount, current)
      );
    });
  });

  describe('isPairWithCurrenPage', () => {
    it('should return true with current page as first element', () => {
      const current = 2;
      const pair = [2, 3];

      assert.equal(true, PaginationUtils.isPairWithCurrentPage(current, pair));
    });
    it('should return true with current page as last element', () => {
      const current = 3;
      const pair = [2, 3];

      assert.equal(true, PaginationUtils.isPairWithCurrentPage(current, pair));
    });

    it('should return false without current page in given pair', () => {
      const current = 4;
      const pair = [2, 3];

      assert.equal(false, PaginationUtils.isPairWithCurrentPage(current, pair));
    });
  });

  describe('combinePairs', () => {
    it('should combine given pairs and remove duplicates', () => {
      const pairs = [
        [1, 2],
        [2, 3]
      ];
      const expected = [1, 2, 3];

      assert.deepEqual(expected, PaginationUtils.combinePairs(pairs));
    });

    it('should return empty with empty input', () => {
      const pairs = [];
      const expected = [];

      assert.deepEqual(expected, PaginationUtils.combinePairs(pairs));
    });
  });

  describe('consecutivePairsForPageCount', () => {
    it('should return consecutive pairs with proper range', () => {
      const pageCount = 5;
      const expected = [
        [1, 2],
        [2, 3],
        [3, 4],
        [4, 5]
      ];

      assert.deepEqual(
        expected,
        PaginationUtils.consecutivePairsForPageCount(pageCount)
      );
    });

    it('should return empty list with pageCount of 1', () => {
      const pageCount = 1;
      const expected = [];

      assert.deepEqual(
        expected,
        PaginationUtils.consecutivePairsForPageCount(pageCount)
      );
    });
  });

  describe('dropFirstAndLastPages', () => {
    it('should not drop anything when not needed', () => {
      const pages = [5, 6, 7];
      const pageCount = 10;
      const expected = [5, 6, 7];

      assert.deepEqual(
        expected,
        PaginationUtils.dropFirstAndLastPages(pageCount, pages)
      );
    });
    it('should drop first if needed', () => {
      const pages = [1, 2, 3];
      const pageCount = 10;
      const expected = [2, 3];

      assert.deepEqual(
        expected,
        PaginationUtils.dropFirstAndLastPages(pageCount, pages)
      );
    });
    it('should drop last if needed', () => {
      const pages = [8, 9, 10];
      const pageCount = 10;
      const expected = [8, 9];

      assert.deepEqual(
        expected,
        PaginationUtils.dropFirstAndLastPages(pageCount, pages)
      );
    });

    it('should drop both first and last if needed', () => {
      const pages = [1, 2, 3];
      const pageCount = 3;
      const expected = [2];

      assert.deepEqual(
        expected,
        PaginationUtils.dropFirstAndLastPages(pageCount, pages)
      );
    });
  });

  describe('truncate', () => {
    it('should not truncate with little amount of pages', () => {
      const numberOfPages = 3;
      const page = 0;

      const expected = [0, 1, 2];

      assert.deepEqual(PaginationUtils.truncate(numberOfPages, page), expected);
    });

    it('should truncate with enough pages', () => {
      const numberOfPages = 21;
      const page = 0;

      const expected = [0, 1, 19, 20];
      assert.deepEqual(PaginationUtils.truncate(numberOfPages, page), expected);
    });

    it('should return near pages for current with enough pages', () => {
      const numberOfPages = 21;
      const page = 10;

      const expected = [0, 1, 9, 10, 11, 19, 20];
      assert.deepEqual(PaginationUtils.truncate(numberOfPages, page), expected);
    });
  });
});
