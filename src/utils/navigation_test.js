import { assert } from 'chai';
import * as R from 'ramda';

import * as Navigation from './navigation';

describe('Navigation', () => {
  const i18n = R.prop(R.__, {
    'navigation.et': 'ET',
    'navigation.viestit': 'Viestit',
    'navigation.liitteet': 'Liitteet',
    'navigation.muutoshistoria': 'Muutoshistoria',
    'navigation.energiatodistukset': 'Energiatodistukset',
    'navigation.yritykset': 'Yritykset',
    'navigation.omattiedot': 'Omat tiedot',
    'navigation.laatijoidentuonti': 'Laatijoiden tuonti',
    'navigation.laatijat': 'Laatijat',
    'navigation.tyojono': 'Työjono',
    'navigation.kaytonvalvonta': 'Käytönvalvonta',
    'navigation.halytykset': 'Hälytykset',
    'navigation.kayttajat': 'Käyttäjät'
  });

  describe('parseEnergiatodistus', () => {
    const kayttaja = { rooli: 0, id: 2, laatija: 3 };

    it('should return energiatodistus-links when within energiatodistus', () => {
      const expected = [
        { label: 'ET 1', href: '#/energiatodistus/2018/1' },
        { label: 'Viestit', href: '#/energiatodistus/2018/1/viestit' },
        { label: 'Liitteet', href: '#/energiatodistus/2018/1/liitteet' },
        {
          label: 'Muutoshistoria',
          href: '#/energiatodistus/2018/1/muutoshistoria'
        }
      ];

      assert.deepEqual(
        Navigation.parseEnergiatodistus(i18n, kayttaja, [
          '2018',
          '1',
          'allekirjoitus'
        ]),
        expected
      );
    });

    it('should return root links when outside single energiatodistus', () => {
      const expected = [
        {
          label: 'Energiatodistukset',
          href: '#/energiatodistus/all'
        },
        { label: 'Viestit', href: '#/viestit' },
        {
          label: 'Yritykset',
          href: `#/laatija/3/yritykset`
        },
        {
          label: 'Omat tiedot',
          href: '#/myinfo',
          activePath: `#/kayttaja/2`
        }
      ];

      assert.deepEqual(
        Navigation.parseEnergiatodistus(i18n, kayttaja, []),
        expected
      );
    });

    it('should return root links when creating new energiatodistus', () => {
      const expected = [
        {
          label: 'Energiatodistukset',
          href: '#/energiatodistus/all'
        },
        { label: 'Viestit', href: '#/viestit' },
        {
          label: 'Yritykset',
          href: `#/laatija/3/yritykset`
        },
        {
          label: 'Omat tiedot',
          href: '#/myinfo',
          activePath: `#/kayttaja/2`
        }
      ];

      assert.deepEqual(
        Navigation.parseEnergiatodistus(i18n, kayttaja, ['2018', 'new']),
        expected
      );
    });
  });

  describe('parseRoot', () => {
    it('should return links for laatija', () => {
      const kayttaja = { rooli: 0, id: 1, laatija: 2 };
      const expected = [
        {
          label: 'Energiatodistukset',
          href: '#/energiatodistus/all'
        },
        { label: 'Viestit', href: '#/viestit' },
        {
          label: 'Yritykset',
          href: `#/laatija/2/yritykset`
        },
        {
          label: 'Omat tiedot',
          href: '#/myinfo',
          activePath: `#/kayttaja/1`
        }
      ];

      assert.deepEqual(
        Navigation.parseEnergiatodistus(i18n, kayttaja, []),
        expected
      );
    });

    it('should return links for pätevyydentoteaja', () => {
      const kayttaja = { rooli: 1, id: 1 };
      const expected = [
        {
          label: 'Laatijoiden tuonti',
          href: '#/laatija/laatijoidentuonti'
        },
        { label: 'Laatijat', href: '#/laatija/all' },
        {
          label: 'Omat tiedot',
          href: '#/myinfo',
          activePath: `#/kayttaja/1`
        }
      ];

      assert.deepEqual(
        Navigation.parseEnergiatodistus(i18n, kayttaja, []),
        expected
      );
    });

    it('should return links for pääkäyttäjä', () => {
      const kayttaja = { rooli: 2, id: 1 };
      const expected = [
        { label: 'Työjono', href: '#/tyojono' },
        { label: 'Käytönvalvonta', href: '#/kaytonvalvonta' },
        { label: 'Hälytykset', href: '#/halytykset' },
        { label: 'Käyttäjät', href: '#/kayttaja/all' },
        { label: 'Yritykset', href: '#/yritys/all' },
        { label: 'Viestit', href: '#/viestit' },
        {
          label: 'Omat tiedot',
          href: '#/myinfo',
          activePath: `#/kayttaja/1`
        }
      ];

      assert.deepEqual(
        Navigation.parseEnergiatodistus(i18n, kayttaja, []),
        expected
      );
    });
  });
});
