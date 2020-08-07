import { assert } from 'chai';
import * as Maybe from '@Utility/maybe-utils';
import * as EtHakuUtils from './energiatodistus-haku-utils';

describe('EtHakuUtils:', () => {
  describe('findOperation', () => {
    it('should return Some when operation is found', () => {
      const operation = 'sisaltaa';
      const operations = EtHakuUtils.laatijaKriteerit();

      assert.isTrue(
        Maybe.exists(
          m => m.operation === operation,
          EtHakuUtils.findOperation(operation, operations)
        )
      );
    });
    it('should return None when operation is not found', () => {
      const operation = 'no-operation-named-this';
      const operations = EtHakuUtils.laatijaKriteerit();

      assert.deepEqual(
        EtHakuUtils.findOperation(operation, operations),
        Maybe.None()
      );
    });
  });

  describe('blockToQueryParameter', () => {
    it('should return Just query parameter for given block', () => {
      const block = ['sisaltaa', 'nimi', 'asdf'];
      const expected = Maybe.Some(['like', 'nimi', '%asdf%']);

      assert.deepEqual(EtHakuUtils.blockToQueryParameter(block), expected);
    });

    it('should return proper query parameter for given block', () => {
      const block = ['no-operation-named-this', 'nimi', 'asdf'];
      const expected = Maybe.None();

      assert.deepEqual(EtHakuUtils.blockToQueryParameter(block), expected);
    });
  });

  describe('convertWhereToQuery', () => {
    it('should convert Where to Query', () => {
      const where = [
        [
          ['sisaltaa', 'nimi', 'asdf'],
          ['=', 'id', 2]
        ],
        [['>', 'key', 'value']]
      ];
      const expected = [
        [
          ['like', 'nimi', '%asdf%'],
          ['=', 'id', 2]
        ],
        [['>', 'key', 'value']]
      ];

      assert.deepEqual(EtHakuUtils.convertWhereToQuery(where), expected);
    });

    it('should remove empty blocks when converting Where to Query', () => {
      const where = [
        [
          ['sisaltaa', 'nimi', 'asdf'],
          ['no-operation-named-this', 'id', 2]
        ],
        [['no-operation-named-this', 'key', 'value']]
      ];
      const expected = [[['like', 'nimi', '%asdf%']]];

      assert.deepEqual(EtHakuUtils.convertWhereToQuery(where), expected);
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
        [Maybe.Some('or'), ['=', 'key', 'value']],
        [Maybe.Some('and'), ['>', 'key', 'value']],
        [Maybe.Some('and'), ['<', 'key', 'value']]
      ];

      assert.deepEqual(EtHakuUtils.deserializeAndBlocks(andBlocks), expected);
    });
  });

  describe('deserializeWhere', () => {
    it('should return deserialized where', () => {
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
        { conjunction: Maybe.None(), block: ['sisaltaa', 'nimi', 'asdf'] },
        { conjunction: Maybe.Some('and'), block: ['=', 'id', 2] },
        { conjunction: Maybe.Some('or'), block: ['>', 'key', 'value'] },
        { conjunction: Maybe.Some('and'), block: ['>=', 'key', 'value'] }
      ];

      assert.deepEqual(EtHakuUtils.deserializeWhere(where), expected);
    });
  });

  describe('serializeWhere', () => {
    it('should return serialized where', () => {
      const queryItems = [
        { conjunction: Maybe.None(), block: ['sisaltaa', 'nimi', 'asdf'] },
        { conjunction: Maybe.Some('and'), block: ['=', 'id', 2] },
        { conjunction: Maybe.Some('or'), block: ['>', 'key', 'value'] },
        { conjunction: Maybe.Some('and'), block: ['>=', 'key', 'value'] }
      ];

      const expected = [
        [
          ['sisaltaa', 'nimi', 'asdf'],
          ['=', 'id', 2]
        ],
        [
          ['>', 'key', 'value'],
          ['>=', 'key', 'value']
        ]
      ];

      assert.deepEqual(EtHakuUtils.serializeWhere(queryItems), expected);
    });
  });

  describe('removeQueryItem', () => {
    it('should remove given index', () => {
      const queryItems = [
        { conjunction: Maybe.None(), block: ['sisaltaa', 'nimi', 'asdf'] },
        { conjunction: Maybe.Some('and'), block: ['=', 'id', 2] },
        { conjunction: Maybe.Some('or'), block: ['>', 'key', 'value'] },
        { conjunction: Maybe.Some('and'), block: ['>=', 'key', 'value'] }
      ];

      const expected = [
        { conjunction: Maybe.None(), block: ['sisaltaa', 'nimi', 'asdf'] },
        { conjunction: Maybe.Some('and'), block: ['=', 'id', 2] },
        { conjunction: Maybe.Some('and'), block: ['>=', 'key', 'value'] }
      ];

      assert.deepEqual(EtHakuUtils.removeQueryItem(2, queryItems), expected);
    });

    it('should set new first item conjuntion None on removal', () => {
      const queryItems = [
        { conjunction: Maybe.None(), block: ['sisaltaa', 'nimi', 'asdf'] },
        { conjunction: Maybe.Some('and'), block: ['=', 'id', 2] },
        { conjunction: Maybe.Some('or'), block: ['>', 'key', 'value'] },
        { conjunction: Maybe.Some('and'), block: ['>=', 'key', 'value'] }
      ];

      const expected = [
        { conjunction: Maybe.None(), block: ['=', 'id', 2] },
        { conjunction: Maybe.Some('or'), block: ['>', 'key', 'value'] },
        { conjunction: Maybe.Some('and'), block: ['>=', 'key', 'value'] }
      ];

      assert.deepEqual(EtHakuUtils.removeQueryItem(0, queryItems), expected);
    });
  });

  describe('appendDefaultQueryItem', () => {
    it('should append defaultQueryItem', () => {
      const queryItems = [
        { conjunction: Maybe.None(), block: ['sisaltaa', 'nimi', 'asdf'] },
        { conjunction: Maybe.Some('and'), block: ['=', 'id', 2] },
        { conjunction: Maybe.Some('or'), block: ['>', 'key', 'value'] },
        { conjunction: Maybe.Some('and'), block: ['>=', 'key', 'value'] }
      ];

      const expected = [
        { conjunction: Maybe.None(), block: ['sisaltaa', 'nimi', 'asdf'] },
        { conjunction: Maybe.Some('and'), block: ['=', 'id', 2] },
        { conjunction: Maybe.Some('or'), block: ['>', 'key', 'value'] },
        { conjunction: Maybe.Some('and'), block: ['>=', 'key', 'value'] },
        { conjunction: Maybe.Some('and'), block: ['=', 'id', 0] }
      ];

      assert.deepEqual(
        EtHakuUtils.appendDefaultQueryItem(queryItems),
        expected
      );
    });
  });

  describe('findFromKriteeritByKey', () => {
    it('should return Some kriteeri with given key', () => {
      const kriteerit = EtHakuUtils.laatijaKriteerit();
      const key = 'id';

      assert.isTrue(
        Maybe.isSome(EtHakuUtils.findFromKriteeritByKey(kriteerit, key))
      );
    });

    it('should return None kriteeri when not finding with given key', () => {
      const kriteerit = EtHakuUtils.laatijaKriteerit();
      const key = 'key-that-does-not-exist';

      assert.isTrue(
        Maybe.isNone(EtHakuUtils.findFromKriteeritByKey(kriteerit, key))
      );
    });
  });
});
