import { assert } from 'chai';
import * as R from 'ramda';

import * as Navigation from './navigation';

describe('Navigation', () => {
  const i18n = R.prop(R.__, {
    'navigation.et': 'ET',
    'navigation.uusi-energiatodistus': 'Uusi energiatodistus',
    'navigation.viestit': 'Viestit',
    'navigation.liitteet': 'Liitteet',
    'navigation.muutoshistoria': 'Muutoshistoria',
    'navigation.energiatodistukset': 'Energiatodistukset',
    'navigation.yritykset': 'Yritykset',
    'navigation.omattiedot': 'Omat tiedot',
    'navigation.laatijoidentuonti': 'Laatijoiden tuonti',
    'navigation.laatijat': 'Laatijat',
    'navigation.tyojono': 'Työjono',
    'navigation.halytykset': 'Hälytykset',
    'navigation.kayttajat': 'Käyttäjät',
    'navigation.valvonta.oikeellisuus.all': 'Oikeellisuuden valvonta',
    'navigation.valvonta.kaytto.all': 'Käytönvalvonta',
  });

  describe('locationParts', () => {
    it('should return parts', () => {
      const location = '/yritys/new';
      const expected = ['yritys', 'new'];

      assert.deepEqual(Navigation.locationParts(location), expected);
    });
  });

  describe('parseEnergiatodistus', () => {
    const kayttaja = { rooli: 0, id: 2 };

    it('should return energiatodistus-links when within energiatodistus', () => {
      const expected = [
        { label: 'ET 1', href: '#/energiatodistus/2018/1' },
        // Hidden until implemented
        //{ label: 'Viestit', href: '#/energiatodistus/2018/1/viestit' },
        { label: 'Liitteet', href: '#/energiatodistus/2018/1/liitteet' }
        // Hidden until implemented
        //{
        //  label: 'Muutoshistoria',
        //  href: '#/energiatodistus/2018/1/muutoshistoria'
        //}
      ];

      assert.deepEqual(
        Navigation.parseEnergiatodistus(false, i18n, kayttaja, [
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
        {
          label: 'Yritykset',
          href: `#/laatija/2/yritykset`
        },
        { label: 'Viestit', href: '#/viesti/all' }
      ];

      assert.deepEqual(
        R.map(
          R.dissoc('badge'),
          Navigation.parseEnergiatodistus(false, i18n, kayttaja, [])
        ),
        expected
      );
    });

    it('should return empty links when creating new energiatodistus', () => {
      const expected = [];

      assert.deepEqual(
        Navigation.parseEnergiatodistus(false, i18n, kayttaja, ['2018', 'new']),
        expected
      );
    });
  });

  describe('parseRoot', () => {
    it('should return links for laatija', () => {
      const kayttaja = { rooli: 0, id: 1 };
      const expected = [
        {
          label: 'Energiatodistukset',
          href: '#/energiatodistus/all'
        },
        {
          label: 'Yritykset',
          href: `#/laatija/1/yritykset`
        },
        { label: 'Viestit', href: '#/viesti/all' }
      ];

      assert.deepEqual(
        R.map(
          R.dissoc('badge'),
          Navigation.parseEnergiatodistus(false, i18n, kayttaja, [])
        ),
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
        { label: 'Laatijat', href: '#/laatija/all' }
      ];

      assert.deepEqual(
        Navigation.parseEnergiatodistus(false, i18n, kayttaja, []),
        expected
      );
    });

    it('should return links for pääkäyttäjä', () => {
      const kayttaja = { rooli: 2, id: 1 };
      const expected = [
        { label: 'Energiatodistukset', href: '#/energiatodistus/all' },
        { label: 'Laatijat', href: '#/laatija/all' },
        {
          label: 'Oikeellisuuden valvonta',
          href: `#/valvonta/oikeellisuus/all?valvoja-id=${kayttaja.id}&has-valvoja=false`
        },
        { label: 'Viestit', href: '#/viesti/all' }
      ];

      assert.deepEqual(
        R.map(
          R.dissoc('badge'),
          Navigation.parseEnergiatodistus(false, i18n, kayttaja, [])
        ),
        expected
      );
    });
  });
});
