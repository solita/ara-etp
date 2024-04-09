import { expect, describe, it } from '@jest/globals';
import * as R from 'ramda';

import * as Navigation from './navigation';

describe('Navigation', () => {
  const i18n = R.prop(R.__, {
    'navigation.et': 'ET',
    'navigation.uusi-energiatodistus': 'Uusi energiatodistus',
    'navigation.viestit': 'Viestit',
    'navigation.energiatodistus-viestit': 'Viestit',
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
    'navigation.viestiketju': 'Viestiketju',
    'navigation.valvonta.valvonta': 'Valvonta',
    'navigation.valvonta.oikeellisuus.all.valvoja': 'Oikeellisuuden valvonnat',
    'navigation.valvonta.oikeellisuus.all.laatija': 'Valvonnat',
    'navigation.valvonta.oikeellisuus.valvonta': 'Valvonta',
    'navigation.valvonta.oikeellisuus.virhetypes': 'Virhetyypit',
    'navigation.valvonta.kaytto.all': 'Käytön valvonta'
  });

  describe('locationParts', () => {
    it('should return parts', () => {
      const location = '/yritys/new';
      const expected = ['yritys', 'new'];

      expect(Navigation.locationParts(location)).toEqual(expected);
    });
  });

  describe('parseEnergiatodistus', () => {
    const kayttaja = { rooli: 0, id: 2 };

    it('should return energiatodistus-links when within energiatodistus', () => {
      const expected = [
        { label: 'ET 1', href: '#/energiatodistus/2018/1' },
        { label: 'Liitteet', href: '#/energiatodistus/2018/1/liitteet' },
        { label: 'Valvonta', href: '#/valvonta/oikeellisuus/2018/1' },
        { label: 'Viestit', href: '#/energiatodistus/2018/1/viestit' },
        {
          label: 'Muutoshistoria',
          href: '#/energiatodistus/2018/1/muutoshistoria'
        }
      ];

      expect(
        R.map(
          R.pick(['label', 'href']),
          Navigation.parseEnergiatodistus(false, i18n, kayttaja, ['2018', '1'])
        )
      ).toEqual(expected);
    });

    it('should return root links when outside single energiatodistus', () => {
      const expected = [
        {
          label: 'Energiatodistukset',
          href: '#/energiatodistus/all'
        },
        { label: 'Viestit', href: '#/viesti/all' },
        {
          href: '#/valvonta/oikeellisuus/all',
          label: 'Valvonnat'
        },
        {
          label: 'Yritykset',
          href: `#/laatija/2/yritykset`
        }
      ];

      expect(
        R.map(
          R.dissoc('badge'),
          Navigation.parseEnergiatodistus(false, i18n, kayttaja, [])
        )
      ).toEqual(expected);
    });

    it('should return empty links when creating new energiatodistus', () => {
      const expected = [];

      expect(
        Navigation.parseEnergiatodistus(false, i18n, kayttaja, ['2018', 'new'])
      ).toEqual(expected);
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
        { label: 'Viestit', href: '#/viesti/all' },
        {
          href: '#/valvonta/oikeellisuus/all',
          label: 'Valvonnat'
        },
        {
          label: 'Yritykset',
          href: `#/laatija/1/yritykset`
        }
      ];

      expect(
        R.map(
          R.dissoc('badge'),
          Navigation.parseEnergiatodistus(false, i18n, kayttaja, [])
        )
      ).toEqual(expected);
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

      expect(
        Navigation.parseEnergiatodistus(false, i18n, kayttaja, [])
      ).toEqual(expected);
    });

    it('should return links for pääkäyttäjä', () => {
      const kayttaja = { rooli: 2, id: 1 };
      const expected = [
        { label: 'Energiatodistukset', href: '#/energiatodistus/all' },
        {
          label: 'Viestit',
          href: '#/viesti/all?kasittelija-id=1&has-kasittelija=false'
        },
        {
          label: 'Oikeellisuuden valvonnat',
          href: `#/valvonta/oikeellisuus/all?valvoja-id=${kayttaja.id}&has-valvoja=false`
        },
        {
          label: 'Käytön valvonta',
          href: `#/valvonta/kaytto/all?valvoja-id=${kayttaja.id}&has-valvoja=false`
        },
        { label: 'Laatijat', href: '#/laatija/all' }
      ];

      expect(
        R.map(
          R.dissoc('badge'),
          Navigation.parseEnergiatodistus(false, i18n, kayttaja, [])
        )
      ).toEqual(expected);
    });
  });

  describe('parseYritys', () => {
    it('should return empty for all', () => {
      const isDev = false;
      const locationParts = ['all'];
      const whoami = { rooli: 0, id: 1 };
      const expected = [];

      expect(
        R.map(
          R.dissoc('badge'),
          Navigation.parseYritys(isDev, i18n, whoami, {}, locationParts)
        )
      ).toEqual(expected);
    });
    it('should return empty for new', () => {
      const isDev = false;
      const locationParts = ['new'];
      const whoami = { rooli: 0, id: 1 };
      const expected = [];

      expect(
        Navigation.parseYritys(isDev, i18n, whoami, {}, locationParts)
      ).toEqual(expected);
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

      expect(
        R.map(
          R.dissoc('badge'),
          Navigation.parseYritys(isDev, i18n, whoami, {}, locationParts)
        )
      ).toEqual(expected);
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

      expect(
        R.map(
          R.dissoc('badge'),
          Navigation.parseYritys(
            isDev,
            i18n,
            whoami,
            { yritys: { 1: 'Testiyritys' } },
            locationParts
          )
        )
      ).toEqual(expected);
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

      expect(
        Navigation.parseLaatija(
          isDev,
          patevyydentoteaja,
          i18n,
          {},
          locationParts
        )
      ).toEqual(expected);
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

      expect(
        Navigation.parseLaatija(
          isDev,
          patevyydentoteaja,
          i18n,
          {},
          locationParts
        )
      ).toEqual(expected);
    });

    it('should return laatija-nav for laatija', () => {
      const isDev = false;
      const locationParts = ['1'];

      const expected = [
        {
          label: 'Testi Laatija',
          href: '#/kayttaja/1'
        },
        { label: 'Yritykset', href: '#/laatija/1/yritykset' },
        { label: 'Muutoshistoria', href: '#/kayttaja/1/muutoshistoria' }
      ];

      expect(
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
        )
      ).toEqual(expected);
    });
    it('should return empty without translate', () => {
      const isDev = false;
      const locationParts = ['1'];

      const expected = [];

      expect(
        Navigation.parseKayttaja(
          isDev,
          paakayttaja,
          i18n,
          {
            kayttaja: {}
          },
          locationParts
        )
      ).toEqual(expected);
    });

    it('should return empty if locationParts contains yritykset', () => {
      const isDev = false;
      const locationParts = ['1', 'yritykset'];
      const expected = [];

      expect(
        Navigation.parseKayttaja(
          isDev,
          paakayttaja,
          i18n,
          {
            kayttaja: {}
          },
          locationParts
        )
      ).toEqual(expected);
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
          label: 'Viestit',
          href: '#/viesti/all?kasittelija-id=1&has-kasittelija=false'
        },
        {
          label: 'Oikeellisuuden valvonnat',
          href: `#/valvonta/oikeellisuus/all?valvoja-id=1&has-valvoja=false`
        },
        {
          label: 'Käytön valvonta',
          href: `#/valvonta/kaytto/all?valvoja-id=1&has-valvoja=false`
        },
        {
          label: 'Laatijat',
          href: `#/laatija/all`
        }
      ];

      expect(
        R.map(
          R.dissoc('badge'),
          Navigation.parseValvontaOikeellisuus(
            isDev,
            i18n,
            whoami,
            locationParts
          )
        )
      ).toEqual(expected);
    });
    it('should return empty for long locations', () => {
      const isDev = false;
      const whoami = { id: 1, rooli: 2 };
      const locationParts = ['1', '2', '3'];

      const expected = [];

      expect(
        R.map(
          R.dissoc('badge'),
          Navigation.parseValvontaOikeellisuus(
            isDev,
            i18n,
            whoami,
            locationParts
          )
        )
      ).toEqual(expected);
    });
    it('should return energiatodistus for others', () => {
      const isDev = false;
      const whoami = { id: 1, rooli: 2 };
      const locationParts = ['2018', '1'];

      const expected = [
        { label: 'ET 1', href: '#/energiatodistus/2018/1' },
        { label: 'Liitteet', href: '#/energiatodistus/2018/1/liitteet' },
        { label: 'Valvonta', href: '#/valvonta/oikeellisuus/2018/1' },
        { label: 'Viestit', href: '#/energiatodistus/2018/1/viestit' },
        {
          label: 'Muutoshistoria',
          href: '#/energiatodistus/2018/1/muutoshistoria'
        }
      ];

      expect(
        R.map(
          R.dissoc('badge'),
          Navigation.parseValvontaOikeellisuus(
            isDev,
            i18n,
            whoami,
            locationParts
          )
        )
      ).toEqual(expected);
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
          label: 'Viestit',
          href: '#/viesti/all?kasittelija-id=1&has-kasittelija=false'
        },
        {
          label: 'Oikeellisuuden valvonnat',
          href: `#/valvonta/oikeellisuus/all?valvoja-id=1&has-valvoja=false`
        },
        {
          label: 'Käytön valvonta',
          href: `#/valvonta/kaytto/all?valvoja-id=1&has-valvoja=false`
        },
        {
          label: 'Laatijat',
          href: `#/laatija/all`
        }
      ];

      expect(
        R.map(
          R.dissoc('badge'),
          Navigation.parseViesti(isDev, i18n, whoami, {}, locationParts)
        )
      ).toEqual(expected);
    });
    it('should return viesti links for others', () => {
      const isDev = false;
      const whoami = { id: 1, rooli: 2 };
      const locationParts = ['1'];

      const expected = [
        {
          href: '#/viesti/1',
          label: 'Viestiketju 1'
        },
        {
          href: '#/viesti/1/liitteet',
          label: 'Liitteet'
        }
      ];

      expect(
        R.map(
          R.dissoc('badgeValue'),
          Navigation.parseViesti(isDev, i18n, whoami, {}, locationParts)
        )
      ).toEqual(expected);
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
          href: '#/kayttaja/all',
          text: 'Käyttäjät'
        },
        {
          href: `#/yritys/all`,
          text: 'Yritykset'
        },
        {
          href: '#/valvonta/oikeellisuus/virhetypes',
          text: 'Virhetyypit'
        }
      ];

      expect(Navigation.roleBasedHeaderMenuLinks(i18n, paakayttaja)).toEqual(
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

      expect(Navigation.roleBasedHeaderMenuLinks(i18n, laskuttaja)).toEqual(
        expected
      );
    });

    it('should return empty for others', () => {
      const expected = [];

      expect(Navigation.roleBasedHeaderMenuLinks(i18n, laatija)).toEqual(
        expected
      );
    });
  });
});
