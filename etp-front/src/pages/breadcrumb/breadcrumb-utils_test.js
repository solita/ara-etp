import { expect, describe, it } from '@jest/globals';
import * as BreadcrumbUtils from './breadcrumb-utils';
import * as Maybe from '@Utility/maybe-utils';

describe('BreadcrumbUtils', () => {
  const whoami = {
    laatija: { rooli: 0, id: 1, etunimi: 'Laila', sukunimi: 'Laatija' },
    patevyydentoteaja: {
      rooli: 1,
      id: 1,
      etunimi: 'Paavo',
      sukunimi: 'Pätevyydentoteaja'
    },
    paakayttaja: { rooli: 2, id: 1, etunimi: 'Päivi', sukunimi: 'Pääkäyttäjä' },
    laskuttaja: { rooli: 3, id: 1, etunimi: 'Lasse', sukunimi: 'Laskuttaja' }
  };

  const locations = {
    all: ['all'],
    new: ['new']
  };

  const idTranslate = {
    yritys: {
      1: 'Testiyritys'
    },
    kayttaja: {
      2: { id: 2, rooli: 0, etunimi: 'Testi', sukunimi: 'Laatija' },
      3: { id: 3, rooli: 3, etunimi: 'Testi', sukunimi: 'Pääkäyttäjä' }
    },
    viesti: {
      1: {
        id: 1,
        subject: 'Testiotsikko 1',
        'energiatodistus-id': Maybe.None()
      },

      2: {
        id: 2,
        subject: 'Testiotsikko 2',
        'energiatodistus-id': Maybe.Some(1)
      }
    }
  };

  const i18n = a => a;

  describe('yritysCrumb', () => {
    describe('all', () => {
      it('should return all yritykset link for paakayttaja with all', () => {
        const expected = [
          { url: '#/yritys/all', label: 'navigation.yritykset' }
        ];

        expect(
          BreadcrumbUtils.yritysCrumb(
            i18n,
            idTranslate,
            whoami.paakayttaja,
            locations.all
          )
        ).toEqual(expected);
      });

      it('should return all yritykset link for laskuttaja with all', () => {
        const expected = [
          { url: '#/yritys/all', label: 'navigation.yritykset' }
        ];

        expect(
          BreadcrumbUtils.yritysCrumb(
            i18n,
            idTranslate,
            whoami.laskuttaja,
            locations.all
          )
        ).toEqual(expected);
      });

      it('should return laatijan yritykset link for laatija with all', () => {
        const expected = [
          { url: '#/laatija/1/yritykset', label: 'navigation.yritykset' }
        ];

        expect(
          BreadcrumbUtils.yritysCrumb(
            i18n,
            idTranslate,
            whoami.laatija,
            locations.all
          )
        ).toEqual(expected);
      });
    });

    describe('new', () => {
      it('should return crumb for new when not laatija', () => {
        const expected = [
          { url: '#/yritys/all', label: 'navigation.yritykset' },
          { url: '#/yritys/new', label: 'navigation.new-yritys' }
        ];

        expect(
          BreadcrumbUtils.yritysCrumb(
            i18n,
            idTranslate,
            whoami.paakayttaja,
            locations.new
          )
        ).toEqual(expected);
      });

      it('should return crumb for new with omat tiedot and yritykset before for laatija', () => {
        const expected = [
          { url: '#/kayttaja/1', label: 'navigation.omattiedot' },
          { url: '#/laatija/1/yritykset', label: 'navigation.yritykset' },
          { url: '#/yritys/new', label: 'navigation.new-yritys' }
        ];

        expect(
          BreadcrumbUtils.yritysCrumb(
            i18n,
            idTranslate,
            whoami.laatija,
            locations.new
          )
        ).toEqual(expected);
      });
    });

    describe('existing yritys', () => {
      const yritysId = ['1'];
      it('should return crumb for existing yritys with only ID', () => {
        const expected = [
          { url: '#/yritys/all', label: 'navigation.yritykset' },
          { url: '#/yritys/1', label: 'navigation.yritys 1' }
        ];

        expect(
          BreadcrumbUtils.yritysCrumb(i18n, {}, whoami.paakayttaja, yritysId)
        ).toEqual(expected);
      });

      it('should return crumb for existing', () => {
        const expected = [
          { url: '#/yritys/all', label: 'navigation.yritykset' },
          { url: '#/yritys/1', label: 'Testiyritys' }
        ];

        expect(
          BreadcrumbUtils.yritysCrumb(
            i18n,
            idTranslate,
            whoami.paakayttaja,
            yritysId
          )
        ).toEqual(expected);
      });

      it('should return crumb with yrityksen laatijat', () => {
        const expected = [
          { url: '#/yritys/all', label: 'navigation.yritykset' },
          { url: '#/yritys/1', label: 'Testiyritys' },
          { url: '#/yritys/1/laatijat', label: 'navigation.yritys-laatijat' }
        ];

        expect(
          BreadcrumbUtils.yritysCrumb(i18n, idTranslate, whoami.paakayttaja, [
            ...yritysId,
            'laatijat'
          ])
        ).toEqual(expected);
      });
    });
  });

  describe('kayttajaCrumb', () => {
    it('should return kayttajat with all', () => {
      const expected = [
        { url: '#/kayttaja/all', label: 'navigation.kayttajat' }
      ];

      expect(
        BreadcrumbUtils.kayttajaCrumb(
          i18n,
          idTranslate,
          whoami.laatija,
          locations.all
        )
      ).toEqual(expected);
    });

    describe('existing', () => {
      it('should return kayttaja crumb with id when not in translate', () => {
        const expected = [
          {
            url: '#/kayttaja/2',
            label: 'navigation.kayttaja 2'
          }
        ];

        expect(
          BreadcrumbUtils.kayttajaCrumb(i18n, {}, whoami.paakayttaja, ['2'])
        ).toEqual(expected);
      });
      it('should return kayttaja crumb with id when contained in translate and is laatija', () => {
        const expected = [
          { url: '#/laatija/all', label: 'navigation.laatijat' },
          {
            url: '#/kayttaja/2',
            label: 'Testi Laatija'
          }
        ];

        expect(
          BreadcrumbUtils.kayttajaCrumb(i18n, idTranslate, whoami.paakayttaja, [
            '2'
          ])
        ).toEqual(expected);
      });
      it('should return kayttaja crumb with id when contained in translate and is not laatija', () => {
        const expected = [
          {
            label: 'navigation.kayttajat',
            url: '#/kayttaja/all'
          },
          {
            url: '#/kayttaja/3',
            label: 'Testi Pääkäyttäjä'
          }
        ];

        expect(
          BreadcrumbUtils.kayttajaCrumb(i18n, idTranslate, whoami.paakayttaja, [
            '3'
          ])
        ).toEqual(expected);
      });

      it('should return kayttaja crumb with self', () => {
        const expected = [
          { url: '#/kayttaja/1', label: 'navigation.omattiedot' }
        ];

        expect(
          BreadcrumbUtils.kayttajaCrumb(i18n, {}, whoami.paakayttaja, ['1'])
        ).toEqual(expected);
      });

      it('should return kayttaja crumb with self and user in translate', () => {
        const expected = [
          { url: '#/kayttaja/1', label: 'navigation.omattiedot' }
        ];

        expect(
          BreadcrumbUtils.kayttajaCrumb(
            i18n,
            { kayttaja: { 1: whoami.paakayttaja } },
            whoami.paakayttaja,
            ['1']
          )
        ).toEqual(expected);
      });
    });
  });

  describe('laatijaCrumb', () => {
    it('should return crumb for laatijoidentuonti', () => {
      const location = ['laatijoidentuonti'];

      const expected = [
        {
          url: `#/laatija/laatijoidentuonti`,
          label: 'navigation.laatijoidentuonti'
        }
      ];

      expect(
        BreadcrumbUtils.laatijaCrumb(
          i18n,
          idTranslate,
          whoami.patevyydentoteaja,
          location
        )
      ).toEqual(expected);
    });

    it('should return all', () => {
      const expected = [
        {
          url: '#/laatija/all',
          label: 'navigation.laatijat'
        }
      ];
      expect(
        BreadcrumbUtils.laatijaCrumb(
          i18n,
          idTranslate,
          whoami.paakayttaja,
          locations.all
        )
      ).toEqual(expected);
    });

    describe('existing', () => {
      describe('no translation', () => {
        it('should return laatija crumb with omattiedot for laatija', () => {
          const expected = [
            {
              url: '#/kayttaja/1',
              label: 'navigation.omattiedot'
            }
          ];

          expect(
            BreadcrumbUtils.laatijaCrumb(i18n, {}, whoami.laatija, ['1'])
          ).toEqual(expected);
        });
        it('should return laatija crumb for other than laatija', () => {
          const expected = [
            { url: '#/laatija/all', label: 'navigation.laatijat' },
            {
              url: '#/kayttaja/2',
              label: 'navigation.kayttaja 2'
            }
          ];
          expect(
            BreadcrumbUtils.laatijaCrumb(i18n, {}, whoami.paakayttaja, ['2'])
          ).toEqual(expected);
        });
        it('should return laatija crumb with yritykset', () => {
          const expected = [
            { url: '#/laatija/all', label: 'navigation.laatijat' },
            {
              url: '#/kayttaja/2',
              label: 'navigation.kayttaja 2'
            },
            { url: '#/laatija/2/yritykset', label: 'navigation.yritykset' }
          ];
          expect(
            BreadcrumbUtils.laatijaCrumb(i18n, {}, whoami.paakayttaja, [
              '2',
              'yritykset'
            ])
          ).toEqual(expected);
        });
      });
      describe('with translation', () => {
        it('should return laatija crumb with translation', () => {
          const expected = [
            { url: '#/laatija/all', label: 'navigation.laatijat' },
            {
              url: '#/kayttaja/2',
              label: 'Testi Laatija'
            }
          ];
          expect(
            BreadcrumbUtils.laatijaCrumb(
              i18n,
              idTranslate,
              whoami.paakayttaja,
              ['2']
            )
          ).toEqual(expected);
        });
        it('should return non-laatija crumb with translation', () => {
          const expected = [
            { url: '#/laatija/all', label: 'navigation.laatijat' },
            {
              url: '#/kayttaja/3',
              label: 'Testi Pääkäyttäjä'
            }
          ];
          expect(
            BreadcrumbUtils.laatijaCrumb(
              i18n,
              idTranslate,
              whoami.paakayttaja,
              ['3']
            )
          ).toEqual(expected);
        });
      });
    });
  });

  describe('energiatodistusCrumb', () => {
    it('should return all', () => {
      const expected = [
        {
          url: '#/energiatodistus/all',
          label: 'navigation.energiatodistukset'
        }
      ];

      expect(BreadcrumbUtils.energiatodistusCrumb(i18n, locations.all)).toEqual(
        expected
      );
    });
    it('should return new', () => {
      const expected = [
        {
          url: '#/energiatodistus/all',
          label: 'navigation.energiatodistukset'
        },
        {
          url: `#/energiatodistus/2018/new`,
          label: 'navigation.uusi-energiatodistus'
        }
      ];

      expect(
        BreadcrumbUtils.energiatodistusCrumb(i18n, ['2018', ...locations.new])
      ).toEqual(expected);
    });
    describe('existing', () => {
      it('should return crumb for energiatodistus', () => {
        const expected = [
          {
            url: '#/energiatodistus/all',
            label: 'navigation.energiatodistukset'
          },
          {
            url: `#/energiatodistus/2018/1`,
            label: 'navigation.et 1'
          }
        ];

        expect(
          BreadcrumbUtils.energiatodistusCrumb(i18n, ['2018', '1'])
        ).toEqual(expected);
      });

      it('should return crumb for energiatodistus liitteet', () => {
        const expected = [
          {
            url: '#/energiatodistus/all',
            label: 'navigation.energiatodistukset'
          },
          {
            url: `#/energiatodistus/2018/1`,
            label: 'navigation.et 1'
          },
          {
            url: `#/energiatodistus/2018/1/liitteet`,
            label: 'navigation.liitteet'
          }
        ];

        expect(
          BreadcrumbUtils.energiatodistusCrumb(i18n, ['2018', '1', 'liitteet'])
        ).toEqual(expected);
      });

      it('should return crumb for energiatodistus viestit', () => {
        const expected = [
          {
            url: '#/energiatodistus/all',
            label: 'navigation.energiatodistukset'
          },
          {
            url: `#/energiatodistus/2018/1`,
            label: 'navigation.et 1'
          },
          {
            url: `#/energiatodistus/2018/1/viestit`,
            label: 'navigation.viestit'
          }
        ];

        expect(
          BreadcrumbUtils.energiatodistusCrumb(i18n, ['2018', '1', 'viestit'])
        ).toEqual(expected);
      });

      it('should return crumb for energiatodistus new viesti', () => {
        const expected = [
          {
            url: '#/energiatodistus/all',
            label: 'navigation.energiatodistukset'
          },
          {
            url: `#/energiatodistus/2018/1`,
            label: 'navigation.et 1'
          },
          {
            url: `#/energiatodistus/2018/1/viestit`,
            label: 'navigation.viestit'
          },
          {
            url: `#/energiatodistus/2018/1/viestit/new`,
            label: i18n('navigation.uusi-viesti')
          }
        ];

        expect(
          BreadcrumbUtils.energiatodistusCrumb(i18n, [
            '2018',
            '1',
            'viestit',
            'new'
          ])
        ).toEqual(expected);
      });

      it('should return crumb for energiatodistus muutoshistoria', () => {
        const expected = [
          {
            url: '#/energiatodistus/all',
            label: 'navigation.energiatodistukset'
          },
          {
            url: `#/energiatodistus/2018/1`,
            label: 'navigation.et 1'
          },
          {
            url: `#/energiatodistus/2018/1/muutoshistoria`,
            label: 'navigation.muutoshistoria'
          }
        ];

        expect(
          BreadcrumbUtils.energiatodistusCrumb(i18n, [
            '2018',
            '1',
            'muutoshistoria'
          ])
        ).toEqual(expected);
      });
    });
  });

  describe('viestiCrumb', () => {
    const laatija = { id: 1, rooli: 0 };
    const paakayttaja = { id: 1, rooli: 2 };

    it('should return all for laatija', () => {
      const expected = [{ url: '#/viesti/all', label: 'navigation.viesti' }];

      expect(
        BreadcrumbUtils.viestiCrumb(i18n, idTranslate, laatija, locations.all)
      ).toEqual(expected);
    });

    it('should return all for paakayttaja', () => {
      const expected = [
        {
          url: '#/viesti/all?kasittelija-id=1&has-kasittelija=false',
          label: 'navigation.viesti'
        }
      ];

      expect(
        BreadcrumbUtils.viestiCrumb(
          i18n,
          idTranslate,
          paakayttaja,
          locations.all
        )
      ).toEqual(expected);
    });

    it('should return new for laatija', () => {
      const expected = [
        { url: '#/viesti/all', label: 'navigation.viesti' },
        { url: '#/viesti/new', label: 'navigation.uusi-viesti' }
      ];

      expect(
        BreadcrumbUtils.viestiCrumb(i18n, idTranslate, laatija, locations.new)
      ).toEqual(expected);
    });

    it('should return new for paakayttaja', () => {
      const expected = [
        {
          url: '#/viesti/all?kasittelija-id=1&has-kasittelija=false',
          label: 'navigation.viesti'
        },
        { url: '#/viesti/new', label: 'navigation.uusi-viesti' }
      ];

      expect(
        BreadcrumbUtils.viestiCrumb(
          i18n,
          idTranslate,
          paakayttaja,
          locations.new
        )
      ).toEqual(expected);
    });

    describe('existing', () => {
      it('should return id crumb for non translated', () => {
        const expected = [
          { url: '#/viesti/all', label: 'navigation.viesti' },
          { url: '#/viesti/1', label: 'navigation.viestiketju 1' }
        ];

        expect(BreadcrumbUtils.viestiCrumb(i18n, {}, laatija, ['1'])).toEqual(
          expected
        );
      });

      it('should return crumb for translated', () => {
        const expected = [
          { url: '#/viesti/all', label: 'navigation.viesti' },
          { url: '#/viesti/1', label: 'Testiotsikko 1' }
        ];

        expect(
          BreadcrumbUtils.viestiCrumb(i18n, idTranslate, laatija, ['1'])
        ).toEqual(expected);
      });

      it('should return crumb for translated and attached', () => {
        const expected = [
          {
            url: '#/energiatodistus/all',
            label: 'navigation.energiatodistukset'
          },
          { url: '#/energiatodistus/1', label: 'navigation.et 1' },
          { url: '#/energiatodistus/1/viestit', label: 'navigation.viestit' },
          { url: '#/viesti/2', label: 'Testiotsikko 2' }
        ];

        expect(
          BreadcrumbUtils.viestiCrumb(i18n, idTranslate, laatija, ['2'])
        ).toEqual(expected);
      });
    });
  });

  describe('valvontaCrumb', () => {
    const whoami = { id: 1 };
    describe('oikeellisuus', () => {
      it('should return all', () => {
        const expected = [
          {
            url: '#/valvonta/oikeellisuus/all',
            label: 'navigation.valvonta.oikeellisuus.all.laatija'
          }
        ];

        expect(
          BreadcrumbUtils.valvontaCrumb(i18n, whoami, [
            'oikeellisuus',
            ...locations.all
          ])
        ).toEqual(expected);
      });

      it('should return existing', () => {
        const expected = [
          {
            url: '#/energiatodistus/all',
            label: 'navigation.energiatodistukset'
          },
          {
            url: '#/energiatodistus/2018/1',
            label: 'navigation.et 1'
          },
          {
            url: '#/valvonta/oikeellisuus/2018/1',
            label: 'navigation.valvonta.oikeellisuus.valvonta'
          }
        ];

        expect(
          BreadcrumbUtils.valvontaCrumb(i18n, whoami, [
            'oikeellisuus',
            '2018',
            '1'
          ])
        ).toEqual(expected);
      });
    });
    describe('kaytto', () => {
      it('should return all', () => {
        const expected = [
          {
            url: '#/valvonta/kaytto/all',
            label: 'navigation.valvonta.kaytto.all'
          }
        ];

        expect(
          BreadcrumbUtils.valvontaCrumb(i18n, whoami, [
            'kaytto',
            ...locations.all
          ])
        ).toEqual(expected);
      });

      it('should return existing', () => {
        const expected = [
          {
            url: '#/valvonta/kaytto/all',
            label: 'navigation.valvonta.kaytto.all'
          },
          {
            url: `#/valvonta/kaytto/1/kohde`,
            label: 'navigation.valvonta.kaytto.kohde'
          }
        ];

        expect(
          BreadcrumbUtils.valvontaCrumb(i18n, whoami, ['kaytto', '1'])
        ).toEqual(expected);
      });
    });
  });
});
