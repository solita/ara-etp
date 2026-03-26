import { expect, describe, it } from '@jest/globals';
import * as R from 'ramda';
import * as Schema from './schema';
import fi from '@Language/fi.json';
import sv from '@Language/sv.json';

const dummySchemaObject = R.curry((type, key) => ({
  type,
  key
}));

describe('EtHakuSchema', () => {
  describe('flattenSchema', () => {
    it('should flatten given schema with 1 item', () => {
      const schema = {
        perustiedot: {
          id: [dummySchemaObject('NUMBER')]
        }
      };

      const expected = {
        'perustiedot.id': [
          {
            key: 'perustiedot.id',
            type: 'NUMBER'
          }
        ]
      };

      expect(Schema.flattenSchema(schema)).toEqual(expected);
    });

    it('should flatten given schema with multiple items in single branch', () => {
      const schema = {
        perustiedot: {
          id: [dummySchemaObject('NUMBER')],
          nimi: [dummySchemaObject('STRING')],
          osoite: [dummySchemaObject('STRING')]
        }
      };

      const expected = {
        'perustiedot.id': [
          {
            key: 'perustiedot.id',
            type: 'NUMBER'
          }
        ],
        'perustiedot.nimi': [
          {
            key: 'perustiedot.nimi',
            type: 'STRING'
          }
        ],
        'perustiedot.osoite': [
          {
            key: 'perustiedot.osoite',
            type: 'STRING'
          }
        ]
      };

      expect(Schema.flattenSchema(schema)).toEqual(expected);
    });

    it('should flatten given schema with multiple items in branching branch', () => {
      const schema = {
        perustiedot: {
          id: [dummySchemaObject('NUMBER')],
          branch: {
            nimi: [dummySchemaObject('STRING')],
            osoite: [dummySchemaObject('STRING')]
          }
        }
      };

      const expected = {
        'perustiedot.id': [
          {
            key: 'perustiedot.id',
            type: 'NUMBER'
          }
        ],
        'perustiedot.branch.nimi': [
          {
            key: 'perustiedot.branch.nimi',
            type: 'STRING'
          }
        ],
        'perustiedot.branch.osoite': [
          {
            key: 'perustiedot.branch.osoite',
            type: 'STRING'
          }
        ]
      };

      expect(Schema.flattenSchema(schema)).toEqual(expected);
    });

    it('should flatten given schema with multiple items in multiple branches', () => {
      const schema = {
        perustiedot: {
          id: [dummySchemaObject('NUMBER')],
          branch: {
            nimi: [dummySchemaObject('STRING')],
            osoite: [dummySchemaObject('STRING')]
          }
        },
        laajemmattiedot: {
          id: [dummySchemaObject('STRING')],
          branch: {
            nimi: [dummySchemaObject('STRING')],
            osoite: [dummySchemaObject('STRING')],
            puhelinnumero: {
              suuntanumero: [dummySchemaObject('NUMBER')],
              numero: [dummySchemaObject('NUMBER')]
            }
          }
        }
      };

      const expected = {
        'perustiedot.id': [
          {
            key: 'perustiedot.id',
            type: 'NUMBER'
          }
        ],
        'perustiedot.branch.nimi': [
          {
            key: 'perustiedot.branch.nimi',
            type: 'STRING'
          }
        ],
        'perustiedot.branch.osoite': [
          {
            key: 'perustiedot.branch.osoite',
            type: 'STRING'
          }
        ],
        'laajemmattiedot.id': [
          {
            key: 'laajemmattiedot.id',
            type: 'STRING'
          }
        ],
        'laajemmattiedot.branch.nimi': [
          {
            key: 'laajemmattiedot.branch.nimi',
            type: 'STRING'
          }
        ],
        'laajemmattiedot.branch.osoite': [
          {
            key: 'laajemmattiedot.branch.osoite',
            type: 'STRING'
          }
        ],
        'laajemmattiedot.branch.puhelinnumero.suuntanumero': [
          {
            key: 'laajemmattiedot.branch.puhelinnumero.suuntanumero',
            type: 'NUMBER'
          }
        ],
        'laajemmattiedot.branch.puhelinnumero.numero': [
          {
            key: 'laajemmattiedot.branch.puhelinnumero.numero',
            type: 'NUMBER'
          }
        ]
      };

      expect(Schema.flattenSchema(schema)).toEqual(expected);
    });

    it('should work with multiple operations', () => {
      const schema = {
        perustiedot: {
          id: [dummySchemaObject('NUMBER'), dummySchemaObject('STRING')]
        }
      };

      const expected = {
        'perustiedot.id': [
          {
            key: 'perustiedot.id',
            type: 'NUMBER'
          },
          {
            key: 'perustiedot.id',
            type: 'STRING'
          }
        ]
      };

      expect(Schema.flattenSchema(schema)).toEqual(expected);
    });
  });

  describe('ET2026 new search fields', () => {
    const flatSchema = Schema.flattenSchema(Schema.schema);

    it('flattenSchema includes havainnointikayntityyppi-id field', () => {
      // Given: the real schema
      const key =
        'energiatodistus.perustiedot.havainnointikayntityyppi-id';

      // When: flattenSchema is called on the real schema
      // Then: result contains the key with type HAVAINNOINTIKAYNTITYYPPI
      expect(flatSchema).toHaveProperty([key]);
      expect(flatSchema[key][0].type).toBe(
        Schema.OPERATOR_TYPES.HAVAINNOINTIKAYNTITYYPPI
      );
    });

    it('flattenSchema includes energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin', () => {
      // Given: the real schema
      const key =
        'energiatodistus.lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin';

      // When: flattenSchema is called on the real schema
      // Then: result contains the key with type BOOLEAN
      expect(flatSchema).toHaveProperty([key]);
      expect(flatSchema[key][0].type).toBe(Schema.OPERATOR_TYPES.BOOLEAN);
    });

    it('flattenSchema includes lammonjako-lampotilajousto', () => {
      // Given: the real schema
      const key =
        'energiatodistus.lahtotiedot.lammitys.lammonjako-lampotilajousto';

      // When: flattenSchema is called on the real schema
      // Then: result contains the key with type BOOLEAN
      expect(flatSchema).toHaveProperty([key]);
      expect(flatSchema[key][0].type).toBe(Schema.OPERATOR_TYPES.BOOLEAN);
    });

    it('flattenSchema includes all 6 kokonaistuotanto fields', () => {
      // Given: the real schema and the 6 kokonaistuotanto sub-fields
      const kokonaistuotantoFields = [
        'aurinkosahko',
        'aurinkolampo',
        'tuulisahko',
        'lampopumppu',
        'muulampo',
        'muusahko'
      ];

      // When: flattenSchema is called on the real schema
      // Then: each kokonaistuotanto field exists with 5 numeric comparison operations
      kokonaistuotantoFields.forEach(field => {
        const key = `energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.${field}`;
        expect(flatSchema).toHaveProperty([key]);
        expect(flatSchema[key]).toHaveLength(5);
        expect(flatSchema[key][0].type).toBe(Schema.OPERATOR_TYPES.NUMBER);
      });
    });

    it('flattenSchema includes kasvihuonepaastot-per-nelio field', () => {
      // Given: the real schema
      const key = 'energiatodistus.tulokset.kasvihuonepaastot-per-nelio';

      // When: flattenSchema is called on the real schema
      // Then: result contains the key with numeric comparisons
      expect(flatSchema).toHaveProperty([key]);
      expect(flatSchema[key]).toHaveLength(5);
      expect(flatSchema[key][0].type).toBe(Schema.OPERATOR_TYPES.NUMBER);
    });

    it('flattenSchema includes uusiutuvan-energian-osuus field', () => {
      // Given: the real schema
      const key = 'energiatodistus.tulokset.uusiutuvan-energian-osuus';

      // When: flattenSchema is called on the real schema
      // Then: result contains the key with numeric comparisons
      expect(flatSchema).toHaveProperty([key]);
      expect(flatSchema[key]).toHaveLength(5);
      expect(flatSchema[key][0].type).toBe(Schema.OPERATOR_TYPES.NUMBER);
    });
  });

  describe('paakayttajaSchema with ET2026 fields', () => {
    it('paakayttajaSchema includes all new ET2026 fields', () => {
      // Given: all new ET2026 field keys
      const newKeys = [
        'energiatodistus.perustiedot.havainnointikayntityyppi-id',
        'energiatodistus.lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin',
        'energiatodistus.lahtotiedot.lammitys.lammonjako-lampotilajousto',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkolampo',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.tuulisahko',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.lampopumppu',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.muulampo',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.muusahko',
        'energiatodistus.tulokset.kasvihuonepaastot-per-nelio',
        'energiatodistus.tulokset.uusiutuvan-energian-osuus'
      ];

      // When: checking paakayttajaSchema
      // Then: all new keys exist
      newKeys.forEach(key => {
        expect(Schema.paakayttajaSchema).toHaveProperty([key]);
      });
    });
  });

  describe('laatijaSchema with ET2026 fields', () => {
    // REGRESSION: laatijaSchema restricts lahtotiedot to only lammitetty-nettoala
    // and tulokset to only e-luku and e-luokka, so new fields should NOT appear
    it('laatijaSchema does NOT include new ET2026 fields', () => {
      // Given: all new ET2026 field keys
      const newKeys = [
        'energiatodistus.perustiedot.havainnointikayntityyppi-id',
        'energiatodistus.lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin',
        'energiatodistus.lahtotiedot.lammitys.lammonjako-lampotilajousto',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkolampo',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.tuulisahko',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.lampopumppu',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.muulampo',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.muusahko',
        'energiatodistus.tulokset.kasvihuonepaastot-per-nelio',
        'energiatodistus.tulokset.uusiutuvan-energian-osuus'
      ];

      // When: checking laatijaSchema
      // Then: none of the new keys exist
      newKeys.forEach(key => {
        expect(Schema.laatijaSchema).not.toHaveProperty([key]);
      });
    });
  });

  describe('OPERATOR_TYPES', () => {
    it('includes HAVAINNOINTIKAYNTITYYPPI value', () => {
      // Given: OPERATOR_TYPES
      // When: checking for HAVAINNOINTIKAYNTITYYPPI
      // Then: it is a defined, non-empty string value
      expect(Schema.OPERATOR_TYPES.HAVAINNOINTIKAYNTITYYPPI).toBeTruthy();
      expect(typeof Schema.OPERATOR_TYPES.HAVAINNOINTIKAYNTITYYPPI).toBe(
        'string'
      );
    });

    // REGRESSION: OPERATOR_TYPES should remain frozen after extension
    it('is still frozen after extension', () => {
      // Given: OPERATOR_TYPES object
      // When: checking if frozen
      // Then: it is frozen
      expect(Object.isFrozen(Schema.OPERATOR_TYPES)).toBe(true);
    });
  });

  describe('New field operators and defaults', () => {
    const flatSchema = Schema.flattenSchema(Schema.schema);

    it('havainnointikayntityyppi-id uses = operator with luokittelu pattern', () => {
      // Given: the havainnointikayntityyppi-id field from the real schema
      const key =
        'energiatodistus.perustiedot.havainnointikayntityyppi-id';

      // When: examining the field operations
      const operations = flatSchema[key];

      // Then: it uses = operator and luokitteluDefault pattern (returns [0])
      expect(operations).toBeDefined();
      expect(operations).toHaveLength(1);
      expect(operations[0].operation.serverCommand).toBe('=');
      expect(operations[0].defaultValues()).toEqual([0]);
    });

    it('boolean fields use singleBoolean pattern', () => {
      // Given: the boolean ET2026 fields
      const booleanKeys = [
        'energiatodistus.lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin',
        'energiatodistus.lahtotiedot.lammitys.lammonjako-lampotilajousto'
      ];

      // When: examining each field
      // Then: each uses BOOLEAN type with defaultValues [true]
      booleanKeys.forEach(key => {
        const operations = flatSchema[key];
        expect(operations).toBeDefined();
        expect(operations).toHaveLength(1);
        expect(operations[0].type).toBe(Schema.OPERATOR_TYPES.BOOLEAN);
        expect(operations[0].defaultValues()).toEqual([true]);
      });
    });

    it('kokonaistuotanto fields use numberComparisons', () => {
      // Given: the 6 kokonaistuotanto fields
      const fields = [
        'aurinkosahko',
        'aurinkolampo',
        'tuulisahko',
        'lampopumppu',
        'muulampo',
        'muusahko'
      ];

      // When: examining each field
      // Then: each has 5 operations (=, >, >=, <, <=) with NUMBER type
      fields.forEach(field => {
        const key = `energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.${field}`;
        const operations = flatSchema[key];
        expect(operations).toBeDefined();
        expect(operations).toHaveLength(5);
        const serverCommands = operations.map(
          op => op.operation.serverCommand
        );
        expect(serverCommands).toEqual(['=', '>', '>=', '<', '<=']);
      });
    });

    it('kasvihuonepaastot-per-nelio uses numberComparisons', () => {
      // Given: the kasvihuonepaastot-per-nelio field
      const key = 'energiatodistus.tulokset.kasvihuonepaastot-per-nelio';

      // When: examining the field operations
      const operations = flatSchema[key];

      // Then: it has 5 numeric operations
      expect(operations).toBeDefined();
      expect(operations).toHaveLength(5);
      const serverCommands = operations.map(
        op => op.operation.serverCommand
      );
      expect(serverCommands).toEqual(['=', '>', '>=', '<', '<=']);
    });

    it('uusiutuvan-energian-osuus uses numberComparisons', () => {
      // Given: the uusiutuvan-energian-osuus field
      const key = 'energiatodistus.tulokset.uusiutuvan-energian-osuus';

      // When: examining the field operations
      const operations = flatSchema[key];

      // Then: it has 5 numeric operations
      expect(operations).toBeDefined();
      expect(operations).toHaveLength(5);
      const serverCommands = operations.map(
        op => op.operation.serverCommand
      );
      expect(serverCommands).toEqual(['=', '>', '>=', '<', '<=']);
    });
  });

  describe('Localization', () => {
    const getI18nLabel = (lang, fieldKey) => {
      // Navigate the JSON structure using the dot-separated field key
      // e.g. 'energiatodistus.perustiedot.havainnointikayntityyppi-id'
      // -> lang['energiatodistus']['perustiedot']['havainnointikayntityyppi-id']
      const parts = fieldKey.split('.');
      return R.path(parts, lang);
    };

    const newFieldKeys = [
      'energiatodistus.perustiedot.havainnointikayntityyppi-id',
      'energiatodistus.lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin',
      'energiatodistus.lahtotiedot.lammitys.lammonjako-lampotilajousto',
      'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko',
      'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkolampo',
      'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.tuulisahko',
      'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.lampopumppu',
      'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.muulampo',
      'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.muusahko',
      'energiatodistus.tulokset.kasvihuonepaastot-per-nelio',
      'energiatodistus.tulokset.uusiutuvan-energian-osuus'
    ];

    it('all new search fields have fi.json localization keys', () => {
      // Given: fi.json and all new ET2026 field keys
      // When: looking up each key in the fi.json structure
      // Then: each key resolves to a non-empty string label
      newFieldKeys.forEach(key => {
        const label = getI18nLabel(fi, key);
        expect(label).toBeDefined();
        expect(typeof label).toBe('string');
        expect(label.length).toBeGreaterThan(0);
      });
    });

    it('all new search fields have sv.json localization keys with (sv) suffix', () => {
      // Given: fi.json and sv.json with all new ET2026 field keys
      // When: looking up each key in the sv.json structure
      // Then: each key resolves to the fi value plus ' (sv)' suffix
      newFieldKeys.forEach(key => {
        const fiLabel = getI18nLabel(fi, key);
        const svLabel = getI18nLabel(sv, key);
        expect(svLabel).toBeDefined();
        expect(svLabel).toBe(`${fiLabel} (sv)`);
      });
    });
  });
});
