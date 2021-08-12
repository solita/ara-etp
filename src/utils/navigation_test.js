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
    'navigation.yritys': 'Yritys',
    'navigation.yritykset': 'Yritykset',
    'navigation.yritys-laatijat': 'Laatijat',
    'navigation.omattiedot': 'Omat tiedot',
    'navigation.laatijoidentuonti': 'Laatijoiden tuonti',
    'navigation.laatijat': 'Laatijat',
    'navigation.tyojono': 'Työjono',
    'navigation.halytykset': 'Hälytykset',
    'navigation.kayttajat': 'Käyttäjät',
    'navigation.valvonta.valvonta': 'Valvonta',
    'navigation.valvonta.oikeellisuus.all': 'Oikeellisuuden valvonta',
    'navigation.valvonta.oikeellisuus.valvonta': 'Valvonta',
    'navigation.valvonta.kaytto.all': 'Käytönvalvonta'
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

  describe('parseYritys', () => {
    it('should return root for all', () => {
      const isDev = false;
      const locationParts = ['all'];
      const whoami = { rooli: 0, id: 1 };
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
          Navigation.parseYritys(isDev, i18n, whoami, {}, locationParts)
        ),
        expected
      );
    });
    it('should return empty for new', () => {
      const isDev = false;
      const locationParts = ['new'];
      const whoami = { rooli: 0, id: 1 };
      const expected = [];

      assert.deepEqual(
        Navigation.parseYritys(isDev, i18n, whoami, {}, locationParts),
        expected
      );
    });
    it('should return yritys with id-label', () => {
      const isDev = false;
      const locationParts = ['1'];
      const whoami = { rooli: 0, id: 1 };
      const expected = [
        {
          label: 'Yritys 1',
          href: '#/yritys/1'
        },
        {
          label: 'Laatijat',
          href: `#/yritys/1/laatijat`
        }
      ];

      assert.deepEqual(
        R.map(
          R.dissoc('badge'),
          Navigation.parseYritys(isDev, i18n, whoami, {}, locationParts)
        ),
        expected
      );
    });
    it('should return yritys with name-label', () => {
      const isDev = false;
      const locationParts = ['1'];
      const whoami = { rooli: 0, id: 1 };
      const expected = [
        {
          label: 'Testiyritys',
          href: '#/yritys/1'
        },
        {
          label: 'Laatijat',
          href: `#/yritys/1/laatijat`
        }
      ];

      assert.deepEqual(
        R.map(
          R.dissoc('badge'),
          Navigation.parseYritys(
            isDev,
            i18n,
            whoami,
            { yritys: { 1: 'Testiyritys' } },
            locationParts
          )
        ),
        expected
      );
    });
  });

  describe('parseKayttaja', () => {
    const laatija = { id: 1, rooli: 0 };
    const patevyydentoteaja = { id: 1, rooli: 1 };
    const paakayttaja = { id: 1, rooli: 2 };
    const laskuttaja = { id: 1, rooli: 3 };

    it('should return root for laatijoidentuonti', () => {
      const isDev = false;
      const locationParts = ['laatijoidentuonti'];

      const expected = [
        {
          label: 'Laatijoiden tuonti',
          href: '#/laatija/laatijoidentuonti'
        },
        { label: 'Laatijat', href: '#/laatija/all' }
      ];

      assert.deepEqual(
        Navigation.parseKayttaja(
          isDev,
          patevyydentoteaja,
          i18n,
          {},
          locationParts
        ),
        expected
      );
    });

    it('should return root for all for patevyydentoteaja', () => {
      const isDev = false;
      const locationParts = ['all'];

      const expected = [
        {
          label: 'Laatijoiden tuonti',
          href: '#/laatija/laatijoidentuonti'
        },
        { label: 'Laatijat', href: '#/laatija/all' }
      ];

      assert.deepEqual(
        Navigation.parseKayttaja(
          isDev,
          patevyydentoteaja,
          i18n,
          {},
          locationParts
        ),
        expected
      );
    });

    it('should return laatija-nav for laatija', () => {
      const isDev = false;
      const locationParts = ['1'];

      const expected = [
        {
          label: 'Testi Laatija',
          href: '#/kayttaja/1'
        },
        { label: 'Yritykset', href: '#/laatija/1/yritykset' }
      ];

      assert.deepEqual(
        Navigation.parseKayttaja(
          isDev,
          paakayttaja,
          i18n,
          {
            kayttaja: {
              1: { id: 1, etunimi: 'Testi', sukunimi: 'Laatija', rooli: 0 }
            }
          },
          locationParts
        ),
        expected
      );
    });
    it('should return empty without translate', () => {
      const isDev = false;
      const locationParts = ['1'];

      const expected = [];

      assert.deepEqual(
        Navigation.parseKayttaja(
          isDev,
          paakayttaja,
          i18n,
          {
            kayttaja: {}
          },
          locationParts
        ),
        expected
      );
    });

    it('should return empty if locationParts contains yritykset', () => {
      const isDev = false;
      const locationParts = ['1', 'yritykset'];
      const expected = [];

      assert.deepEqual(
        Navigation.parseKayttaja(
          isDev,
          paakayttaja,
          i18n,
          {
            kayttaja: {}
          },
          locationParts
        ),
        expected
      );
    });
  });

  describe('parseValvonta', () => {
    it('should return root for all', () => {
      const isDev = false;
      const whoami = { id: 1, rooli: 2 };
      const locationParts = ['all'];

      const expected = [
        {
          label: 'Energiatodistukset',
          href: '#/energiatodistus/all'
        },
        {
          label: 'Laatijat',
          href: `#/laatija/all`
        },
        {
          label: 'Oikeellisuuden valvonta',
          href: `#/valvonta/oikeellisuus/all?valvoja-id=1&has-valvoja=false`
        },
        { label: 'Viestit', href: '#/viesti/all' }
      ];

      assert.deepEqual(
        R.map(
          R.dissoc('badge'),
          Navigation.parseValvontaOikeellisuus(
            isDev,
            i18n,
            whoami,
            locationParts
          )
        ),
        expected
      );
    });
    it('should return empty for long locations', () => {
      const isDev = false;
      const whoami = { id: 1, rooli: 2 };
      const locationParts = ['1', '2', '3'];

      const expected = [];

      assert.deepEqual(
        R.map(
          R.dissoc('badge'),
          Navigation.parseValvontaOikeellisuus(
            isDev,
            i18n,
            whoami,
            locationParts
          )
        ),
        expected
      );
    });
    it('should return energiatodistus for others', () => {
      const isDev = false;
      const whoami = { id: 1, rooli: 2 };
      const locationParts = ['2018', '1'];

      const expected = [
        { label: 'ET 1', href: '#/energiatodistus/2018/1' },
        { label: 'Liitteet', href: '#/energiatodistus/2018/1/liitteet' },
        { label: 'Valvonta', href: '#/valvonta/oikeellisuus/2018/1' }
      ];

      assert.deepEqual(
        Navigation.parseValvontaOikeellisuus(
          isDev,
          i18n,
          whoami,
          locationParts
        ),
        expected
      );
    });
  });

  describe('parseViesti', () => {
    it('should return root for all', () => {
      const isDev = false;
      const whoami = { id: 1, rooli: 2 };
      const locationParts = ['all'];

      const expected = [
        {
          label: 'Energiatodistukset',
          href: '#/energiatodistus/all'
        },
        {
          label: 'Laatijat',
          href: `#/laatija/all`
        },
        {
          label: 'Oikeellisuuden valvonta',
          href: `#/valvonta/oikeellisuus/all?valvoja-id=1&has-valvoja=false`
        },
        { label: 'Viestit', href: '#/viesti/all' }
      ];

      assert.deepEqual(
        R.map(
          R.dissoc('badge'),
          Navigation.parseViesti(isDev, i18n, whoami, locationParts)
        ),
        expected
      );
    });
    it('should return empty for others', () => {
      const isDev = false;
      const whoami = { id: 1, rooli: 2 };
      const locationParts = ['not-all'];

      const expected = [];

      assert.deepEqual(
        R.map(
          R.dissoc('badge'),
          Navigation.parseViesti(isDev, i18n, whoami, locationParts)
        ),
        expected
      );
    });
  });

  describe('roleBasedHeaderMenuLinks', () => {
    const laatija = { id: 1, rooli: 0 };
    const patevyydentoteaja = { id: 1, rooli: 1 };
    const paakayttaja = { id: 1, rooli: 2 };
    const laskuttaja = { id: 1, rooli: 3 };
    it('should return links for paakayttaja', () => {
      const expected = [
        {
          href: `#/yritys/all`,
          text: 'Yritykset'
        }
      ];

      assert.deepEqual(
        Navigation.roleBasedHeaderMenuLinks(i18n, paakayttaja),
        expected
      );
    });

    it('should return links for laskuttaja', () => {
      const expected = [
        {
          href: `#/yritys/all`,
          text: 'Yritykset'
        }
      ];

      assert.deepEqual(
        Navigation.roleBasedHeaderMenuLinks(i18n, laskuttaja),
        expected
      );
    });

    it('should return empty for others', () => {
      const expected = [];

      assert.deepEqual(
        Navigation.roleBasedHeaderMenuLinks(i18n, laatija),
        expected
      );
    });
  });
});
