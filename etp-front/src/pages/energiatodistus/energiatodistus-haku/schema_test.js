import { expect, describe, it } from '@jest/globals';
import * as R from 'ramda';
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

// === AE-2622: ET2026-kenttien lisääminen energiatodistushakuun ===

describe('ET2026 search schema — flatSchema contains new fields', () => {
  // Test 1: flatSchema contains ilmastoselvitys fields
  describe('ilmastoselvitys fields', () => {
    it('given flatSchema, when checking for ilmastoselvitys numeric fields, then they exist with NUMBER type', () => {
      const numericFields = [
        'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennus.energiankaytto',
        'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennus.rakennustuotteiden-valmistus',
        'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennus.kuljetukset-tyomaavaihe',
        'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennus.rakennustuotteiden-vaihdot',
        'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennus.purkuvaihe',
        'energiatodistus.ilmastoselvitys.hiilikadenjalki.rakennus.uudelleenkaytto',
        'energiatodistus.ilmastoselvitys.hiilikadenjalki.rakennus.kierratys',
        'energiatodistus.ilmastoselvitys.hiilikadenjalki.rakennus.ylimaarainen-uusiutuvaenergia',
        'energiatodistus.ilmastoselvitys.hiilikadenjalki.rakennus.hiilivarastovaikutus',
        'energiatodistus.ilmastoselvitys.hiilikadenjalki.rakennus.karbonatisoituminen',
        'energiatodistus.ilmastoselvitys.laadintaperuste'
      ];

      numericFields.forEach(field => {
        expect(Schema.flatSchema).toHaveProperty([field]);
        expect(Schema.flatSchema[field][0].type).toBe(
          Schema.OPERATOR_TYPES.NUMBER
        );
      });
    });

    it('given flatSchema, when checking for ilmastoselvitys string fields, then they exist with STRING type', () => {
      const stringFields = [
        'energiatodistus.ilmastoselvitys.laatija',
        'energiatodistus.ilmastoselvitys.yritys',
        'energiatodistus.ilmastoselvitys.yritys-osoite',
        'energiatodistus.ilmastoselvitys.yritys-postinumero',
        'energiatodistus.ilmastoselvitys.yritys-postitoimipaikka'
      ];

      stringFields.forEach(field => {
        expect(Schema.flatSchema).toHaveProperty([field]);
        expect(Schema.flatSchema[field][0].type).toBe(
          Schema.OPERATOR_TYPES.STRING
        );
      });
    });

    it('given flatSchema, when checking for ilmastoselvitys date field, then it exists with DATE type', () => {
      expect(Schema.flatSchema).toHaveProperty(
        ['energiatodistus.ilmastoselvitys.laatimisajankohta']
      );
      expect(
        Schema.flatSchema[
          'energiatodistus.ilmastoselvitys.laatimisajankohta'
        ][0].type
      ).toBe(Schema.OPERATOR_TYPES.DATE);
    });
  });

  // Test 2: flatSchema contains havainnointikayntityyppi-id
  it('given flatSchema, when checking for havainnointikayntityyppi-id, then it exists with HAVAINNOINTIKAYNTITYYPPI type', () => {
    const key =
      'energiatodistus.perustiedot.havainnointikayntityyppi-id';
    expect(Schema.flatSchema).toHaveProperty([key]);
    expect(Schema.flatSchema[key][0].type).toBe(
      Schema.OPERATOR_TYPES.HAVAINNOINTIKAYNTITYYPPI
    );
  });

  // Test 3: flatSchema contains uusiutuvat-omavaraisenergiat-kokonaistuotanto fields
  it('given flatSchema, when checking for kokonaistuotanto fields, then they exist with NUMBER type', () => {
    const fields = [
      'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko',
      'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkolampo',
      'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.tuulisahko',
      'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.lampopumppu',
      'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.muulampo',
      'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.muusahko'
    ];

    fields.forEach(field => {
      expect(Schema.flatSchema).toHaveProperty([field]);
      expect(Schema.flatSchema[field][0].type).toBe(
        Schema.OPERATOR_TYPES.NUMBER
      );
    });
  });

  // Test 4: flatSchema contains toteutunut-ostoenergiankulutus new fields
  describe('toteutunut-ostoenergiankulutus new fields', () => {
    it('given flatSchema, when checking for numeric new fields, then they exist with NUMBER type', () => {
      const numericFields = [
        'energiatodistus.toteutunut-ostoenergiankulutus.tietojen-alkuperavuosi',
        'energiatodistus.toteutunut-ostoenergiankulutus.uusiutuvat-polttoaineet-vuosikulutus-yhteensa',
        'energiatodistus.toteutunut-ostoenergiankulutus.fossiiliset-polttoaineet-vuosikulutus-yhteensa',
        'energiatodistus.toteutunut-ostoenergiankulutus.uusiutuva-energia-vuosituotto-yhteensa'
      ];

      numericFields.forEach(field => {
        expect(Schema.flatSchema).toHaveProperty([field]);
        expect(Schema.flatSchema[field][0].type).toBe(
          Schema.OPERATOR_TYPES.NUMBER
        );
      });
    });

    it('given flatSchema, when checking for string new fields, then they exist with STRING type', () => {
      const stringFields = [
        'energiatodistus.toteutunut-ostoenergiankulutus.lisatietoja-fi',
        'energiatodistus.toteutunut-ostoenergiankulutus.lisatietoja-sv'
      ];

      stringFields.forEach(field => {
        expect(Schema.flatSchema).toHaveProperty([field]);
        expect(Schema.flatSchema[field][0].type).toBe(
          Schema.OPERATOR_TYPES.STRING
        );
      });
    });
  });

  // Test 5: flatSchema contains lahtotiedot boolean fields
  it('given flatSchema, when checking for new boolean lahtotiedot fields, then they exist with BOOLEAN type', () => {
    const booleanFields = [
      'energiatodistus.lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin',
      'energiatodistus.lahtotiedot.lammitys.lammonjako-lampotilajousto'
    ];

    booleanFields.forEach(field => {
      expect(Schema.flatSchema).toHaveProperty([field]);
      expect(Schema.flatSchema[field][0].type).toBe(
        Schema.OPERATOR_TYPES.BOOLEAN
      );
    });
  });

  // Test 6: flatSchema contains huomiot.lammitys.kayttoikaa-jaljella-arvio-vuosina
  it('given flatSchema, when checking for kayttoikaa-jaljella-arvio-vuosina, then it exists with NUMBER type', () => {
    const key =
      'energiatodistus.huomiot.lammitys.kayttoikaa-jaljella-arvio-vuosina';
    expect(Schema.flatSchema).toHaveProperty([key]);
    expect(Schema.flatSchema[key][0].type).toBe(
      Schema.OPERATOR_TYPES.NUMBER
    );
  });
});

describe('ET2026 search schema — laatijaSchema field visibility', () => {
  // Test 7: laatijaSchema contains ilmastoselvitys fields
  it('given laatijaSchema, when checking for ilmastoselvitys fields, then they are included', () => {
    expect(Schema.laatijaSchema).toHaveProperty(
      ['energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennus.energiankaytto']
    );
    expect(Schema.laatijaSchema).toHaveProperty(
      ['energiatodistus.ilmastoselvitys.laatija']
    );
    expect(Schema.laatijaSchema).toHaveProperty(
      ['energiatodistus.ilmastoselvitys.laadintaperuste']
    );
  });

  // Test 8: laatijaSchema contains havainnointikayntityyppi-id
  it('given laatijaSchema, when checking for havainnointikayntityyppi-id, then it is included', () => {
    expect(Schema.laatijaSchema).toHaveProperty(
      ['energiatodistus.perustiedot.havainnointikayntityyppi-id']
    );
  });

  // Test 9: laatijaSchema does NOT contain restricted ET2026 fields
  it('given laatijaSchema, when checking restricted fields, then they are NOT included', () => {
    // toteutunut-ostoenergiankulutus is dissocPath'd from laatijaSchema
    expect(Schema.laatijaSchema).not.toHaveProperty(
      ['energiatodistus.toteutunut-ostoenergiankulutus.uusiutuvat-polttoaineet-vuosikulutus-yhteensa']
    );
    // huomiot is dissocPath'd from laatijaSchema
    expect(Schema.laatijaSchema).not.toHaveProperty(
      ['energiatodistus.huomiot.lammitys.kayttoikaa-jaljella-arvio-vuosina']
    );
    // lahtotiedot is pick'd to only lammitetty-nettoala
    expect(Schema.laatijaSchema).not.toHaveProperty(
      ['energiatodistus.lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin']
    );
    // tulokset is pick'd to only e-luku, e-luokka
    expect(Schema.laatijaSchema).not.toHaveProperty(
      ['energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko']
    );
  });
});

describe('ET2026 search schema — paakayttajaSchema contains all new fields', () => {
  // Test 10: paakayttajaSchema contains all ET2026 fields
  it('given paakayttajaSchema, when checking for all new ET2026 fields, then they are all included', () => {
    const allNewFields = [
      'energiatodistus.ilmastoselvitys.hiilijalanjalki.rakennus.energiankaytto',
      'energiatodistus.ilmastoselvitys.hiilikadenjalki.rakennus.uudelleenkaytto',
      'energiatodistus.ilmastoselvitys.laatija',
      'energiatodistus.ilmastoselvitys.laadintaperuste',
      'energiatodistus.toteutunut-ostoenergiankulutus.tietojen-alkuperavuosi',
      'energiatodistus.toteutunut-ostoenergiankulutus.uusiutuvat-polttoaineet-vuosikulutus-yhteensa',
      'energiatodistus.huomiot.lammitys.kayttoikaa-jaljella-arvio-vuosina',
      'energiatodistus.lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin',
      'energiatodistus.lahtotiedot.lammitys.lammonjako-lampotilajousto',
      'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko',
      'energiatodistus.perustiedot.havainnointikayntityyppi-id'
    ];

    allNewFields.forEach(field => {
      expect(Schema.paakayttajaSchema).toHaveProperty([field]);
    });
  });
});
