import { expect, describe, it } from '@jest/globals';
import * as R from 'ramda';
import * as Schema from './schema';
import {
  OPERATOR_TYPES,
  flatSchema,
  paakayttajaSchema,
  laatijaSchema
} from './schema';

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
});

describe('Schema ET2026 search fields:', () => {
  // 1.1 - paakayttajaSchema contains ET2026 search fields

  describe('paakayttajaSchema contains ET2026 fields', () => {
    it('should contain perustiedot.havainnointikayntityyppi-id', () => {
      // Given the paakayttajaSchema
      // When checking for havainnointikayntityyppi-id
      // Then it should be present
      expect(
        paakayttajaSchema[
          'energiatodistus.perustiedot.havainnointikayntityyppi-id'
        ]
      ).toBeDefined();
    });

    it('should contain lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin', () => {
      expect(
        paakayttajaSchema[
          'energiatodistus.lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin'
        ]
      ).toBeDefined();
    });

    it('should contain lahtotiedot.lammitys.lammonjako-lampotilajousto', () => {
      expect(
        paakayttajaSchema[
          'energiatodistus.lahtotiedot.lammitys.lammonjako-lampotilajousto'
        ]
      ).toBeDefined();
    });

    it('should contain tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto fields', () => {
      // Given the paakayttajaSchema
      // When checking for kokonaistuotanto sub-fields
      // Then all six fields should be present
      const fields = [
        'aurinkosahko',
        'aurinkolampo',
        'tuulisahko',
        'lampopumppu',
        'muulampo',
        'muusahko'
      ];
      fields.forEach(field => {
        expect(
          paakayttajaSchema[
            `energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.${field}`
          ]
        ).toBeDefined();
      });
    });

    it('should contain toteutunut-ostoenergiankulutus new fields', () => {
      const fields = [
        'tietojen-alkuperavuosi',
        'uusiutuvat-polttoaineet-vuosikulutus-yhteensa',
        'fossiiliset-polttoaineet-vuosikulutus-yhteensa',
        'uusiutuva-energia-vuosituotto-yhteensa'
      ];
      fields.forEach(field => {
        expect(
          paakayttajaSchema[
            `energiatodistus.toteutunut-ostoenergiankulutus.${field}`
          ]
        ).toBeDefined();
      });
    });

    it('should contain ilmastoselvitys basic fields', () => {
      const fields = [
        'laatimisajankohta',
        'laatija',
        'yritys',
        'yritys-osoite',
        'yritys-postinumero',
        'yritys-postitoimipaikka',
        'laadintaperuste'
      ];
      fields.forEach(field => {
        expect(
          paakayttajaSchema[`energiatodistus.ilmastoselvitys.${field}`]
        ).toBeDefined();
      });
    });

    it('should contain ilmastoselvitys.hiilijalanjalki fields', () => {
      // Given the paakayttajaSchema
      // When checking for hiilijalanjalki sub-fields
      // Then representative fields should be present
      expect(
        paakayttajaSchema[
          'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennus.rakennustuotteiden-valmistus'
        ]
      ).toBeDefined();
      expect(
        paakayttajaSchema[
          'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennuspaikka.energiankaytto'
        ]
      ).toBeDefined();
    });

    it('should contain ilmastoselvitys.hiilikadenjalki fields', () => {
      expect(
        paakayttajaSchema[
          'energiatodistus.ilmastoselvitys.hiilikadenjalki.rakennus.uudelleenkaytto'
        ]
      ).toBeDefined();
      expect(
        paakayttajaSchema[
          'energiatodistus.ilmastoselvitys.hiilikadenjalki.rakennuspaikka.hiilivarastovaikutus'
        ]
      ).toBeDefined();
    });

    it('should contain huomiot.lammitys.kayttoikaa-jaljella-arvio-vuosina', () => {
      expect(
        paakayttajaSchema[
          'energiatodistus.huomiot.lammitys.kayttoikaa-jaljella-arvio-vuosina'
        ]
      ).toBeDefined();
    });

    it('should contain kasvihuonepaastojen-muutos for huomiot toimenpide fields', () => {
      // Given the paakayttajaSchema
      // When checking for kasvihuonepaastojen-muutos in each huomio category
      // Then at least one representative field should be present
      const categories = [
        'iv-ilmastointi',
        'valaistus-muut',
        'lammitys',
        'ymparys',
        'alapohja-ylapohja'
      ];
      categories.forEach(category => {
        expect(
          paakayttajaSchema[
            `energiatodistus.huomiot.${category}.toimenpide.0.kasvihuonepaastojen-muutos`
          ]
        ).toBeDefined();
      });
    });
  });

  // 1.2 - laatijaSchema contains ilmastoselvitys basic fields

  describe('laatijaSchema contains ilmastoselvitys basic fields', () => {
    const allowedFields = [
      'laatimisajankohta',
      'laatija',
      'yritys',
      'yritys-osoite',
      'yritys-postinumero',
      'yritys-postitoimipaikka'
    ];

    allowedFields.forEach(field => {
      it(`should contain energiatodistus.ilmastoselvitys.${field}`, () => {
        // Given the laatijaSchema
        // When checking for allowed ilmastoselvitys field
        // Then it should be present
        expect(
          laatijaSchema[`energiatodistus.ilmastoselvitys.${field}`]
        ).toBeDefined();
      });
    });
  });

  // 1.3 - laatijaSchema does NOT contain other ET2026 fields

  describe('laatijaSchema does NOT contain admin-only ET2026 fields', () => {
    it('should NOT contain perustiedot.havainnointikayntityyppi-id', () => {
      // Given the laatijaSchema
      // When checking for admin-only fields
      // Then they should NOT be present
      expect(
        laatijaSchema[
          'energiatodistus.perustiedot.havainnointikayntityyppi-id'
        ]
      ).toBeUndefined();
    });

    it('should NOT contain lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin', () => {
      expect(
        laatijaSchema[
          'energiatodistus.lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin'
        ]
      ).toBeUndefined();
    });

    it('should NOT contain lahtotiedot.lammitys.lammonjako-lampotilajousto', () => {
      expect(
        laatijaSchema[
          'energiatodistus.lahtotiedot.lammitys.lammonjako-lampotilajousto'
        ]
      ).toBeUndefined();
    });

    it('should NOT contain tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto fields', () => {
      expect(
        laatijaSchema[
          'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko'
        ]
      ).toBeUndefined();
    });

    it('should NOT contain ilmastoselvitys.hiilijalanjalki fields', () => {
      expect(
        laatijaSchema[
          'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennus.rakennustuotteiden-valmistus'
        ]
      ).toBeUndefined();
    });

    it('should NOT contain ilmastoselvitys.hiilikadenjalki fields', () => {
      expect(
        laatijaSchema[
          'energiatodistus.ilmastoselvitys.hiilikadenjalki.rakennus.uudelleenkaytto'
        ]
      ).toBeUndefined();
    });

    it('should NOT contain ilmastoselvitys.laadintaperuste', () => {
      expect(
        laatijaSchema['energiatodistus.ilmastoselvitys.laadintaperuste']
      ).toBeUndefined();
    });

    it('should NOT contain huomiot kasvihuonepaastojen-muutos fields', () => {
      expect(
        laatijaSchema[
          'energiatodistus.huomiot.iv-ilmastointi.toimenpide.0.kasvihuonepaastojen-muutos'
        ]
      ).toBeUndefined();
    });
  });

  // 1.4 - Operator types are correct for new fields

  describe('Operator types for ET2026 fields', () => {
    it('should have BOOLEAN type for energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin', () => {
      // Given the flatSchema
      // When checking the operator type for the boolean field
      // Then it should be BOOLEAN
      const ops =
        flatSchema[
          'energiatodistus.lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin'
        ];
      expect(ops).toBeDefined();
      expect(ops[0].type).toBe(OPERATOR_TYPES.BOOLEAN);
    });

    it('should have BOOLEAN type for lammonjako-lampotilajousto', () => {
      const ops =
        flatSchema[
          'energiatodistus.lahtotiedot.lammitys.lammonjako-lampotilajousto'
        ];
      expect(ops).toBeDefined();
      expect(ops[0].type).toBe(OPERATOR_TYPES.BOOLEAN);
    });

    it('should have DATE type for ilmastoselvitys.laatimisajankohta', () => {
      const ops =
        flatSchema['energiatodistus.ilmastoselvitys.laatimisajankohta'];
      expect(ops).toBeDefined();
      expect(ops[0].type).toBe(OPERATOR_TYPES.DATE);
    });

    it('should have STRING type for ilmastoselvitys.laatija', () => {
      const ops = flatSchema['energiatodistus.ilmastoselvitys.laatija'];
      expect(ops).toBeDefined();
      expect(ops[0].type).toBe(OPERATOR_TYPES.STRING);
    });

    it('should have NUMBER type for hiilijalanjalki numeric fields', () => {
      const ops =
        flatSchema[
          'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennus.rakennustuotteiden-valmistus'
        ];
      expect(ops).toBeDefined();
      expect(ops[0].type).toBe(OPERATOR_TYPES.NUMBER);
    });
  });
});
