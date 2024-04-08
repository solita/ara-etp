import { expect, describe, it } from '@jest/globals';
import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';
import * as EtHakuUtils from './energiatodistus-haku-utils';
import { OPERATOR_TYPES, flatSchema } from './schema';

describe('EtHakuUtils:', () => {
  describe('blockToQueryParameter', () => {
    it('should return None on empty input', () => {
      const block = ['sisaltaa', 'energiatodistus.perustiedot.nimi-fi', ''];
      const expected = Maybe.None();

      expect(EtHakuUtils.blockToQueryParameter(flatSchema, block)).toEqual(
        expected
      );
    });
    it('should return Just query parameter for given block', () => {
      const block = ['sisaltaa', 'energiatodistus.perustiedot.nimi-fi', 'asdf'];
      const expected = Maybe.Some([
        ['ilike', 'energiatodistus.perustiedot.nimi-fi', '%asdf%']
      ]);

      expect(EtHakuUtils.blockToQueryParameter(flatSchema, block)).toEqual(
        expected
      );
    });

    it('should return proper query parameter for given block', () => {
      const block = ['no-operation-named-this', 'nimi', 'asdf'];
      const expected = Maybe.None();

      expect(EtHakuUtils.blockToQueryParameter(flatSchema, block)).toEqual(
        expected
      );
    });
  });

  describe('convertWhereToQuery', () => {
    it('should convert Where to Query', () => {
      const where = [
        [
          ['sisaltaa', 'energiatodistus.perustiedot.nimi-fi', 'asdf'],
          ['=', 'energiatodistus.id', 2]
        ],
        [['>', 'energiatodistus.id', 'value']]
      ];
      const expected = [
        [
          ['ilike', 'energiatodistus.perustiedot.nimi-fi', '%asdf%'],
          ['=', 'energiatodistus.id', 2]
        ],
        [['>', 'energiatodistus.id', 'value']]
      ];

      expect(EtHakuUtils.convertWhereToQuery(flatSchema, where)).toEqual(
        expected
      );
    });

    it('should remove empty blocks when converting Where to Query', () => {
      const where = [
        [
          ['sisaltaa', 'energiatodistus.perustiedot.nimi-fi', 'asdf'],
          ['no-operation-named-this', 'id', 2]
        ],
        [['no-operation-named-this', 'key', 'value']]
      ];
      const expected = [
        [['ilike', 'energiatodistus.perustiedot.nimi-fi', '%asdf%']]
      ];

      expect(EtHakuUtils.convertWhereToQuery(flatSchema, where)).toEqual(
        expected
      );
    });
  });

  describe('deserializeConjuntionBlockPair', () => {
    const pair = [Maybe.Some('and'), ['=', 'key', 'value']];
    const expected = {
      conjunction: Maybe.Some('and'),
      block: ['=', 'key', 'value']
    };

    expect(EtHakuUtils.deserializeConjuntionBlockPair(pair)).toEqual(expected);
  });

  describe('deserializeAndBlocks', () => {
    it('should return deserialized and-blocks with first conjunction as or', () => {
      const andBlocks = [
        ['=', 'key', 'value'],
        ['>', 'key', 'value'],
        ['<', 'key', 'value']
      ];
      const expected = [
        ['or', ['=', 'key', 'value']],
        ['and', ['>', 'key', 'value']],
        ['and', ['<', 'key', 'value']]
      ];

      expect(EtHakuUtils.deserializeAndBlocks(andBlocks)).toEqual(expected);
    });
  });

  describe('deserializeWhere', () => {
    it('should return deserialized where', () => {
      const schema = {
        nimi: true,
        id: true,
        key: true
      };

      const where = [
        [
          ['sisaltaa', 'nimi', 'asdf'],
          ['=', 'id', 2]
        ],
        [
          ['>', 'key', 'value'],
          ['>=', 'key', 'value']
        ]
      ];

      const expected = [
        { conjunction: 'and', block: ['sisaltaa', 'nimi', 'asdf'] },
        { conjunction: 'and', block: ['=', 'id', 2] },
        { conjunction: 'or', block: ['>', 'key', 'value'] },
        { conjunction: 'and', block: ['>=', 'key', 'value'] }
      ];

      expect(EtHakuUtils.deserializeWhere(schema, where)).toEqual(expected);
    });

    it('should remove not found criteria', () => {
      const schema = {
        nimi: true,
        id: true,
        key: true
      };

      const where = [
        [
          ['sisaltaa', 'notfound', 'asdf'],
          ['=', 'id', 2]
        ],
        [
          ['>', 'key', 'value'],
          ['>=', 'key', 'value']
        ]
      ];

      const expected = [
        { conjunction: 'and', block: ['=', 'id', 2] },
        { conjunction: 'or', block: ['>', 'key', 'value'] },
        { conjunction: 'and', block: ['>=', 'key', 'value'] }
      ];

      expect(EtHakuUtils.deserializeWhere(schema, where)).toEqual(expected);
    });
  });

  describe('removeQueryItem', () => {
    it('should remove given index', () => {
      const queryItems = [
        { conjunction: 'and', block: ['sisaltaa', 'nimi', 'asdf'] },
        { conjunction: 'and', block: ['=', 'id', 2] },
        { conjunction: 'or', block: ['>', 'key', 'value'] },
        { conjunction: 'and', block: ['>=', 'key', 'value'] }
      ];

      const expected = [
        { conjunction: 'and', block: ['sisaltaa', 'nimi', 'asdf'] },
        { conjunction: 'and', block: ['=', 'id', 2] },
        { conjunction: 'and', block: ['>=', 'key', 'value'] }
      ];

      expect(EtHakuUtils.removeQueryItem(2, queryItems)).toEqual(expected);
    });

    it('should set new first item conjuntion None on removal', () => {
      const queryItems = [
        { conjunction: 'and', block: ['sisaltaa', 'nimi', 'asdf'] },
        { conjunction: 'and', block: ['=', 'id', 2] },
        { conjunction: 'or', block: ['>', 'key', 'value'] },
        { conjunction: 'and', block: ['>=', 'key', 'value'] }
      ];

      const expected = [
        { conjunction: 'and', block: ['=', 'id', 2] },
        { conjunction: 'or', block: ['>', 'key', 'value'] },
        { conjunction: 'and', block: ['>=', 'key', 'value'] }
      ];

      expect(EtHakuUtils.removeQueryItem(0, queryItems)).toEqual(expected);
    });
  });

  describe('parseValueByType', () => {
    it('should return Right with proper number type', () => {
      const types = [
        OPERATOR_TYPES.NUMBER,
        OPERATOR_TYPES.UNFORMATTED_NUMBER,
        OPERATOR_TYPES.VERSIO,
        OPERATOR_TYPES.TILA,
        OPERATOR_TYPES.LAATIJA,
        OPERATOR_TYPES.LAATIMISVAIHE,
        OPERATOR_TYPES.KIELISYYS,
        OPERATOR_TYPES.ILMANVAIHTOTYYPPI,
        OPERATOR_TYPES.PATEVYYSTASO
      ];

      const value = '1';

      expect(
        R.all(
          Either.isRight,
          R.map(EtHakuUtils.parseValueByType(R.__, value), types)
        )
      ).toBe(true);
    });
    it('should return Left with invalid number', () => {
      const types = [
        OPERATOR_TYPES.NUMBER,
        OPERATOR_TYPES.UNFORMATTED_NUMBER,
        OPERATOR_TYPES.VERSIO,
        OPERATOR_TYPES.TILA,
        OPERATOR_TYPES.LAATIJA,
        OPERATOR_TYPES.LAATIMISVAIHE,
        OPERATOR_TYPES.KIELISYYS,
        OPERATOR_TYPES.ILMANVAIHTOTYYPPI,
        OPERATOR_TYPES.PATEVYYSTASO
      ];

      const value = 'a';

      expect(
        R.all(
          Either.isLeft,
          R.map(EtHakuUtils.parseValueByType(R.__, value), types)
        )
      ).toBe(true);
    });

    it('should return Right with daycount', () => {
      expect(
        Either.isRight(
          EtHakuUtils.parseValueByType(OPERATOR_TYPES.DAYCOUNT, '1d')
        )
      ).toBe(true);
    });
    it('should return Left with invalid daycount', () => {
      expect(
        Either.isLeft(
          EtHakuUtils.parseValueByType(OPERATOR_TYPES.DAYCOUNT, 'dd')
        )
      ).toBe(true);
    });

    it('should return Right with percent', () => {
      expect(
        Either.isRight(
          EtHakuUtils.parseValueByType(OPERATOR_TYPES.PERCENT, '54%')
        )
      ).toBe(true);
    });

    it('should return Left with invalid percent', () => {
      expect(
        Either.isLeft(
          EtHakuUtils.parseValueByType(OPERATOR_TYPES.PERCENT, 'a%')
        )
      ).toBe(true);
    });

    it('should return Right with boolean', () => {
      expect(
        EtHakuUtils.parseValueByType(OPERATOR_TYPES.BOOLEAN, 'true')
      ).toEqual(Either.Right(true));
      expect(
        EtHakuUtils.parseValueByType(OPERATOR_TYPES.BOOLEAN, 'false')
      ).toEqual(Either.Right(false));
    });

    it('should return Right with e-luokka', () => {
      expect(
        EtHakuUtils.parseValueByType(OPERATOR_TYPES.ELUOKKA, '1,2,3')
      ).toEqual(Either.Right(['1', '2', '3']));
    });

    it('should return Right no type', () => {
      expect(EtHakuUtils.parseValueByType('', '1,2,3')).toEqual(
        Either.Right('1,2,3')
      );
    });
  });

  describe('hakuCriteriaFromGroupedInput', () => {
    it('should return haku criteria', () => {
      const inputs = [
        ['conjunction', 'and'],
        ['key', 'energiatodistus.id'],
        ['type', OPERATOR_TYPES.NUMBER],
        ['operation', '='],
        ['value', '0', '4']
      ];

      const expected = {
        conjunction: 'and',
        block: ['=', 'energiatodistus.id', 4]
      };

      expect(EtHakuUtils.hakuCriteriaFromGroupedInput(inputs)).toEqual(
        expected
      );
    });

    it('should return haku criteria with multiple values in right order', () => {
      const inputs = [
        ['conjunction', 'and'],
        ['key', 'some_key'],
        ['type', OPERATOR_TYPES.NUMBER],
        ['operation', 'between'],
        ['value', '0', '4'],
        ['value', '3', '1'],
        ['value', '2', '2'],
        ['value', '1', '3']
      ];

      const expected = {
        conjunction: 'and',
        block: ['between', 'some_key', 4, 3, 2, 1]
      };

      expect(EtHakuUtils.hakuCriteriaFromGroupedInput(inputs)).toEqual(
        expected
      );
    });
  });

  describe('searchString', () => {
    it('should return search string based on criteria', () => {
      const criteria = [
        { conjunction: 'and', block: ['<', 'key1', 4] },
        { conjunction: 'and', block: ['=', 'key2', 3] },
        { conjunction: 'or', block: ['between', 'key3', 1, 4] }
      ];

      const expected = `[[["<","key1",4],["=","key2",3]],[["between","key3",1,4]]]`;

      expect(EtHakuUtils.searchString(criteria)).toEqual(expected);
    });
  });
});
