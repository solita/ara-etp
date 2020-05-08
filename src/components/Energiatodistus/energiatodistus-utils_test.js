import { assert } from 'chai';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';

import * as EtUtils from './energiatodistus-utils';

describe('Energiatodistus Utils: ', () => {
  describe('unnestValidation', () => {
    it('should return value within Either', () => {
      const value = Either.Right(Maybe.Some(1));
      const expected = Maybe.Some(1);

      assert.deepEqual(expected, EtUtils.unnestValidation(value));
    });

    it('should return none on Either.Left', () => {
      const value = Either.Left(1);
      const expected = Maybe.None();

      assert.deepEqual(expected, EtUtils.unnestValidation(value));
    });

    it('should return none on Either.Left', () => {
      const value = Either.Left(1);
      const expected = Maybe.None();

      assert.deepEqual(expected, EtUtils.unnestValidation(value));
    });

    it('should return Identity with non-either value', () => {
      const value = Maybe.Some(1);
      const expected = Maybe.Some(1);

      assert.deepEqual(expected, EtUtils.unnestValidation(value));
    });
  });

  describe('energiatodistusPath', () => {
    it('should return unnested value from path', () => {
      const obj = { a: { b: Either.Right(Maybe.Some(1)) } };
      const path = ['a', 'b'];

      const expected = Maybe.Some(1);

      assert.deepEqual(expected, EtUtils.energiatodistusPath(path, obj));
    });

    it('should return value when non-either', () => {
      const obj = { a: { b: Maybe.Some(1) } };
      const path = ['a', 'b'];

      const expected = Maybe.Some(1);

      assert.deepEqual(expected, EtUtils.energiatodistusPath(path, obj));
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

      assert.deepEqual(
        expected,
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

      assert.deepEqual(
        expected,
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

      assert.deepEqual(
        expected,
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

      assert.deepEqual(expected, EtUtils.teknistenJarjestelmienSahkot(et));
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

      assert.deepEqual(expected, EtUtils.teknistenJarjestelmienLammot(et));
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

      assert.deepEqual(
        expected,
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

      assert.deepEqual(expected, EtUtils.sumEtValues(values));
    });
  });

  describe('partOfSum', () => {
    it('should calculate part of sum', () => {
      const sum = Maybe.Some(2);
      const value = Maybe.Some(1);

      const expected = Maybe.Some(0.5);

      assert.deepEqual(expected, EtUtils.partOfSum(sum, value));
    });

    it('should return None when input is None', () => {
      const sum = Maybe.None();
      const value = Maybe.Some(1);

      const expected = Maybe.None();

      assert.deepEqual(expected, EtUtils.partOfSum(sum, value));
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

      assert.deepEqual(expected, EtUtils.rakennusvaippaUA(et));
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

      assert.deepEqual(expected, EtUtils.ostoenergiat(et));
    });
  });

  describe('multiplyOstoenergia', () => {
    it('should return Some with proper values', () => {
      const ostoenergiamaara = Maybe.Some(10);
      const kerroin = 2;

      const expected = Maybe.Some(20);

      assert.deepEqual(
        expected,
        EtUtils.multiplyOstoenergia(kerroin, ostoenergiamaara)
      );
    });

    it('should return None with None ostoenergiamaara', () => {
      const ostoenergiamaara = Maybe.None();
      const kerroin = 2;

      const expected = Maybe.None();

      assert.deepEqual(
        expected,
        EtUtils.multiplyOstoenergia(kerroin, ostoenergiamaara)
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

      assert.deepEqual(expected, EtUtils.perLammitettyNettoala(et, values));
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

      assert.deepEqual(expected, EtUtils.perLammitettyNettoala(et, values));
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

      assert.deepEqual(expected, EtUtils.perLammitettyNettoala(et, values));
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

      assert.deepEqual(expected, EtUtils.omavaraisenergiat(et));
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

      assert.deepEqual(expected, EtUtils.nettotarpeet(et));
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

      assert.deepEqual(expected, EtUtils.kuormat(et));
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

      assert.deepEqual(expected, EtUtils.ostetutEnergiamuodot(et));
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

      assert.deepEqual(expected, EtUtils.toteutuneetOstoenergiat(et));
    });
  });
});
