import { assert } from 'chai';
import * as Maybe from '@Utility/maybe-utils';
import * as EtHakuUtils from './energiatodistus-haku-utils';
import { flatSchema } from './schema';

describe('EtHakuUtils:', () => {
  describe('blockToQueryParameter', () => {
    it('should return Just query parameter for given block', () => {
      const block = ['sisaltaa', 'perustiedot.nimi', 'asdf'];
      const expected = Maybe.Some([
        ['like', 'energiatodistus.perustiedot.nimi', '%asdf%']
      ]);

      assert.deepEqual(
        EtHakuUtils.blockToQueryParameter(flatSchema, block),
        expected
      );
    });

    it('should return proper query parameter for given block', () => {
      const block = ['no-operation-named-this', 'nimi', 'asdf'];
      const expected = Maybe.None();

      assert.deepEqual(
        EtHakuUtils.blockToQueryParameter(flatSchema, block),
        expected
      );
    });
  });

  describe('convertWhereToQuery', () => {
    it('should convert Where to Query', () => {
      const where = [
        [
          ['sisaltaa', 'perustiedot.nimi', 'asdf'],
          ['=', 'id', 2]
        ],
        [['>', 'id', 'value']]
      ];
      const expected = [
        [
          ['like', 'energiatodistus.perustiedot.nimi', '%asdf%'],
          ['=', 'energiatodistus.id', 2]
        ],
        [['>', 'energiatodistus.id', 'value']]
      ];

      assert.deepEqual(
        EtHakuUtils.convertWhereToQuery(flatSchema, where),
        expected
      );
    });

    it('should remove empty blocks when converting Where to Query', () => {
      const where = [
        [
          ['sisaltaa', 'perustiedot.nimi', 'asdf'],
          ['no-operation-named-this', 'id', 2]
        ],
        [['no-operation-named-this', 'key', 'value']]
      ];
      const expected = [
        [['like', 'energiatodistus.perustiedot.nimi', '%asdf%']]
      ];

      assert.deepEqual(
        EtHakuUtils.convertWhereToQuery(flatSchema, where),
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

    assert.deepEqual(
      EtHakuUtils.deserializeConjuntionBlockPair(pair),
      expected
    );
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

      assert.deepEqual(EtHakuUtils.deserializeAndBlocks(andBlocks), expected);
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

      assert.deepEqual(EtHakuUtils.deserializeWhere(schema, where), expected);
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

      assert.deepEqual(EtHakuUtils.deserializeWhere(schema, where), expected);
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

      assert.deepEqual(EtHakuUtils.removeQueryItem(2, queryItems), expected);
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

      assert.deepEqual(EtHakuUtils.removeQueryItem(0, queryItems), expected);
    });
  });
});
