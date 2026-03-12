import { expect, describe, it } from '@jest/globals';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';
import * as R from 'ramda';

import * as EtUtils from './energiatodistus-utils';

describe('Energiatodistus Utils: ', () => {
  describe('unnestValidation', () => {
    it('should return value within Either', () => {
      const value = Either.Right(Maybe.Some(1));
      const expected = Maybe.Some(1);

      expect(expected).toEqual(EtUtils.unnestValidation(value));
    });

    it('should return none on Either.Left', () => {
      const value = Either.Left(1);
      const expected = Maybe.None();

      expect(expected).toEqual(EtUtils.unnestValidation(value));
    });

    it('should return none on Either.Left', () => {
      const value = Either.Left(1);
      const expected = Maybe.None();

      expect(expected).toEqual(EtUtils.unnestValidation(value));
    });

    it('should return Identity with non-either value', () => {
      const value = Maybe.Some(1);
      const expected = Maybe.Some(1);

      expect(expected).toEqual(EtUtils.unnestValidation(value));
    });
  });

  describe('energiatodistusPath', () => {
    it('should return unnested value from path', () => {
      const obj = { a: { b: Either.Right(Maybe.Some(1)) } };
      const path = ['a', 'b'];

      const expected = Maybe.Some(1);

      expect(expected).toEqual(EtUtils.energiatodistusPath(path, obj));
    });

    it('should return value when non-either', () => {
      const obj = { a: { b: Maybe.Some(1) } };
      const path = ['a', 'b'];

      const expected = Maybe.Some(1);

      expect(expected).toEqual(EtUtils.energiatodistusPath(path, obj));
    });
  });

  describe('calculatePaths', () => {
    it('should calculate unnested paths with given function', () => {
      const obj = {
        first: { a: Either.Right(Maybe.Some(1)) },
        second: { b: Either.Right(Maybe.Some(2)) }
      };
      const firstPath = ['first', 'a'];
      const secondPath = ['second', 'b'];

      const calcFn = a => b => a + b;
      const expected = Maybe.Some(3);

      expect(expected).toEqual(
        EtUtils.calculatePaths(calcFn, firstPath, secondPath, obj)
      );
    });

    it('should calculate unnested paths with given function with mixed-eithers', () => {
      const obj = {
        first: { a: Maybe.Some(1) },
        second: { b: Either.Right(Maybe.Some(2)) }
      };
      const firstPath = ['first', 'a'];
      const secondPath = ['second', 'b'];

      const calcFn = a => b => a + b;
      const expected = Maybe.Some(3);

      expect(expected).toEqual(
        EtUtils.calculatePaths(calcFn, firstPath, secondPath, obj)
      );
    });

    it('should calculate unnested paths with given function with non-eithers', () => {
      const obj = {
        first: { a: Maybe.Some(1) },
        second: { b: Maybe.Some(2) }
      };
      const firstPath = ['first', 'a'];
      const secondPath = ['second', 'b'];

      const calcFn = a => b => a + b;
      const expected = Maybe.Some(3);

      expect(expected).toEqual(
        EtUtils.calculatePaths(calcFn, firstPath, secondPath, obj)
      );
    });
  });

  describe('teknistenJarjestelmienSahkot', () => {
    it('should return object with all possible sahko-fields', () => {
      const et = {
        tulokset: {
          'tekniset-jarjestelmat': {
            'iv-sahko': Either.Right(1),
            'kuluttajalaitteet-ja-valaistus-sahko': Either.Right(2),
            jaahdytys: {
              sahko: Either.Right(3),
              lampo: Either.Right(1),
              kaukojaahdytys: Either.Right(1)
            },
            'kayttoveden-valmistus': {
              sahko: Either.Right(4),
              lampo: Either.Right(2)
            },
            'tilojen-lammitys': {
              sahko: Either.Right(5),
              lampo: Either.Right(3)
            },
            'tuloilman-lammitys': {
              sahko: Either.Right(6),
              lampo: Either.Right(4)
            }
          }
        }
      };

      const expected = {
        'iv-sahko': 1,
        'kuluttajalaitteet-ja-valaistus-sahko': 2,
        jaahdytys: 3,
        'kayttoveden-valmistus': 4,
        'tilojen-lammitys': 5,
        'tuloilman-lammitys': 6
      };

      expect(expected).toEqual(EtUtils.teknistenJarjestelmienSahkot(et));
    });
  });

  describe('teknistenJarjestelmienLammot', () => {
    it('should return object with all possible lampo-fields', () => {
      const et = {
        tulokset: {
          'tekniset-jarjestelmat': {
            'iv-sahko': Either.Right(1),
            'kuluttajalaitteet-ja-valaistus-sahko': Either.Right(2),
            jaahdytys: {
              sahko: Either.Right(3),
              lampo: Either.Right(1),
              kaukojaahdytys: Either.Right(1)
            },
            'kayttoveden-valmistus': {
              sahko: Either.Right(4),
              lampo: Either.Right(2)
            },
            'tilojen-lammitys': {
              sahko: Either.Right(5),
              lampo: Either.Right(3)
            },
            'tuloilman-lammitys': {
              sahko: Either.Right(6),
              lampo: Either.Right(4)
            }
          }
        }
      };

      const expected = {
        jaahdytys: 1,
        'kayttoveden-valmistus': 2,
        'tilojen-lammitys': 3,
        'tuloilman-lammitys': 4
      };

      expect(expected).toEqual(EtUtils.teknistenJarjestelmienLammot(et));
    });
  });

  describe('teknistenJarjestelmienKaukojaahdytys', () => {
    it('should return object with all possible lampo-fields', () => {
      const et = {
        tulokset: {
          'tekniset-jarjestelmat': {
            'iv-sahko': Either.Right(1),
            'kuluttajalaitteet-ja-valaistus-sahko': Either.Right(2),
            jaahdytys: {
              sahko: Either.Right(3),
              lampo: Either.Right(1),
              kaukojaahdytys: Either.Right(1)
            },
            'kayttoveden-valmistus': {
              sahko: Either.Right(4),
              lampo: Either.Right(2)
            },
            'tilojen-lammitys': {
              sahko: Either.Right(5),
              lampo: Either.Right(3)
            },
            'tuloilman-lammitys': {
              sahko: Either.Right(6),
              lampo: Either.Right(4)
            }
          }
        }
      };

      const expected = {
        jaahdytys: 1
      };

      expect(expected).toEqual(
        EtUtils.teknistenJarjestelmienKaukojaahdytys(et)
      );
    });
  });

  describe('sumEtValues', () => {
    it('should sum given object with Maybe-values', () => {
      const values = {
        a: Maybe.Some(1),
        b: Maybe.Some(2),
        c: Maybe.None(),
        d: Maybe.Some(3),
        e: Maybe.None()
      };
      const expected = Maybe.Some(6);

      expect(expected).toEqual(EtUtils.sumEtValues(values));
    });
  });

  describe('partOfSum', () => {
    it('should calculate part of sum', () => {
      const sum = Maybe.Some(2);
      const value = Maybe.Some(1);

      const expected = Maybe.Some(0.5);

      expect(expected).toEqual(EtUtils.partOfSum(sum, value));
    });

    it('should return None when input is None', () => {
      const sum = Maybe.None();
      const value = Maybe.Some(1);

      const expected = Maybe.None();

      expect(expected).toEqual(EtUtils.partOfSum(sum, value));
    });
  });

  describe('rakennusvaippaUA', () => {
    it('should return all calculated U*As', () => {
      const et = {
        lahtotiedot: {
          rakennusvaippa: {
            'kylmasillat-UA': Either.Right(Maybe.Some(1)),
            ulkoseinat: { ala: Maybe.Some(2), U: Maybe.Some(2) },
            ylapohja: { ala: Maybe.Some(2), U: Maybe.Some(2) },
            alapohja: { ala: Maybe.Some(2), U: Maybe.Some(2) },
            ikkunat: { ala: Maybe.Some(2), U: Maybe.Some(2) },
            ulkoovet: { ala: Maybe.Some(2), U: Maybe.Some(2) }
          }
        }
      };

      const expected = {
        'kylmasillat-UA': Maybe.Some(1),
        ulkoseinat: Maybe.Some(4),
        ylapohja: Maybe.Some(4),
        alapohja: Maybe.Some(4),
        ikkunat: Maybe.Some(4),
        ulkoovet: Maybe.Some(4)
      };

      expect(expected).toEqual(EtUtils.rakennusvaippaUA(et));
    });
  });

  describe('ostoenergiat', () => {
    it('should return all ostoenergiat-fields and unnest', () => {
      const et = {
        tulokset: {
          'kaytettavat-energiamuodot': {
            kaukolampo: Either.Right(Maybe.Some(1)),
            sahko: Either.Right(Maybe.Some(2)),
            'uusiutuva-polttoaine': Either.Right(Maybe.Some(3)),
            'fossiilinen-polttoaine': Either.Right(Maybe.Some(4)),
            kaukojaahdytys: Either.Right(Maybe.Some(5))
          }
        }
      };

      const expected = {
        kaukolampo: Maybe.Some(1),
        sahko: Maybe.Some(2),
        'uusiutuva-polttoaine': Maybe.Some(3),
        'fossiilinen-polttoaine': Maybe.Some(4),
        kaukojaahdytys: Maybe.Some(5)
      };

      expect(expected).toEqual(EtUtils.ostoenergiat(et));
    });
  });

  describe('multiplyWithKerroin', () => {
    it('should return Some with proper values', () => {
      const ostoenergiamaara = Maybe.Some(10);
      const kerroin = Maybe.Some(2);

      const expected = Maybe.Some(20);

      expect(expected).toEqual(
        EtUtils.multiplyWithKerroin(kerroin, ostoenergiamaara)
      );
    });

    it('should return None with None ostoenergiamaara', () => {
      const ostoenergiamaara = Maybe.None();
      const kerroin = Maybe.Some(2);

      const expected = Maybe.None();

      expect(expected).toEqual(
        EtUtils.multiplyWithKerroin(kerroin, ostoenergiamaara)
      );
    });
  });

  describe('perLammitettyNettoala', () => {
    it('should return values divided by lammitetty nettoala', () => {
      const et = {
        lahtotiedot: {
          'lammitetty-nettoala': Either.Right(Maybe.Some(10))
        }
      };

      const values = {
        a: Maybe.Some(10),
        b: Maybe.Some(15)
      };

      const expected = {
        a: Maybe.Some(1),
        b: Maybe.Some(1.5)
      };

      expect(expected).toEqual(EtUtils.perLammitettyNettoala(et, values));
    });

    it('should return none when value is none', () => {
      const et = {
        lahtotiedot: {
          'lammitetty-nettoala': Either.Right(Maybe.Some(10))
        }
      };

      const values = {
        a: Maybe.Some(10),
        b: Maybe.None()
      };

      const expected = {
        a: Maybe.Some(1),
        b: Maybe.None()
      };

      expect(expected).toEqual(EtUtils.perLammitettyNettoala(et, values));
    });

    it('should return none when nettoala is none', () => {
      const et = {
        lahtotiedot: {
          'lammitetty-nettoala': Either.Right(Maybe.None())
        }
      };

      const values = {
        a: Maybe.Some(10),
        b: Maybe.Some(15)
      };

      const expected = {
        a: Maybe.None(),
        b: Maybe.None()
      };

      expect(expected).toEqual(EtUtils.perLammitettyNettoala(et, values));
    });
  });

  describe('omavaraisenergiat', () => {
    it('should return unnested omavaraisenergiat', () => {
      const et = {
        tulokset: {
          'uusiutuvat-omavaraisenergiat': {
            aurinkosahko: Either.Right(Maybe.Some(1)),
            tuulisahko: Either.Right(Maybe.Some(1)),
            aurinkolampo: Either.Right(Maybe.Some(1)),
            muulampo: Either.Right(Maybe.Some(1)),
            muusahko: Either.Right(Maybe.Some(1)),
            lampopumppu: Either.Right(Maybe.Some(1))
          }
        }
      };

      const expected = {
        aurinkosahko: Maybe.Some(1),
        tuulisahko: Maybe.Some(1),
        aurinkolampo: Maybe.Some(1),
        muulampo: Maybe.Some(1),
        muusahko: Maybe.Some(1),
        lampopumppu: Maybe.Some(1)
      };

      expect(expected).toEqual(EtUtils.omavaraisenergiat(et));
    });
  });

  describe('nettotarpeet', () => {
    it('should return unnested nettotarpeet', () => {
      const et = {
        tulokset: {
          nettotarve: {
            'tilojen-lammitys-vuosikulutus': Either.Right(Maybe.Some(1)),
            'ilmanvaihdon-lammitys-vuosikulutus': Either.Right(Maybe.Some(1)),
            'kayttoveden-valmistus-vuosikulutus': Either.Right(Maybe.Some(1)),
            'jaahdytys-vuosikulutus': Either.Right(Maybe.Some(1))
          }
        }
      };

      const expected = {
        'tilojen-lammitys-vuosikulutus': Maybe.Some(1),
        'ilmanvaihdon-lammitys-vuosikulutus': Maybe.Some(1),
        'kayttoveden-valmistus-vuosikulutus': Maybe.Some(1),
        'jaahdytys-vuosikulutus': Maybe.Some(1)
      };

      expect(expected).toEqual(EtUtils.nettotarpeet(et));
    });
  });

  describe('kuormat', () => {
    it('should return unnested kuormat', () => {
      const et = {
        tulokset: {
          lampokuormat: {
            aurinko: Either.Right(Maybe.Some(1)),
            ihmiset: Either.Right(Maybe.Some(1)),
            kuluttajalaitteet: Either.Right(Maybe.Some(1)),
            valaistus: Either.Right(Maybe.Some(1)),
            kvesi: Either.Right(Maybe.Some(1))
          }
        }
      };

      const expected = {
        aurinko: Maybe.Some(1),
        ihmiset: Maybe.Some(1),
        kuluttajalaitteet: Maybe.Some(1),
        valaistus: Maybe.Some(1),
        kvesi: Maybe.Some(1)
      };

      expect(expected).toEqual(EtUtils.kuormat(et));
    });
  });

  describe('ostetutEnergiamuodot', () => {
    it('should return unnested ostetut energiamuodot', () => {
      const et = {
        'toteutunut-ostoenergiankulutus': {
          'ostettu-energia': {
            'kaukolampo-vuosikulutus': Either.Right(Maybe.Some(1)),
            'kokonaissahko-vuosikulutus': Either.Right(Maybe.Some(1)),
            'kiinteistosahko-vuosikulutus': Either.Right(Maybe.Some(1)),
            'kayttajasahko-vuosikulutus': Either.Right(Maybe.Some(1)),
            'kaukojaahdytys-vuosikulutus': Either.Right(Maybe.Some(1))
          }
        }
      };

      const expected = {
        'kaukolampo-vuosikulutus': Maybe.Some(1),
        'kokonaissahko-vuosikulutus': Maybe.Some(1),
        'kiinteistosahko-vuosikulutus': Maybe.Some(1),
        'kayttajasahko-vuosikulutus': Maybe.Some(1),
        'kaukojaahdytys-vuosikulutus': Maybe.Some(1)
      };

      expect(expected).toEqual(EtUtils.ostetutEnergiamuodot(et));
    });
  });

  describe('toteutuneetOstoenergiat', () => {
    it('should return unnested toteutuneet ostoenergiat', () => {
      const et = {
        'toteutunut-ostoenergiankulutus': {
          'sahko-vuosikulutus-yhteensa': Either.Right(Maybe.Some(1)),
          'kaukolampo-vuosikulutus-yhteensa': Either.Right(Maybe.Some(1)),
          'polttoaineet-vuosikulutus-yhteensa': Either.Right(Maybe.Some(1)),
          'kaukojaahdytys-vuosikulutus-yhteensa': Either.Right(Maybe.Some(1))
        }
      };

      const expected = {
        'sahko-vuosikulutus-yhteensa': Maybe.Some(1),
        'kaukolampo-vuosikulutus-yhteensa': Maybe.Some(1),
        'polttoaineet-vuosikulutus-yhteensa': Maybe.Some(1),
        'kaukojaahdytys-vuosikulutus-yhteensa': Maybe.Some(1)
      };

      expect(expected).toEqual(EtUtils.toteutuneetOstoenergiat(2018)(et));
    });
  });

  describe('polttoaineet', () => {
    it('should return unnested toteutuneet ostoenergiat', () => {
      const et = {
        'toteutunut-ostoenergiankulutus': {
          'ostetut-polttoaineet': {
            'kevyt-polttooljy': Either.Right(Maybe.Some(1)),
            'pilkkeet-havu-sekapuu': Either.Right(Maybe.Some(1)),
            'pilkkeet-koivu': Either.Right(Maybe.Some(1)),
            puupelletit: Either.Right(Maybe.Some(1))
          }
        }
      };

      const expected = {
        'kevyt-polttooljy': Maybe.Some(1),
        'pilkkeet-havu-sekapuu': Maybe.Some(1),
        'pilkkeet-koivu': Maybe.Some(1),
        puupelletit: Maybe.Some(1)
      };

      expect(expected).toEqual(EtUtils.polttoaineet(et));
    });
  });

  describe('vapaatPolttoaineet', () => {
    it('should return unnested toteutuneet ostoenergiat', () => {
      const et = {
        'toteutunut-ostoenergiankulutus': {
          'ostetut-polttoaineet': {
            muu: [
              {
                'maara-vuodessa': Either.Right(Maybe.Some(1))
              },
              {
                'maara-vuodessa': Either.Right(Maybe.Some(2))
              },
              {
                'maara-vuodessa': Either.Right(Maybe.Some(3))
              },
              {
                'maara-vuodessa': Either.Right(Maybe.Some(4))
              }
            ]
          }
        }
      };

      const expected = [
        Maybe.Some(1),
        Maybe.Some(2),
        Maybe.Some(3),
        Maybe.Some(4)
      ];

      expect(expected).toEqual(EtUtils.vapaatPolttoaineet(et));
    });
  });

  describe('vapaatKertoimet', () => {
    it('should return unnested toteutuneet ostoenergiat', () => {
      const et = {
        'toteutunut-ostoenergiankulutus': {
          'ostetut-polttoaineet': {
            muu: [
              {
                muunnoskerroin: Either.Right(Maybe.Some(1))
              },
              {
                muunnoskerroin: Either.Right(Maybe.Some(2))
              },
              {
                muunnoskerroin: Either.Right(Maybe.Some(3))
              },
              {
                muunnoskerroin: Either.Right(Maybe.Some(4))
              }
            ]
          }
        }
      };

      const expected = [
        Maybe.Some(1),
        Maybe.Some(2),
        Maybe.Some(3),
        Maybe.Some(4)
      ];

      expect(expected).toEqual(EtUtils.vapaatKertoimet(et));
    });
  });
});

describe('e-luku calculations', () => {
  describe('complete e-luku calculation', () => {
    describe('Only fossiilinen polttoaine', () => {
      it('works okay with everything filled', () => {
        const energiamuodot = {
          'fossiilinen-polttoaine': Either.Right(Maybe.Some(10000)),
          sahko: Either.Right(Maybe.Some(0)),
          kaukojaahdytys: Either.Right(Maybe.Some(0)),
          kaukolampo: Either.Right(Maybe.Some(0)),
          'uusiutuva-polttoaine': Either.Right(Maybe.Some(0))
        };

        const e_luku = EtUtils.eluku(
          2018,
          Either.Right(Maybe.Some(100)),
          energiamuodot
        );
        expect(e_luku).toEqual(Maybe.Some(100));
      });

      it('works okay with None in unused fields', () => {
        const energiamuodot = {
          'fossiilinen-polttoaine': Either.Right(Maybe.Some(10000)),
          sahko: Either.Right(Maybe.None()),
          kaukojaahdytys: Either.Right(Maybe.None()),
          kaukolampo: Either.Right(Maybe.None()),
          'uusiutuva-polttoaine': Either.Right(Maybe.None())
        };

        const e_luku = EtUtils.eluku(
          2018,
          Either.Right(Maybe.Some(100)),
          energiamuodot
        );
        expect(e_luku).toEqual(Maybe.Some(100));
      });

      it('None nettoala produces a None', () => {
        const energiamuodot = {
          'fossiilinen-polttoaine': Either.Right(Maybe.Some(10000)),
          sahko: Either.Right(Maybe.None()),
          kaukojaahdytys: Either.Right(Maybe.None()),
          kaukolampo: Either.Right(Maybe.None()),
          'uusiutuva-polttoaine': Either.Right(Maybe.None())
        };

        const e_luku = EtUtils.eluku(
          2018,
          Either.Right(Maybe.None()),
          energiamuodot
        );
        expect(e_luku).toEqual(Maybe.None());
      });

      it('produces the correct value for a more complicated input', () => {
        const energiamuodot = {
          'fossiilinen-polttoaine': Either.Right(Maybe.Some(10000)),
          sahko: Either.Right(Maybe.Some(10000)),
          kaukojaahdytys: Either.Right(Maybe.Some(10000)),
          kaukolampo: Either.Right(Maybe.Some(10000)),
          'uusiutuva-polttoaine': Either.Right(Maybe.Some(10000))
        };

        const e_luku = EtUtils.eluku(
          2018,
          Either.Right(Maybe.Some(100)),
          energiamuodot
        );
        expect(e_luku).toEqual(Maybe.Some(348));
      });
    });
  });
});

// === AE-2614: ET2026 energiamuotokertoimet ===

describe('ET2026 energiamuotokertoimet', () => {
  // 3.1 - Correct 2026 coefficient values
  describe('energiamuotokertoimet returns correct 2026 values', () => {
    it('should have the new 2026 coefficients', () => {
      // Given the energiamuotokertoimet function
      // When we retrieve 2026 coefficients
      const kertoimet = EtUtils.energiamuotokertoimet()[2026];

      // Then all values match the new regulation
      expect(kertoimet['fossiilinen-polttoaine']).toEqual(Maybe.Some(1));
      expect(kertoimet.sahko).toEqual(Maybe.Some(0.9));
      expect(kertoimet.kaukolampo).toEqual(Maybe.Some(0.38));
      expect(kertoimet.kaukojaahdytys).toEqual(Maybe.Some(0.21));
      expect(kertoimet['uusiutuva-polttoaine']).toEqual(Maybe.Some(0.38));
    });
  });

  // 3.2 - Regression: 2018 values unchanged
  describe('energiamuotokertoimet 2018 values unchanged', () => {
    it('should still have the original 2018 coefficients', () => {
      // Given the energiamuotokertoimet function
      // When we retrieve 2018 coefficients
      const kertoimet = EtUtils.energiamuotokertoimet()[2018];

      // Then all values remain as they were
      expect(kertoimet.sahko).toEqual(Maybe.Some(1.2));
      expect(kertoimet.kaukolampo).toEqual(Maybe.Some(0.5));
      expect(kertoimet.kaukojaahdytys).toEqual(Maybe.Some(0.28));
      expect(kertoimet['fossiilinen-polttoaine']).toEqual(Maybe.Some(1));
      expect(kertoimet['uusiutuva-polttoaine']).toEqual(Maybe.Some(0.5));
    });
  });

  // 3.3 - E-luku with 2026: fossiilinen-polttoaine only
  describe('eluku calculation with 2026 coefficients', () => {
    it('fossiilinen-polttoaine only produces correct result', () => {
      // Given only fossiilinen-polttoaine at 10000, nettoala=100
      const energiamuodot = {
        'fossiilinen-polttoaine': Either.Right(Maybe.Some(10000)),
        sahko: Either.Right(Maybe.Some(0)),
        kaukojaahdytys: Either.Right(Maybe.Some(0)),
        kaukolampo: Either.Right(Maybe.Some(0)),
        'uusiutuva-polttoaine': Either.Right(Maybe.Some(0))
      };

      // When E-luku is calculated with 2026 coefficients
      const e_luku = EtUtils.eluku(
        2026,
        Either.Right(Maybe.Some(100)),
        energiamuodot
      );

      // Then result = ceil(10000 * 1.0 / 100) = 100
      expect(e_luku).toEqual(Maybe.Some(100));
    });

    // 3.4 - E-luku with 2026: all energy forms
    it('all energy forms produces the correct 2026 result (differs from 2018)', () => {
      // Given all energy forms at 10000, nettoala=100
      const energiamuodot = {
        'fossiilinen-polttoaine': Either.Right(Maybe.Some(10000)),
        sahko: Either.Right(Maybe.Some(10000)),
        kaukojaahdytys: Either.Right(Maybe.Some(10000)),
        kaukolampo: Either.Right(Maybe.Some(10000)),
        'uusiutuva-polttoaine': Either.Right(Maybe.Some(10000))
      };

      // When E-luku is calculated with 2026 coefficients
      const e_luku = EtUtils.eluku(
        2026,
        Either.Right(Maybe.Some(100)),
        energiamuodot
      );

      // Then result = ceil((10000*(1+0.90+0.21+0.38+0.38)) / 100) = ceil(28700/100) = 287
      // (this differs from 2018 result of 348)
      expect(e_luku).toEqual(Maybe.Some(287));
    });

    // 3.5 - E-luku with 2026: realistic nettoala
    it('realistic nettoala produces correct ceiling-rounded result', () => {
      // Given a pientalo: sahko=5000, kaukolampo=12000, nettoala=150
      const energiamuodot = {
        'fossiilinen-polttoaine': Either.Right(Maybe.Some(0)),
        sahko: Either.Right(Maybe.Some(5000)),
        kaukojaahdytys: Either.Right(Maybe.Some(0)),
        kaukolampo: Either.Right(Maybe.Some(12000)),
        'uusiutuva-polttoaine': Either.Right(Maybe.Some(0))
      };

      // When E-luku is calculated with 2026 coefficients
      const e_luku = EtUtils.eluku(
        2026,
        Either.Right(Maybe.Some(150)),
        energiamuodot
      );

      // Then painotettu = 5000*0.90 + 12000*0.38 = 4500 + 4560 = 9060
      //      E-luku = ceil(9060/150) = ceil(60.4) = 61
      expect(e_luku).toEqual(Maybe.Some(61));
    });

    // 3.6 - None nettoala
    it('None nettoala produces None with 2026 version', () => {
      // Given valid energy forms but None nettoala
      const energiamuodot = {
        'fossiilinen-polttoaine': Either.Right(Maybe.Some(10000)),
        sahko: Either.Right(Maybe.None()),
        kaukojaahdytys: Either.Right(Maybe.None()),
        kaukolampo: Either.Right(Maybe.None()),
        'uusiutuva-polttoaine': Either.Right(Maybe.None())
      };

      // When E-luku is calculated
      const e_luku = EtUtils.eluku(
        2026,
        Either.Right(Maybe.None()),
        energiamuodot
      );

      // Then result is None
      expect(e_luku).toEqual(Maybe.None());
    });

    // 3.7 - None energy values
    it('None energy values with 2026 version produces zero', () => {
      // Given all energy forms are None, nettoala=100
      const energiamuodot = {
        'fossiilinen-polttoaine': Either.Right(Maybe.None()),
        sahko: Either.Right(Maybe.None()),
        kaukojaahdytys: Either.Right(Maybe.None()),
        kaukolampo: Either.Right(Maybe.None()),
        'uusiutuva-polttoaine': Either.Right(Maybe.None())
      };

      // When E-luku is calculated with 2026 coefficients
      const e_luku = EtUtils.eluku(
        2026,
        Either.Right(Maybe.Some(100)),
        energiamuodot
      );

      // Then result is 0
      expect(e_luku).toEqual(Maybe.Some(0));
    });
  });
});

// ============================================================
// Työvaihe 3: applyEluokkaDowngrade — frontend downgrade logic
// ============================================================

describe('applyEluokkaDowngrade', () => {
  // 1.1 A+ raw e-luokka with different vaatimusrasti combinations
  describe('A+ raw e-luokka', () => {
    it('both vaatimukset true → returns A+', () => {
      // Given: rawEluokka is "A+"
      // When: both aplus and a0 are true
      // Then: returns "A+" unchanged
      expect(EtUtils.applyEluokkaDowngrade('A+', true, true)).toBe('A+');
    });

    it('aplus=true, a0=false → returns A (A+ requires both)', () => {
      // Given: rawEluokka is "A+"
      // When: aplus true but a0 false
      // Then: downgraded to "A"
      expect(EtUtils.applyEluokkaDowngrade('A+', true, false)).toBe('A');
    });

    it('aplus=false, a0=true → returns A0', () => {
      // Given: rawEluokka is "A+"
      // When: aplus false but a0 true
      // Then: downgraded to "A0"
      expect(EtUtils.applyEluokkaDowngrade('A+', false, true)).toBe('A0');
    });

    it('both vaatimukset false → returns A', () => {
      // Given: rawEluokka is "A+"
      // When: both false
      // Then: downgraded to "A"
      expect(EtUtils.applyEluokkaDowngrade('A+', false, false)).toBe('A');
    });
  });

  // 1.2 A0 raw e-luokka with different vaatimusrasti combinations
  describe('A0 raw e-luokka', () => {
    it('a0=true → returns A0', () => {
      // Given: rawEluokka is "A0"
      // When: a0 is true
      // Then: returns "A0"
      expect(EtUtils.applyEluokkaDowngrade('A0', false, true)).toBe('A0');
    });

    it('a0=false → returns A', () => {
      // Given: rawEluokka is "A0"
      // When: a0 is false
      // Then: downgraded to "A"
      expect(EtUtils.applyEluokkaDowngrade('A0', false, false)).toBe('A');
    });

    it('aplus=true, a0=true → returns A0 (aplus irrelevant for A0)', () => {
      // Given: rawEluokka is "A0"
      // When: both true
      // Then: returns "A0" (aplus rasti is irrelevant for A0 area)
      expect(EtUtils.applyEluokkaDowngrade('A0', true, true)).toBe('A0');
    });

    it('aplus=true, a0=false → returns A', () => {
      // Given: rawEluokka is "A0"
      // When: aplus true but a0 false
      // Then: downgraded to "A"
      expect(EtUtils.applyEluokkaDowngrade('A0', true, false)).toBe('A');
    });
  });

  // 1.3 Other e-luokka values: no-op
  describe('other e-luokka values are never downgraded', () => {
    it('A remains A', () => {
      expect(EtUtils.applyEluokkaDowngrade('A', true, true)).toBe('A');
    });

    it('B remains B', () => {
      expect(EtUtils.applyEluokkaDowngrade('B', false, false)).toBe('B');
    });

    it('C remains C', () => {
      expect(EtUtils.applyEluokkaDowngrade('C', true, true)).toBe('C');
    });

    it('D remains D', () => {
      expect(EtUtils.applyEluokkaDowngrade('D', false, false)).toBe('D');
    });

    it('E remains E', () => {
      expect(EtUtils.applyEluokkaDowngrade('E', true, false)).toBe('E');
    });

    it('F remains F', () => {
      expect(EtUtils.applyEluokkaDowngrade('F', false, true)).toBe('F');
    });

    it('G remains G', () => {
      expect(EtUtils.applyEluokkaDowngrade('G', true, true)).toBe('G');
    });
  });

  // 1.4 Edge cases
  describe('edge cases', () => {
    it('undefined rawEluokka → returns undefined without crashing', () => {
      // Given: rawEluokka is undefined
      // When: called with any rasti combination
      // Then: returns undefined (does not throw)
      expect(
        EtUtils.applyEluokkaDowngrade(undefined, true, true)
      ).toBeUndefined();
    });

    it('empty string rawEluokka → returns empty string', () => {
      // Given: rawEluokka is ""
      // When: called with any rasti combination
      // Then: returns "" unchanged
      expect(EtUtils.applyEluokkaDowngrade('', true, true)).toBe('');
    });
  });
});

// 1.5 eluokkaFromRajaAsteikko + applyEluokkaDowngrade combined
describe('eluokkaFromRajaAsteikko + applyEluokkaDowngrade combined', () => {
  // 2026 YAT raja-asteikko: A+ ≤ 78, A0 ≤ 98, A ≤ 98, B ≤ 106, C ≤ 130, D ≤ 181, E ≤ 265, F ≤ 310
  const rajaAsteikko2026 = [
    [78, 'A+'],
    [98, 'A0'],
    [98, 'A'],
    [106, 'B'],
    [130, 'C'],
    [181, 'D'],
    [265, 'E'],
    [310, 'F']
  ];

  it('e-luku in A+ range, both true → A+', () => {
    // Given: e-luku 50 is in A+ range (≤ 78)
    const raw = EtUtils.eluokkaFromRajaAsteikko(rajaAsteikko2026, 50);
    // When: both vaatimukset true
    // Then: chain produces "A+"
    expect(EtUtils.applyEluokkaDowngrade(raw, true, true)).toBe('A+');
  });

  it('e-luku in A+ range, both false → A', () => {
    // Given: e-luku 50 is in A+ range
    const raw = EtUtils.eluokkaFromRajaAsteikko(rajaAsteikko2026, 50);
    // When: both false
    // Then: chain produces "A" (downgraded)
    expect(EtUtils.applyEluokkaDowngrade(raw, false, false)).toBe('A');
  });

  it('e-luku in A0 range, a0=true → A0', () => {
    // Given: e-luku 85 is in A0 range (> 78, ≤ 98)
    const raw = EtUtils.eluokkaFromRajaAsteikko(rajaAsteikko2026, 85);
    // When: a0 true
    // Then: chain produces "A0"
    expect(EtUtils.applyEluokkaDowngrade(raw, false, true)).toBe('A0');
  });

  it('e-luku in B range → B regardless of rastit', () => {
    // Given: e-luku 100 is in B range (> 98, ≤ 106)
    const raw = EtUtils.eluokkaFromRajaAsteikko(rajaAsteikko2026, 100);
    // When: both true
    // Then: chain produces "B" unchanged
    expect(EtUtils.applyEluokkaDowngrade(raw, true, true)).toBe('B');
  });
});
