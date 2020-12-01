import { assert } from 'chai';
import * as R from 'ramda';
import * as BreadcrumbUtils from './breadcrumb-utils';
import * as Maybe from '@Utility/maybe-utils';

describe('BreadcrumbUtils', () => {
  describe('translateReservedKeywordLabel', () => {
    const i18n = R.prop(R.__, {
      a: '`a',
      b: '`b',
      d: '`d',
      e: '`e',
      f: '`f'
    });

    it('should translate only reserved keyword', () => {
      const crumbs = [
        { url: 'a', label: 'a' },
        { url: 'b', label: 'b' },
        { url: 'd', label: 'd' },
        { url: 'e', label: 'e' },
        { url: 'f', label: 'f' },
        { url: 'g', label: 'g' }
      ];

      const expected = [
        { url: 'a', label: '`a' },
        { url: 'b', label: '`b' },
        { url: 'd', label: '`d' },
        { url: 'e', label: '`e' },
        { url: 'f', label: '`f' },
        { url: 'g', label: 'g' }
      ];

      R.compose(
        result => assert.deepEqual(result, expected),
        R.map(R.apply(BreadcrumbUtils.translateReservedKeywordLabel(i18n))),
        R.zip([
          'all',
          'new',
          'liitteet',
          'viestit',
          'muutoshistoria',
          'notreserved'
        ])
      )(crumbs);
    });
  });

  describe('yritysCrumb', () => {
    const idTranslate = {
      yritys: { 1: 'eka' }
    };

    const i18n = R.prop(R.__, { 'yritys.yritys': 'Yritys' });

    it('should return proper breadcrumb for translatable id', () => {
      const expected = { url: '#/yritys/1', label: 'eka' };

      assert.deepEqual(
        BreadcrumbUtils.yritysCrumb(idTranslate, i18n, 1),
        expected
      );
    });

    it('should return proper breadcrumb for nontranslatable id', () => {
      const expected = { url: '#/yritys/2', label: 'Yritys 2' };

      assert.deepEqual(
        BreadcrumbUtils.yritysCrumb(idTranslate, i18n, 2),
        expected
      );
    });
  });

  describe('laatijanYrityksetCrumb', () => {
    const i18n = R.prop(R.__, { 'navigation.yritykset': 'Yritykset' });
    const user = { id: 1 };
    it('should return laatijanYritykset crumb', () => {
      const expected = { url: '#/laatija/1/yritykset', label: 'Yritykset' };

      assert.deepEqual(
        BreadcrumbUtils.yrityksetCrumb(i18n, user),
        expected
      );
    });
  });

  describe('parseYritys', () => {
    const user = { id: 1 };

    const i18n = R.prop(R.__, {
      'navigation.yritykset': 'Yritykset',
      'yritys.yritys': 'Yritys',
      'yritys.uusi-yritys': 'Uusi yritys'
    });

    const idTranslate = { yritys: { new: 'yritys.uusi-yritys' } };

    it('should return proper crumb for new yritys', () => {
      const locationParts = ['new'];

      const expected = [
        { label: 'Yritykset', url: '#/laatija/1/yritykset' },
        {
          label: `Uusi yritys`,
          url: `#/yritys/new`
        }
      ];

      assert.deepEqual(
        BreadcrumbUtils.parseYritys(idTranslate, i18n, user, locationParts),
        expected
      );
    });

    it('should return proper label for existing yritys', () => {
      const locationParts = ['1'];

      const expected = [
        { label: 'Yritykset', url: '#/laatija/1/yritykset' },
        {
          label: `Yritys 1`,
          url: `#/yritys/1`
        }
      ];

      assert.deepEqual(
        BreadcrumbUtils.parseYritys(idTranslate, i18n, user, locationParts),
        expected
      );
    });
  });

  describe('energiatodistusRootActionCrumb', () => {
    const i18n = R.prop(R.__, {
      'navigation.energiatodistukset': 'Energiatodistukset'
    });

    it('should return proper root actioncrumb', () => {
      const expected = {
        url: '#/energiatodistus/all',
        label: 'Energiatodistukset'
      };

      assert.deepEqual(
        BreadcrumbUtils.energiatodistusRootActionCrumb(i18n, 'all'),
        expected
      );
    });
  });

  describe('singleEnergiatodistusCrumb', () => {
    const idTranslate = { new: 'Uusi energiatodistus' };
    const i18n = R.prop(R.__, { 'navigation.et': 'Energiatodistus' });
    it('should return proper crumb for single energiatodistus', () => {
      const expected = {
        url: '#/energiatodistus/2018/1',
        label: 'Energiatodistus 1'
      };

      assert.deepEqual(
        BreadcrumbUtils.singleEnergiatodistusCrumb(
          idTranslate,
          i18n,
          '2018',
          '1'
        ),
        expected
      );
    });
  });

  describe('singleEnergiatodistusActionCrumb', () => {
    const i18n = R.prop(R.__, {
      'navigation.liitteet': 'Liitteet',
      'navigation.viestit': 'Viestit',
      'navigation.muutoshistoria': 'Muutoshistoria'
    });

    it('should return proper breadcrumb for single energiatodistus action', () => {
      const keywords = [
        'liitteet',
        'viestit',
        'muutoshistoria'
      ];
      const version = '2018';
      const id = '1';

      const expected = [
        { url: '#/energiatodistus/2018/1/liitteet', label: 'Liitteet' },
        { url: '#/energiatodistus/2018/1/viestit', label: 'Viestit' },
        {
          url: '#/energiatodistus/2018/1/muutoshistoria',
          label: 'Muutoshistoria'
        }
      ];

      R.compose(
        result => assert.deepEqual(result, expected),
        R.map(
          BreadcrumbUtils.singleEnergiatodistusActionCrumb(i18n, version, id)
        )
      )(keywords);
    });
  });

  describe('parseEnergiatodistus', () => {
    const idTranslate = { new: 'Uusi energiatodistus' };

    const i18n = R.prop(R.__, {
      'navigation.energiatodistukset': 'Energiatodistukset',
      'navigation.et': 'Energiatodistus',
      'navigation.liitteet': 'Liitteet',
      'navigation.viestit': 'Viestit',
      'navigation.muutoshistoria': 'Muutoshistoria'
    });

    it('should return root-level breadcrumb', () => {
      const expected = [
        {
          url: '#/energiatodistus/all',
          label: 'Energiatodistukset'
        }
      ];

      assert.deepEqual(
        BreadcrumbUtils.parseEnergiatodistus(idTranslate, i18n, ['all']),
        expected
      );
    });

    it('should return single-et-level breadcrumb', () => {
      const expected = [
        {
          url: '#/energiatodistus/all',
          label: 'Energiatodistukset'
        },
        {
          url: '#/energiatodistus/2018/1',
          label: 'Energiatodistus 1'
        }
      ];

      assert.deepEqual(
        BreadcrumbUtils.parseEnergiatodistus(idTranslate, i18n, ['2018', '1']),
        expected
      );
    });

    it('should return single-et-action-level breadcrumbs', () => {
      const expected = [
        {
          url: '#/energiatodistus/all',
          label: 'Energiatodistukset'
        },
        {
          url: '#/energiatodistus/2018/1',
          label: 'Energiatodistus 1'
        },
        {
          url: '#/energiatodistus/2018/1/liitteet',
          label: 'Liitteet'
        }
      ];

      assert.deepEqual(
        BreadcrumbUtils.parseEnergiatodistus(idTranslate, i18n, [
          '2018',
          '1',
          'liitteet'
        ]),
        expected
      );
    });
  });

  describe('isCurrentUser', () => {
    const user = { id: 1 };
    it('should return true when id matches current user', () => {
      assert.isTrue(BreadcrumbUtils.isCurrentUser('1', user));
    });

    it('should return false when id does not match current user', () => {
      assert.isFalse(BreadcrumbUtils.isCurrentUser('2', user));
    });
  });

  describe('parseKayttaja', () => {
    const idTranslate = {
      kayttaja: { 3: 'Testi Käyttäjä' }
    };

    const i18n = R.prop(R.__, {
      'navigation.omattiedot': 'Omat tiedot',
      'navigation.kayttajat': 'Käyttäjät',
      'navigation.kayttaja': 'Käyttäjä'
    });

    const user = { id: 1 };

    it('should return omat tiedot for current user', () => {
      const expected = { url: '#/kayttaja/1', label: 'Omat tiedot' };

      assert.deepEqual(
        BreadcrumbUtils.parseKayttaja(idTranslate, i18n, user, ['1']),
        expected
      );
    });

    it('should return breadcrumb for some other user', () => {
      const expected = [
        { url: '#/kayttaja/all', label: 'Käyttäjät' },
        { url: '#/kayttaja/2', label: 'Käyttäjä 2' }
      ];

      assert.deepEqual(
        BreadcrumbUtils.parseKayttaja(idTranslate, i18n, user, ['2']),
        expected
      );
    });

    it('should return breadcrumb for known other user', () => {
      const expected = [
        { url: '#/kayttaja/all', label: 'Käyttäjät' },
        { url: '#/kayttaja/3', label: 'Testi Käyttäjä' }
      ];

      assert.deepEqual(
        BreadcrumbUtils.parseKayttaja(idTranslate, i18n, user, ['3']),
        expected
      );
    });
  });

  describe('parseLaatijaRootActionCrumb', () => {
    const keywords = ['all', 'laatijoidentuonti'];

    const i18n = R.prop(R.__, {
      'navigation.laatijat': 'Laatijat',
      'navigation.laatijoidentuonti': 'Laatijoiden tuonti'
    });

    it('should return proper root-level crumbs for laatija', () => {
      const expected = [
        { url: '#/laatija/all', label: 'Laatijat' },
        { url: '#/laatija/laatijoidentuonti', label: 'Laatijoiden tuonti' }
      ];

      R.compose(
        results => assert.deepEqual(results, expected),
        R.map(BreadcrumbUtils.parseLaatijaRootActionCrumb(i18n))
      )(keywords);
    });
  });

  describe('parseSingleLaatijaActionCrumb', () => {
    const i18n = R.prop(R.__, {
      'navigation.yritykset': 'Yritykset'
    });

    it('should return proper single-laatija-action-level crumb', () => {
      const expected = { url: '#/laatija/1/yritykset', label: 'Yritykset' };

      assert.deepEqual(
        BreadcrumbUtils.parseSingleLaatijaActionCrumb(i18n, 1, 'yritykset'),
        expected
      );
    });
  });

  describe('parseLaatija', () => {
    const i18n = R.prop(R.__, {
      'navigation.laatijat': 'Laatijat',
      'navigation.laatijoidentuonti': 'Laatijoiden tuonti',
      'navigation.yritykset': 'Yritykset'
    });

    it('should return root-level crumb', () => {
      const expected = { url: '#/laatija/all', label: 'Laatijat' };

      assert.deepEqual(
        BreadcrumbUtils.parseLaatijaRootActionCrumb(i18n, 'all'),
        expected
      );
    });

    it('should return single-laatija-action-level crumb', () => {
      const expected = { url: '#/laatija/1/yritykset', label: 'Yritykset' };

      assert.deepEqual(
        BreadcrumbUtils.parseSingleLaatijaActionCrumb(i18n, 1, 'yritykset'),
        expected
      );
    });
  });

  describe('parseAction', () => {
    const keywords = [
      'halytykset',
      'kaytonvalvonta',
      'tyojono',
      'viestit',
      'myinfo'
    ];

    const i18n = R.prop(R.__, {
      'navigation.halytykset': 'Hälytykset',
      'navigation.kaytonvalvonta': 'Käytönvalvonta',
      'navigation.tyojono': 'Työjono',
      'navigation.viestit': 'Viestit',
      'navigation.myinfo': 'Omat tiedot'
    });

    it('should return action crumb', () => {
      const expected = [
        { url: '#/halytykset', label: 'Hälytykset' },
        { url: '#/kaytonvalvonta', label: 'Käytönvalvonta' },
        { url: '#/tyojono', label: 'Työjono' },
        { url: '#/viestit', label: 'Viestit' },
        { url: '#/myinfo', label: 'Omat tiedot' }
      ];

      R.compose(
        results => assert.deepEqual(results, expected),
        R.map(BreadcrumbUtils.parseAction(i18n))
      )(keywords);
    });
  });
});
