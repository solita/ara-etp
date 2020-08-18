import * as R from 'ramda';
import { assert } from 'chai';
import * as Schema from './schema';

const dummySchemaObject = R.curry((type, key) => ({
  type,
  key
}));

describe('EtHakuSchema', () => {
  describe('flattenSchema', () => {
    it('should flatten given schema with 1 item', () => {
      const schema = {
        perustiedot: {
          id: dummySchemaObject('NUMBER')
        }
      };

      const expected = [
        {
          key: 'perustiedot.id',
          type: 'NUMBER'
        }
      ];

      assert.deepEqual(Schema.flattenSchema('', schema), expected);
    });

    it('should flatten given schema with multiple items in single branch', () => {
      const schema = {
        perustiedot: {
          id: dummySchemaObject('NUMBER'),
          nimi: dummySchemaObject('STRING'),
          osoite: dummySchemaObject('STRING')
        }
      };

      const expected = [
        {
          key: 'perustiedot.id',
          type: 'NUMBER'
        },
        {
          key: 'perustiedot.nimi',
          type: 'STRING'
        },
        {
          key: 'perustiedot.osoite',
          type: 'STRING'
        }
      ];

      assert.deepEqual(Schema.flattenSchema('', schema), expected);
    });

    it('should flatten given schema with multiple items in branching branch', () => {
      const schema = {
        perustiedot: {
          id: dummySchemaObject('NUMBER'),
          branch: {
            nimi: dummySchemaObject('STRING'),
            osoite: dummySchemaObject('STRING')
          }
        }
      };

      const expected = [
        {
          key: 'perustiedot.id',
          type: 'NUMBER'
        },
        {
          key: 'perustiedot.branch.nimi',
          type: 'STRING'
        },
        {
          key: 'perustiedot.branch.osoite',
          type: 'STRING'
        }
      ];

      assert.deepEqual(Schema.flattenSchema('', schema), expected);
    });

    it('should flatten given schema with multiple items in multiple branches', () => {
      const schema = {
        perustiedot: {
          id: dummySchemaObject('NUMBER'),
          branch: {
            nimi: dummySchemaObject('STRING'),
            osoite: dummySchemaObject('STRING')
          }
        },
        laajemmattiedot: {
          id: dummySchemaObject('STRING'),
          branch: {
            nimi: dummySchemaObject('STRING'),
            osoite: dummySchemaObject('STRING'),
            puhelinnumero: {
              suuntanumero: dummySchemaObject('NUMBER'),
              numero: dummySchemaObject('NUMBER')
            }
          }
        }
      };

      const expected = [
        {
          key: 'perustiedot.id',
          type: 'NUMBER'
        },
        {
          key: 'perustiedot.branch.nimi',
          type: 'STRING'
        },
        {
          key: 'perustiedot.branch.osoite',
          type: 'STRING'
        },
        {
          key: 'laajemmattiedot.id',
          type: 'STRING'
        },
        {
          key: 'laajemmattiedot.branch.nimi',
          type: 'STRING'
        },
        {
          key: 'laajemmattiedot.branch.osoite',
          type: 'STRING'
        },
        {
          key: 'laajemmattiedot.branch.puhelinnumero.suuntanumero',
          type: 'NUMBER'
        },
        {
          key: 'laajemmattiedot.branch.puhelinnumero.numero',
          type: 'NUMBER'
        }
      ];

      assert.deepEqual(Schema.flattenSchema('', schema), expected);
    });
  });
});
