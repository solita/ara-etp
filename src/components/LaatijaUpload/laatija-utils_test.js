import { assert } from 'chai';
import * as R from 'ramda';
import moment from 'moment';

import * as validation from '@Utility/validation';
import { readData, dataValid } from './laatija-utils';

describe('Laatija utils', () => {
  describe('valid data', () => {
    it('a row', () => {
      const data = `FISE;Tarja Helena;Specimen-Pirex;061154-922D;Kirsinkatu;15150;Lahti;arja.pirex@ara.fi;0400123456;2;21.3.2019`;

      let parsedData = readData(data);
      assert.equal(
        R.equals(parsedData, [
          {
            toteaja: 'FISE',
            etunimi: 'Tarja Helena',
            sukunimi: 'Specimen-Pirex',
            henkilotunnus: '061154-922D',
            jakeluosoite: 'Kirsinkatu',
            postinumero: '15150',
            postitoimipaikka: 'Lahti',
            email: 'arja.pirex@ara.fi',
            puhelin: '0400123456',
            patevyystaso: 2,
            toteamispaivamaara: moment(
              '21.3.2019',
              validation.DATE_FORMAT
            ).toDate()
          }
        ]),
        true
      );
      assert.equal(dataValid(readData(data)), true);
    });

    it('more than one row', () => {
      const data = `FISE;Tarja Helena;Specimen-Pirex;061154-922D;Kirsinkatu;15150;Lahti;arja.pirex@ara.fi;0400123456;2;21.3.2019
  FISE;Arja Helena;Specimen-Pirex;061154-922D;Kirsinkatu;15150;Lahti;arja.pirex@ara.fi;0400123456;2;21.3.2019
  FISE;Sari Helena;Specimen-Pirex;061154-922D;Kirsinkatu;15150;Lahti;arja.pirex@ara.fi;0400123456;2;21.3.2019`;

      assert.equal(dataValid(readData(data)), true);
    });

    it('a row with a whitespace', () => {
      const data = `FISE;Tarja Helena;Specimen-Pirex;061154-922D;Kirsinkatu;15150;Lahti;arja.pirex@ara.fi;0400123456;2;21.3.2019
       `;

      assert.equal(dataValid(readData(data)), true);
    });
  });
  describe('invalid data', () => {
    it('a row with an invalid postinumero', () => {
      const data = `FISE;Tarja Helena;Specimen-Pirex;061154-922D;Kirsinkatu;15150a;Lahti;arja.pirex@ara.fi;0400123456;2;21.3.2019`;

      assert.equal(dataValid(readData(data)), false);
    });

    it('more than one row with invalid postinumero', () => {
      const data = `FISE;Tarja Helena;Specimen-Pirex;061154-922D;Kirsinkatu;15150;Lahti;arja.pirex@ara.fi;0400123456;2;21.3.2019
  FISE;Arja Helena;Specimen-Pirex;061154-922D;Kirsinkatu;15150a;Lahti;arja.pirex@ara.fi;0400123456;2;21.3.2019
  FISE;Sari Helena;Specimen-Pirex;061154-922D;Kirsinkatu;15150;Lahti;arja.pirex@ara.fi;0400123456;2;21.3.2019`;

      assert.equal(dataValid(readData(data)), false);
    });

    it('empty data', () => {
      const data = '';

      let parsedData = readData(data);
      assert.equal(R.isEmpty(parsedData), true);
      assert.equal(dataValid(parsedData), false);
    });

    it('null data', () => {
      const data = null;

      let parsedData = readData(data);
      assert.equal(R.isEmpty(parsedData), true);
      assert.equal(dataValid(parsedData), false);
    });

    it('invalid row', () => {
      const data = 'FISE;Tarja Helena;Specimen-Pirex';

      let parsedData = readData(data);
      assert.equal(R.isEmpty(parsedData), true);
      assert.equal(dataValid(parsedData), false);
    });

    it('invalid data', () => {
      const data = 'datadatadata';

      let parsedData = readData(data);
      assert.equal(R.isEmpty(parsedData), true);
      assert.equal(dataValid(parsedData), false);
    });
  });
});
