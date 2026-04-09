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

  describe('schema field inventory', () => {
    it('laatijaSchema contains exactly the expected fields', () => {
      const actual = Object.keys(Schema.laatijaSchema).sort();
      const expected = [
        'energiatodistus.allekirjoitusaika',
        'energiatodistus.id',
        'energiatodistus.lahtotiedot.lammitetty-nettoala',
        'energiatodistus.laskuriviviite',
        'energiatodistus.perustiedot.alakayttotarkoitusluokka',
        'energiatodistus.perustiedot.havainnointikaynti',
        'energiatodistus.perustiedot.julkinen-rakennus',
        'energiatodistus.perustiedot.katuosoite-*',
        'energiatodistus.perustiedot.kayttotarkoitus',
        'energiatodistus.perustiedot.kieli',
        'energiatodistus.perustiedot.laatimisvaihe',
        'energiatodistus.perustiedot.nimi-fi',
        'energiatodistus.perustiedot.nimi-sv',
        'energiatodistus.perustiedot.postinumero',
        'energiatodistus.perustiedot.rakennusosa',
        'energiatodistus.perustiedot.rakennustunnus',
        'energiatodistus.perustiedot.tilaaja',
        'energiatodistus.perustiedot.uudisrakennus',
        'energiatodistus.perustiedot.valmistumisvuosi',
        'energiatodistus.perustiedot.yritys.nimi',
        'energiatodistus.tila-id',
        'energiatodistus.tulokset.e-luku',
        'energiatodistus.tulokset.e-luokka',
        'energiatodistus.versio',
        'energiatodistus.voimassaolo-paattymisaika',
        'kunta.id',
        'laatija.voimassaolo-paattymisaika',
        'perusparannuspassi.id',
        'perusparannuspassi.valid',
        'postinumero.label'
      ];
      expect(actual).toEqual(expected);
    });

    it('paakayttajaSchema contains exactly the expected fields', () => {
      const actual = Object.keys(Schema.paakayttajaSchema).sort();
      const expected = [
        'energiatodistus.allekirjoitusaika',
        'energiatodistus.huomiot.alapohja-ylapohja.teksti-fi',
        'energiatodistus.huomiot.alapohja-ylapohja.teksti-sv',
        'energiatodistus.huomiot.iv-ilmastointi.teksti-fi',
        'energiatodistus.huomiot.iv-ilmastointi.teksti-sv',
        'energiatodistus.huomiot.lammitys.teksti-fi',
        'energiatodistus.huomiot.lammitys.teksti-sv',
        'energiatodistus.huomiot.lisatietoja-fi',
        'energiatodistus.huomiot.lisatietoja-sv',
        'energiatodistus.huomiot.suositukset-fi',
        'energiatodistus.huomiot.suositukset-sv',
        'energiatodistus.huomiot.valaistus-muut.teksti-fi',
        'energiatodistus.huomiot.valaistus-muut.teksti-sv',
        'energiatodistus.huomiot.ymparys.teksti-fi',
        'energiatodistus.huomiot.ymparys.teksti-sv',
        'energiatodistus.id',
        'energiatodistus.korvattu-energiatodistus-id',
        'energiatodistus.laatija-id',
        'energiatodistus.lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin',
        'energiatodistus.lahtotiedot.ikkunat.etela.U',
        'energiatodistus.lahtotiedot.ikkunat.etela.ala',
        'energiatodistus.lahtotiedot.ikkunat.etela.g-ks',
        'energiatodistus.lahtotiedot.ikkunat.ita.U',
        'energiatodistus.lahtotiedot.ikkunat.ita.ala',
        'energiatodistus.lahtotiedot.ikkunat.ita.g-ks',
        'energiatodistus.lahtotiedot.ikkunat.kaakko.U',
        'energiatodistus.lahtotiedot.ikkunat.kaakko.ala',
        'energiatodistus.lahtotiedot.ikkunat.kaakko.g-ks',
        'energiatodistus.lahtotiedot.ikkunat.koillinen.U',
        'energiatodistus.lahtotiedot.ikkunat.koillinen.ala',
        'energiatodistus.lahtotiedot.ikkunat.koillinen.g-ks',
        'energiatodistus.lahtotiedot.ikkunat.lansi.U',
        'energiatodistus.lahtotiedot.ikkunat.lansi.ala',
        'energiatodistus.lahtotiedot.ikkunat.lansi.g-ks',
        'energiatodistus.lahtotiedot.ikkunat.lounas.U',
        'energiatodistus.lahtotiedot.ikkunat.lounas.ala',
        'energiatodistus.lahtotiedot.ikkunat.lounas.g-ks',
        'energiatodistus.lahtotiedot.ikkunat.luode.U',
        'energiatodistus.lahtotiedot.ikkunat.luode.ala',
        'energiatodistus.lahtotiedot.ikkunat.luode.g-ks',
        'energiatodistus.lahtotiedot.ikkunat.pohjoinen.U',
        'energiatodistus.lahtotiedot.ikkunat.pohjoinen.ala',
        'energiatodistus.lahtotiedot.ikkunat.pohjoinen.g-ks',
        'energiatodistus.lahtotiedot.ilmanvaihto.erillispoistot.poisto',
        'energiatodistus.lahtotiedot.ilmanvaihto.erillispoistot.sfp',
        'energiatodistus.lahtotiedot.ilmanvaihto.erillispoistot.tulo',
        'energiatodistus.lahtotiedot.ilmanvaihto.kuvaus-fi',
        'energiatodistus.lahtotiedot.ilmanvaihto.kuvaus-sv',
        'energiatodistus.lahtotiedot.ilmanvaihto.lto-vuosihyotysuhde',
        'energiatodistus.lahtotiedot.ilmanvaihto.paaiv.jaatymisenesto',
        'energiatodistus.lahtotiedot.ilmanvaihto.paaiv.lampotilasuhde',
        'energiatodistus.lahtotiedot.ilmanvaihto.paaiv.poisto',
        'energiatodistus.lahtotiedot.ilmanvaihto.paaiv.sfp',
        'energiatodistus.lahtotiedot.ilmanvaihto.paaiv.tulo',
        'energiatodistus.lahtotiedot.ilmanvaihto.tyyppi-id',
        'energiatodistus.lahtotiedot.jaahdytysjarjestelma.jaahdytyskauden-painotettu-kylmakerroin',
        'energiatodistus.lahtotiedot.lammitetty-nettoala',
        'energiatodistus.lahtotiedot.lammitys.ilmalampopumppu.maara',
        'energiatodistus.lahtotiedot.lammitys.ilmalampopumppu.tuotto',
        'energiatodistus.lahtotiedot.lammitys.lammin-kayttovesi.apulaitteet',
        'energiatodistus.lahtotiedot.lammitys.lammin-kayttovesi.jaon-hyotysuhde',
        'energiatodistus.lahtotiedot.lammitys.lammin-kayttovesi.lampohavio-lammittamaton-tila',
        'energiatodistus.lahtotiedot.lammitys.lammin-kayttovesi.lampokerroin',
        'energiatodistus.lahtotiedot.lammitys.lammin-kayttovesi.lampopumppu-tuotto-osuus',
        'energiatodistus.lahtotiedot.lammitys.lammin-kayttovesi.tuoton-hyotysuhde',
        'energiatodistus.lahtotiedot.lammitys.lammitysmuoto.id',
        'energiatodistus.lahtotiedot.lammitys.lammitysmuoto.kuvaus-fi',
        'energiatodistus.lahtotiedot.lammitys.lammitysmuoto.kuvaus-sv',
        'energiatodistus.lahtotiedot.lammitys.lammonjako-lampotilajousto',
        'energiatodistus.lahtotiedot.lammitys.lammonjako.id',
        'energiatodistus.lahtotiedot.lammitys.lammonjako.kuvaus-fi',
        'energiatodistus.lahtotiedot.lammitys.lammonjako.kuvaus-sv',
        'energiatodistus.lahtotiedot.lammitys.takka.maara',
        'energiatodistus.lahtotiedot.lammitys.takka.tuotto',
        'energiatodistus.lahtotiedot.lammitys.tilat-ja-iv.apulaitteet',
        'energiatodistus.lahtotiedot.lammitys.tilat-ja-iv.jaon-hyotysuhde',
        'energiatodistus.lahtotiedot.lammitys.tilat-ja-iv.lampohavio-lammittamaton-tila',
        'energiatodistus.lahtotiedot.lammitys.tilat-ja-iv.lampokerroin',
        'energiatodistus.lahtotiedot.lammitys.tilat-ja-iv.lampopumppu-tuotto-osuus',
        'energiatodistus.lahtotiedot.lammitys.tilat-ja-iv.tuoton-hyotysuhde',
        'energiatodistus.lahtotiedot.lkvn-kaytto.lammitysenergian-nettotarve',
        'energiatodistus.lahtotiedot.lkvn-kaytto.ominaiskulutus',
        'energiatodistus.lahtotiedot.rakennusvaippa.alapohja.U',
        'energiatodistus.lahtotiedot.rakennusvaippa.alapohja.UA',
        'energiatodistus.lahtotiedot.rakennusvaippa.alapohja.ala',
        'energiatodistus.lahtotiedot.rakennusvaippa.alapohja.osuus-lampohaviosta',
        'energiatodistus.lahtotiedot.rakennusvaippa.ikkunat.U',
        'energiatodistus.lahtotiedot.rakennusvaippa.ikkunat.UA',
        'energiatodistus.lahtotiedot.rakennusvaippa.ikkunat.ala',
        'energiatodistus.lahtotiedot.rakennusvaippa.ikkunat.osuus-lampohaviosta',
        'energiatodistus.lahtotiedot.rakennusvaippa.ilmanvuotoluku',
        'energiatodistus.lahtotiedot.rakennusvaippa.kylmasillat-UA',
        'energiatodistus.lahtotiedot.rakennusvaippa.kylmasillat-osuus-lampohaviosta',
        'energiatodistus.lahtotiedot.rakennusvaippa.ulkoovet.U',
        'energiatodistus.lahtotiedot.rakennusvaippa.ulkoovet.UA',
        'energiatodistus.lahtotiedot.rakennusvaippa.ulkoovet.ala',
        'energiatodistus.lahtotiedot.rakennusvaippa.ulkoovet.osuus-lampohaviosta',
        'energiatodistus.lahtotiedot.rakennusvaippa.ulkoseinat.U',
        'energiatodistus.lahtotiedot.rakennusvaippa.ulkoseinat.UA',
        'energiatodistus.lahtotiedot.rakennusvaippa.ulkoseinat.ala',
        'energiatodistus.lahtotiedot.rakennusvaippa.ulkoseinat.osuus-lampohaviosta',
        'energiatodistus.lahtotiedot.rakennusvaippa.ylapohja.U',
        'energiatodistus.lahtotiedot.rakennusvaippa.ylapohja.UA',
        'energiatodistus.lahtotiedot.rakennusvaippa.ylapohja.ala',
        'energiatodistus.lahtotiedot.rakennusvaippa.ylapohja.osuus-lampohaviosta',
        'energiatodistus.lahtotiedot.sis-kuorma.henkilot.kayttoaste',
        'energiatodistus.lahtotiedot.sis-kuorma.henkilot.lampokuorma',
        'energiatodistus.lahtotiedot.sis-kuorma.kuluttajalaitteet.kayttoaste',
        'energiatodistus.lahtotiedot.sis-kuorma.kuluttajalaitteet.lampokuorma',
        'energiatodistus.lahtotiedot.sis-kuorma.valaistus.kayttoaste',
        'energiatodistus.lahtotiedot.sis-kuorma.valaistus.lampokuorma',
        'energiatodistus.lisamerkintoja-fi',
        'energiatodistus.lisamerkintoja-sv',
        'energiatodistus.perustiedot.alakayttotarkoitusluokka',
        'energiatodistus.perustiedot.havainnointikaynti',
        'energiatodistus.perustiedot.havainnointikayntityyppi-id',
        'energiatodistus.perustiedot.julkinen-rakennus',
        'energiatodistus.perustiedot.katuosoite-*',
        'energiatodistus.perustiedot.kayttotarkoitus',
        'energiatodistus.perustiedot.keskeiset-suositukset-fi',
        'energiatodistus.perustiedot.keskeiset-suositukset-sv',
        'energiatodistus.perustiedot.kieli',
        'energiatodistus.perustiedot.kiinteistotunnus',
        'energiatodistus.perustiedot.laatimisvaihe',
        'energiatodistus.perustiedot.nimi-fi',
        'energiatodistus.perustiedot.nimi-sv',
        'energiatodistus.perustiedot.postinumero',
        'energiatodistus.perustiedot.rakennusosa',
        'energiatodistus.perustiedot.rakennustunnus',
        'energiatodistus.perustiedot.tilaaja',
        'energiatodistus.perustiedot.uudisrakennus',
        'energiatodistus.perustiedot.valmistumisvuosi',
        'energiatodistus.perustiedot.yritys.nimi',
        'energiatodistus.tila-id',
        'energiatodistus.toteutunut-ostoenergiankulutus.ostettu-energia.kaukojaahdytys-neliovuosikulutus',
        'energiatodistus.toteutunut-ostoenergiankulutus.ostettu-energia.kaukolampo-neliovuosikulutus',
        'energiatodistus.toteutunut-ostoenergiankulutus.ostettu-energia.kayttajasahko-neliovuosikulutus',
        'energiatodistus.toteutunut-ostoenergiankulutus.ostettu-energia.kiinteistosahko-neliovuosikulutus',
        'energiatodistus.toteutunut-ostoenergiankulutus.ostettu-energia.kokonaissahko-neliovuosikulutus',
        'energiatodistus.toteutunut-ostoenergiankulutus.ostetut-polttoaineet.kevyt-polttooljy-neliovuosikulutus',
        'energiatodistus.toteutunut-ostoenergiankulutus.ostetut-polttoaineet.pilkkeet-havu-sekapuu-neliovuosikulutus',
        'energiatodistus.toteutunut-ostoenergiankulutus.ostetut-polttoaineet.pilkkeet-koivu-neliovuosikulutus',
        'energiatodistus.toteutunut-ostoenergiankulutus.ostetut-polttoaineet.puupelletit-neliovuosikulutus',
        'energiatodistus.tulokset.e-luku',
        'energiatodistus.tulokset.e-luokka',
        'energiatodistus.tulokset.kasvihuonepaastot-per-nelio',
        'energiatodistus.tulokset.kaytettavat-energiamuodot.fossiilinen-polttoaine',
        'energiatodistus.tulokset.kaytettavat-energiamuodot.fossiilinen-polttoaine-painotettu',
        'energiatodistus.tulokset.kaytettavat-energiamuodot.fossiilinen-polttoaine-painotettu-neliovuosikulutus',
        'energiatodistus.tulokset.kaytettavat-energiamuodot.kaukojaahdytys',
        'energiatodistus.tulokset.kaytettavat-energiamuodot.kaukojaahdytys-painotettu',
        'energiatodistus.tulokset.kaytettavat-energiamuodot.kaukojaahdytys-painotettu-neliovuosikulutus',
        'energiatodistus.tulokset.kaytettavat-energiamuodot.kaukolampo',
        'energiatodistus.tulokset.kaytettavat-energiamuodot.kaukolampo-painotettu',
        'energiatodistus.tulokset.kaytettavat-energiamuodot.kaukolampo-painotettu-neliovuosikulutus',
        'energiatodistus.tulokset.kaytettavat-energiamuodot.sahko',
        'energiatodistus.tulokset.kaytettavat-energiamuodot.sahko-painotettu',
        'energiatodistus.tulokset.kaytettavat-energiamuodot.sahko-painotettu-neliovuosikulutus',
        'energiatodistus.tulokset.kaytettavat-energiamuodot.uusiutuva-polttoaine',
        'energiatodistus.tulokset.kaytettavat-energiamuodot.uusiutuva-polttoaine-painotettu',
        'energiatodistus.tulokset.kaytettavat-energiamuodot.uusiutuva-polttoaine-painotettu-neliovuosikulutus',
        'energiatodistus.tulokset.lampokuormat.aurinko',
        'energiatodistus.tulokset.lampokuormat.aurinko-neliovuosikuorma',
        'energiatodistus.tulokset.lampokuormat.ihmiset',
        'energiatodistus.tulokset.lampokuormat.ihmiset-neliovuosikuorma',
        'energiatodistus.tulokset.lampokuormat.kuluttajalaitteet',
        'energiatodistus.tulokset.lampokuormat.kuluttajalaitteet-neliovuosikuorma',
        'energiatodistus.tulokset.lampokuormat.kvesi',
        'energiatodistus.tulokset.lampokuormat.kvesi-neliovuosikuorma',
        'energiatodistus.tulokset.lampokuormat.valaistus',
        'energiatodistus.tulokset.lampokuormat.valaistus-neliovuosikuorma',
        'energiatodistus.tulokset.laskentatyokalu',
        'energiatodistus.tulokset.nettotarve.ilmanvaihdon-lammitys-neliovuosikulutus',
        'energiatodistus.tulokset.nettotarve.ilmanvaihdon-lammitys-vuosikulutus',
        'energiatodistus.tulokset.nettotarve.jaahdytys-neliovuosikulutus',
        'energiatodistus.tulokset.nettotarve.jaahdytys-vuosikulutus',
        'energiatodistus.tulokset.nettotarve.kayttoveden-valmistus-neliovuosikulutus',
        'energiatodistus.tulokset.nettotarve.kayttoveden-valmistus-vuosikulutus',
        'energiatodistus.tulokset.nettotarve.tilojen-lammitys-neliovuosikulutus',
        'energiatodistus.tulokset.nettotarve.tilojen-lammitys-vuosikulutus',
        'energiatodistus.tulokset.tekniset-jarjestelmat.iv-sahko',
        'energiatodistus.tulokset.tekniset-jarjestelmat.jaahdytys.kaukojaahdytys',
        'energiatodistus.tulokset.tekniset-jarjestelmat.jaahdytys.sahko',
        'energiatodistus.tulokset.tekniset-jarjestelmat.kaukojaahdytys',
        'energiatodistus.tulokset.tekniset-jarjestelmat.kayttoveden-valmistus.lampo',
        'energiatodistus.tulokset.tekniset-jarjestelmat.kayttoveden-valmistus.sahko',
        'energiatodistus.tulokset.tekniset-jarjestelmat.kuluttajalaitteet-ja-valaistus-sahko',
        'energiatodistus.tulokset.tekniset-jarjestelmat.lampo',
        'energiatodistus.tulokset.tekniset-jarjestelmat.sahko',
        'energiatodistus.tulokset.tekniset-jarjestelmat.tilojen-lammitys.lampo',
        'energiatodistus.tulokset.tekniset-jarjestelmat.tilojen-lammitys.sahko',
        'energiatodistus.tulokset.tekniset-jarjestelmat.tuloilman-lammitys.lampo',
        'energiatodistus.tulokset.tekniset-jarjestelmat.tuloilman-lammitys.sahko',
        'energiatodistus.tulokset.uusiutuvan-energian-osuus',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkolampo',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.lampopumppu',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.muulampo',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.muusahko',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.tuulisahko',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat.aurinkolampo',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat.aurinkolampo-neliovuosikulutus',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat.aurinkosahko',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat.aurinkosahko-neliovuosikulutus',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat.lampopumppu',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat.lampopumppu-neliovuosikulutus',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat.muulampo',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat.muulampo-neliovuosikulutus',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat.muusahko',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat.muusahko-neliovuosikulutus',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat.tuulisahko',
        'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat.tuulisahko-neliovuosikulutus',
        'energiatodistus.versio',
        'energiatodistus.voimassaolo-paattymisaika',
        'kunta.id',
        'laatija.patevyystaso',
        'laatija.toteamispaivamaara',
        'laatija.voimassaolo-paattymisaika',
        'perusparannuspassi.id',
        'perusparannuspassi.valid',
        'postinumero.label'
      ];
      expect(actual).toEqual(expected);
    });
  });

  describe('ET2026-only boolean fields version constraint', () => {
    // These two boolean fields exist in all versions as NOT NULL DEFAULT false (migration v5.60),
    // so a bare `= false` query would incorrectly return ET2013/ET2018 certificates.
    // The format function must inject `['=', 'energiatodistus.versio', 2026]` alongside the
    // boolean condition so that searches are scoped to ET2026 only.

    // Both ET2026-only boolean fields, tested with both true and false values
    const et2026BooleanFields = [
      'energiatodistus.lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin',
      'energiatodistus.lahtotiedot.lammitys.lammonjako-lampotilajousto'
    ];

    it.each(et2026BooleanFields)(
      '%s: format includes versio=2026 constraint for both true and false',
      key => {
        const op = Schema.paakayttajaSchema[key][0];

        for (const value of [true, false]) {
          const result = op.operation.format(
            op.operation.serverCommand,
            op.key,
            value
          );
          expect(result).toContainEqual(['=', 'energiatodistus.versio', 2026]);
        }
      }
    );

    // Compared to the above - julkinen-rakennus exists in all certificate versions (pt$julkinen_rakennus is
    // always present and carries real data), so it must NOT receive a versio constraint.
    it('julkinen-rakennus: format does not add versio constraint – field exists in all certificate versions', () => {
      // Given: boolean field present across all certificate versions
      const key = 'energiatodistus.perustiedot.julkinen-rakennus';
      const op = Schema.paakayttajaSchema[key][0];
      // When: format is called with true and false
      const resultTrue = op.operation.format(
        op.operation.serverCommand,
        op.key,
        true
      );
      const resultFalse = op.operation.format(
        op.operation.serverCommand,
        op.key,
        false
      );
      // Then: neither result contains a versio constraint
      const hasVersioConstraint = result =>
        result.some(
          entry => Array.isArray(entry) && entry[1] === 'energiatodistus.versio'
        );
      expect(hasVersioConstraint(resultTrue)).toBe(false);
      expect(hasVersioConstraint(resultFalse)).toBe(false);
    });
  });
});
