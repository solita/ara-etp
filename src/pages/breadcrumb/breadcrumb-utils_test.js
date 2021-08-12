import * as BreadcrumbUtils from './breadcrumb-utils';
import * as Maybe from '@Utility/maybe-utils';
import { assert } from 'chai';

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

        assert.deepEqual(
          BreadcrumbUtils.yritysCrumb(
            i18n,
            idTranslate,
            whoami.paakayttaja,
            locations.all
          ),
          expected
        );
      });

      it('should return all yritykset link for laskuttaja with all', () => {
        const expected = [
          { url: '#/yritys/all', label: 'navigation.yritykset' }
        ];

        assert.deepEqual(
          BreadcrumbUtils.yritysCrumb(
            i18n,
            idTranslate,
            whoami.laskuttaja,
            locations.all
          ),
          expected
        );
      });

      it('should return laatijan yritykset link for laatija with all', () => {
        const expected = [
          { url: '#/laatija/1/yritykset', label: 'navigation.yritykset' }
        ];

        assert.deepEqual(
          BreadcrumbUtils.yritysCrumb(
            i18n,
            idTranslate,
            whoami.laatija,
            locations.all
          ),
          expected
        );
      });
    });

    describe('new', () => {
      it('should return crumb for new when not laatija', () => {
        const expected = [
          { url: '#/yritys/all', label: 'navigation.yritykset' },
          { url: '#/yritys/new', label: 'navigation.new-yritys' }
        ];

        assert.deepEqual(
          BreadcrumbUtils.yritysCrumb(
            i18n,
            idTranslate,
            whoami.paakayttaja,
            locations.new
          ),
          expected
        );
      });

      it('should return crumb for new with omat tiedot and yritykset before for laatija', () => {
        const expected = [
          { url: '#/kayttaja/1', label: 'navigation.omattiedot' },
          { url: '#/laatija/1/yritykset', label: 'navigation.yritykset' },
          { url: '#/yritys/new', label: 'navigation.new-yritys' }
        ];

        assert.deepEqual(
          BreadcrumbUtils.yritysCrumb(
            i18n,
            idTranslate,
            whoami.laatija,
            locations.new
          ),
          expected
        );
      });
    });

    describe('existing yritys', () => {
      const yritysId = ['1'];
      it('should return crumb for existing yritys with only ID', () => {
        const expected = [
          { url: '#/yritys/all', label: 'navigation.yritykset' },
          { url: '#/yritys/1', label: 'navigation.yritys 1' }
        ];

        assert.deepEqual(
          BreadcrumbUtils.yritysCrumb(i18n, {}, whoami.paakayttaja, yritysId),
          expected
        );
      });

      it('should return crumb for existing', () => {
        const expected = [
          { url: '#/yritys/all', label: 'navigation.yritykset' },
          { url: '#/yritys/1', label: 'Testiyritys' }
        ];

        assert.deepEqual(
          BreadcrumbUtils.yritysCrumb(
            i18n,
            idTranslate,
            whoami.paakayttaja,
            yritysId
          ),
          expected
        );
      });

      it('should return crumb with yrityksen laatijat', () => {
        const expected = [
          { url: '#/yritys/all', label: 'navigation.yritykset' },
          { url: '#/yritys/1', label: 'Testiyritys' },
          { url: '#/yritys/1/laatijat', label: 'navigation.yritys-laatijat' }
        ];

        assert.deepEqual(
          BreadcrumbUtils.yritysCrumb(i18n, idTranslate, whoami.paakayttaja, [
            ...yritysId,
            'laatijat'
          ]),
          expected
        );
      });
    });
  });

  describe('kayttajaCrumb', () => {
    it('should return laatijat with all', () => {
      const expected = [{ url: '#/laatija/all', label: 'navigation.laatijat' }];

      assert.deepEqual(
        BreadcrumbUtils.kayttajaCrumb(
          i18n,
          idTranslate,
          whoami.laatija,
          locations.all
        ),
        expected
      );
    });

    describe('existing', () => {
      it('should return kayttaja crumb with id when not in translate', () => {
        const expected = [
          {
            url: '#/kayttaja/2',
            label: 'navigation.kayttaja 2'
          }
        ];

        assert.deepEqual(
          BreadcrumbUtils.kayttajaCrumb(i18n, {}, whoami.paakayttaja, ['2']),
          expected
        );
      });
      it('should return kayttaja crumb with id when contained in translate and is laatija', () => {
        const expected = [
          { url: '#/laatija/all', label: 'navigation.laatijat' },
          {
            url: '#/kayttaja/2',
            label: 'Testi Laatija'
          }
        ];

        assert.deepEqual(
          BreadcrumbUtils.kayttajaCrumb(i18n, idTranslate, whoami.paakayttaja, [
            '2'
          ]),
          expected
        );
      });
      it('should return kayttaja crumb with id when contained in translate and is not laatija', () => {
        const expected = [
          {
            url: '#/kayttaja/3',
            label: 'Testi Pääkäyttäjä'
          }
        ];

        assert.deepEqual(
          BreadcrumbUtils.kayttajaCrumb(i18n, idTranslate, whoami.paakayttaja, [
            '3'
          ]),
          expected
        );
      });

      it('should return kayttaja crumb with self', () => {
        const expected = [
          { url: '#/kayttaja/1', label: 'navigation.omattiedot' }
        ];

        assert.deepEqual(
          BreadcrumbUtils.kayttajaCrumb(i18n, {}, whoami.paakayttaja, ['1']),
          expected
        );
      });

      it('should return kayttaja crumb with self and user in translate', () => {
        const expected = [
          { url: '#/kayttaja/1', label: 'navigation.omattiedot' }
        ];

        assert.deepEqual(
          BreadcrumbUtils.kayttajaCrumb(
            i18n,
            { kayttaja: { 1: whoami.paakayttaja } },
            whoami.paakayttaja,
            ['1']
          ),
          expected
        );
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

      assert.deepEqual(
        BreadcrumbUtils.laatijaCrumb(
          i18n,
          idTranslate,
          whoami.patevyydentoteaja,
          location
        ),
        expected
      );
    });

    it('should return all', () => {
      const expected = [
        {
          url: '#/laatija/all',
          label: 'navigation.laatijat'
        }
      ];
      assert.deepEqual(
        BreadcrumbUtils.laatijaCrumb(
          i18n,
          idTranslate,
          whoami.paakayttaja,
          locations.all
        ),
        expected
      );
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

          assert.deepEqual(
            BreadcrumbUtils.laatijaCrumb(i18n, {}, whoami.laatija, ['1']),
            expected
          );
        });
        it('should return laatija crumb for other than laatija', () => {
          const expected = [
            { url: '#/laatija/all', label: 'navigation.laatijat' },
            {
              url: '#/kayttaja/2',
              label: 'navigation.kayttaja 2'
            }
          ];
          assert.deepEqual(
            BreadcrumbUtils.laatijaCrumb(i18n, {}, whoami.paakayttaja, ['2']),
            expected
          );
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
          assert.deepEqual(
            BreadcrumbUtils.laatijaCrumb(i18n, {}, whoami.paakayttaja, [
              '2',
              'yritykset'
            ]),
            expected
          );
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
          assert.deepEqual(
            BreadcrumbUtils.laatijaCrumb(
              i18n,
              idTranslate,
              whoami.paakayttaja,
              ['2']
            ),
            expected
          );
        });
        it('should return non-laatija crumb with translation', () => {
          const expected = [
            { url: '#/laatija/all', label: 'navigation.laatijat' },
            {
              url: '#/kayttaja/3',
              label: 'Testi Pääkäyttäjä'
            }
          ];
          assert.deepEqual(
            BreadcrumbUtils.laatijaCrumb(
              i18n,
              idTranslate,
              whoami.paakayttaja,
              ['3']
            ),
            expected
          );
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

      assert.deepEqual(
        BreadcrumbUtils.energiatodistusCrumb(i18n, locations.all),
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

      assert.deepEqual(
        BreadcrumbUtils.energiatodistusCrumb(i18n, ['2018', ...locations.new]),
        expected
      );
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

        assert.deepEqual(
          BreadcrumbUtils.energiatodistusCrumb(i18n, ['2018', '1']),
          expected
        );
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

        assert.deepEqual(
          BreadcrumbUtils.energiatodistusCrumb(i18n, ['2018', '1', 'liitteet']),
          expected
        );
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

        assert.deepEqual(
          BreadcrumbUtils.energiatodistusCrumb(i18n, ['2018', '1', 'viestit']),
          expected
        );
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

        assert.deepEqual(
          BreadcrumbUtils.energiatodistusCrumb(i18n, [
            '2018',
            '1',
            'viestit',
            'new'
          ]),
          expected
        );
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

        assert.deepEqual(
          BreadcrumbUtils.energiatodistusCrumb(i18n, [
            '2018',
            '1',
            'muutoshistoria'
          ]),
          expected
        );
      });
    });
  });

  describe('viestiCrumb', () => {
    it('should return all', () => {
      const expected = [{ url: '#/viesti/all', label: 'navigation.viesti' }];

      assert.deepEqual(
        BreadcrumbUtils.viestiCrumb(i18n, idTranslate, locations.all),
        expected
      );
    });

    it('should return new', () => {
      const expected = [
        { url: '#/viesti/all', label: 'navigation.viesti' },
        { url: '#/viesti/new', label: 'navigation.uusi-viesti' }
      ];

      assert.deepEqual(
        BreadcrumbUtils.viestiCrumb(i18n, idTranslate, locations.new),
        expected
      );
    });

    describe('existing', () => {
      it('should return id crumb for non translated', () => {
        const expected = [
          { url: '#/viesti/all', label: 'navigation.viesti' },
          { url: '#/viesti/1', label: 'navigation.viestiketju 1' }
        ];

        assert.deepEqual(
          BreadcrumbUtils.viestiCrumb(i18n, {}, ['1']),
          expected
        );
      });

      it('should return crumb for translated', () => {
        const expected = [
          { url: '#/viesti/all', label: 'navigation.viesti' },
          { url: '#/viesti/1', label: 'Testiotsikko 1' }
        ];

        assert.deepEqual(
          BreadcrumbUtils.viestiCrumb(i18n, idTranslate, ['1']),
          expected
        );
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

        assert.deepEqual(
          BreadcrumbUtils.viestiCrumb(i18n, idTranslate, ['2']),
          expected
        );
      });
    });
  });

  describe('valvontaCrumb', () => {
    describe('oikeellisuus', () => {
      it('should return all', () => {
        const expected = [
          {
            url: '#/valvonta/oikeellisuus/all',
            label: 'navigation.valvonta.oikeellisuus.all'
          }
        ];

        assert.deepEqual(
          BreadcrumbUtils.valvontaCrumb(i18n, [
            'oikeellisuus',
            ...locations.all
          ]),
          expected
        );
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

        assert.deepEqual(
          BreadcrumbUtils.valvontaCrumb(i18n, ['oikeellisuus', '2018', '1']),
          expected
        );
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

        assert.deepEqual(
          BreadcrumbUtils.valvontaCrumb(i18n, ['kaytto', ...locations.all]),
          expected
        );
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

        assert.deepEqual(
          BreadcrumbUtils.valvontaCrumb(i18n, ['kaytto', '1']),
          expected
        );
      });
    });
  });
});
