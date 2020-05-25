import { assert } from 'chai';
import * as R from 'ramda';
import * as BreadcrumbUtils from './breadcrumb-utils';

describe.only('BreadcrumbUtils', () => {
  describe('locationParts', () => {
    it('should return parts', () => {
      const location = '/yritys/new';
      const expected = ['yritys', 'new'];

      assert.deepEqual(BreadcrumbUtils.locationParts(location), expected);
    });
  });

  describe('parseYritys', () => {
    const i18n = R.prop(R.__, {
      'yritys.yritys': 'Yritys',
      'yritys.uusi-yritys': 'Uusi yritys'
    });
    it('should return proper crumb for new yritys', () => {
      const locationParts = ['yritys', 'new'];

      const expected = {
        label: `Uusi yritys`,
        url: `#/yritys/new`
      };

      assert.deepEqual(
        BreadcrumbUtils.parseYritys(i18n, {}, locationParts),
        expected
      );
    });

    it('should return proper label for existing yritys', () => {
      const locationParts = ['yritys', '1'];

      const expected = {
        label: `Yritys 1`,
        url: `#/yritys/1`
      };

      assert.deepEqual(
        BreadcrumbUtils.parseYritys(i18n, {}, locationParts),
        expected
      );
    });
  });

  describe('breadcrumbParse', () => {
    const i18n = R.prop(R.__, {
      'yritys.yritys': 'Yritys',
      'yritys.uusi-yritys': 'Uusi yritys'
    });

    const user = {};

    it('should return locationcurried function for yritys', () => {
      const location = '/yritys/new';
      const expected = {
        label: `Uusi yritys`,
        url: `#/yritys/new`
      };

      assert.deepEqual(
        BreadcrumbUtils.breadcrumbParse(location, i18n, user),
        expected
      );
    });
  });
});
