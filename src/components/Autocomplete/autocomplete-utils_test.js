import { assert } from 'chai';
import * as Maybe from '@Utility/maybe-utils';
import * as AutocompleteUtils from './autocomplete-utils';

describe('AutocompleteUtils:', () => {
  describe('selectedItem', () => {
    it('should return right item with given index', () => {
      const active = 0;
      const filteredItems = [1, 2, 3, 4, 5];
      const expected = Maybe.Some(1);

      assert.deepEqual(
        expected,
        AutocompleteUtils.selectedItem(filteredItems, active)
      );
    });

    it('should return Maybe.None when no item is found', () => {
      const active = 1;
      const filteredItems = [];
      const expected = Maybe.None();

      assert.deepEqual(
        expected,
        AutocompleteUtils.selectedItem(filteredItems, active)
      );
    });
  });

  describe('previousItem', () => {
    it('should return previous when given greater than 0', () => {
      const active = 1;
      const expected = Maybe.Some(0);

      assert.deepEqual(expected, AutocompleteUtils.previousItem(active));
    });

    it('should return Maybe.None when given 0', () => {
      const active = 0;
      const expected = Maybe.None();

      assert.deepEqual(expected, AutocompleteUtils.previousItem(active));
    });
  });

  describe('nextItem', () => {
    it('should return next index when given less than last index', () => {
      const active = 0;
      const filteredItems = [1, 2, 3, 4, 5];
      const expected = Maybe.Some(1);

      assert.deepEqual(
        expected,
        AutocompleteUtils.nextItem(filteredItems, active)
      );
    });

    it('should return last index when given last index', () => {
      const active = 4;
      const filteredItems = [1, 2, 3, 4, 5];
      const expected = Maybe.Some(4);

      assert.deepEqual(
        expected,
        AutocompleteUtils.nextItem(filteredItems, active)
      );
    });
  });
});
