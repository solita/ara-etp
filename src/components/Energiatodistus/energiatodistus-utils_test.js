import { assert } from 'chai';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';

import * as EtUtils from './energiatodistus-utils';

describe.only('Energiatodistus Utils: ', () => {
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
});
