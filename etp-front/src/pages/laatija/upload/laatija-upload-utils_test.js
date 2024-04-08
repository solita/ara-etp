import { expect, describe, it } from '@jest/globals';
import * as R from 'ramda';
import * as dfns from 'date-fns';
import * as Either from '@Utility/either-utils';

import * as LaatijaUploadUtils from './laatija-upload-utils';

describe('Laatija utils', () => {
  describe('deserializing', () => {
    it('a row', () => {
      const data = [
        `FISE;Tarja Helena;Specimen-Pirex;061154-922D;Kirsinkatu;15150;Lahti;arja.pirex@ara.fi;0400123456;2;21.3.2019`
      ];

      const expected = [
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
          patevyystaso: '2',
          toteamispaivamaara: '21.3.2019',
          maa: 'FI',
          passivoitu: false
        }
      ];

      expect(expected).toEqual(LaatijaUploadUtils.deserialize(data));
    });

    it('more than one row', () => {
      const data = [
        `FISE;Tarja Helena;Specimen-Pirex;061154-922D;Kirsinkatu;15150;Lahti;arja.pirex@ara.fi;0400123456;2;21.3.2019\nFISE;Arja Helena;Specimen-Pirex;061154-922D;Kirsinkatu;15150;Lahti;arja.pirex@ara.fi;0400123456;2;21.3.2019\nFISE;Sari Helena;Specimen-Pirex;061154-922D;Kirsinkatu;15150;Lahti;arja.pirex@ara.fi;0400123456;2;21.3.2019`
      ];

      const expected = [
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
          patevyystaso: '2',
          toteamispaivamaara: '21.3.2019',
          maa: 'FI',
          passivoitu: false
        },
        {
          toteaja: 'FISE',
          etunimi: 'Arja Helena',
          sukunimi: 'Specimen-Pirex',
          henkilotunnus: '061154-922D',
          jakeluosoite: 'Kirsinkatu',
          postinumero: '15150',
          postitoimipaikka: 'Lahti',
          email: 'arja.pirex@ara.fi',
          puhelin: '0400123456',
          patevyystaso: '2',
          toteamispaivamaara: '21.3.2019',
          maa: 'FI',
          passivoitu: false
        },
        {
          toteaja: 'FISE',
          etunimi: 'Sari Helena',
          sukunimi: 'Specimen-Pirex',
          henkilotunnus: '061154-922D',
          jakeluosoite: 'Kirsinkatu',
          postinumero: '15150',
          postitoimipaikka: 'Lahti',
          email: 'arja.pirex@ara.fi',
          puhelin: '0400123456',
          patevyystaso: '2',
          toteamispaivamaara: '21.3.2019',
          maa: 'FI',
          passivoitu: false
        }
      ];

      expect(expected).toEqual(LaatijaUploadUtils.deserialize(data));
    });

    it('a row with a whitespace', () => {
      const data = [
        `FISE;Tarja Helena;Specimen-Pirex;061154-922D;Kirsinkatu;15150;Lahti;arja.pirex@ara.fi;0400123456;2;21.3.2019\n    `
      ];

      const expected = [
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
          patevyystaso: '2',
          toteamispaivamaara: '21.3.2019',
          maa: 'FI',
          passivoitu: false
        }
      ];

      expect(expected).toEqual(LaatijaUploadUtils.deserialize(data));
    });
  });

  describe('parse', () => {
    it('should parse single row', () => {
      const data = {
        toteaja: 'FISE',
        etunimi: 'Tarja Helena',
        sukunimi: 'Specimen-Pirex',
        henkilotunnus: '061154-922D',
        jakeluosoite: 'Kirsinkatu',
        postinumero: '15150',
        postitoimipaikka: 'Lahti',
        email: 'arja.pirex@ara.fi',
        puhelin: '0400123456',
        patevyystaso: '2',
        toteamispaivamaara: '21.3.2019',
        maa: 'FI',
        passivoitu: false
      };

      const expected = {
        toteaja: 'FISE',
        etunimi: 'Tarja Helena',
        sukunimi: 'Specimen-Pirex',
        henkilotunnus: '061154-922D',
        jakeluosoite: 'Kirsinkatu',
        postinumero: '15150',
        postitoimipaikka: 'Lahti',
        email: 'arja.pirex@ara.fi',
        puhelin: '0400123456',
        patevyystaso: Either.Right(2),
        toteamispaivamaara: Either.Right(
          dfns.parse('21.3.2019', 'dd.M.yyyy', 0)
        ),
        maa: 'FI',
        passivoitu: false
      };

      expect(expected).toEqual(LaatijaUploadUtils.parse(data));
    });

    it('should return left for fields with error', () => {
      const data = {
        toteaja: 'FISE',
        etunimi: 'Tarja Helena',
        sukunimi: 'Specimen-Pirex',
        henkilotunnus: '061154-922D',
        jakeluosoite: 'Kirsinkatu',
        postinumero: '15150',
        postitoimipaikka: 'Lahti',
        email: 'arja.pirex@ara.fi',
        puhelin: '0400123456',
        patevyystaso: '2a',
        toteamispaivamaara: 'a21.3.2019',
        maa: 'FI',
        passivoitu: false
      };

      const result = LaatijaUploadUtils.parse(data);

      expect(
        Either.isLeft(result.patevyystaso) &&
          Either.isLeft(result.toteamispaivamaara)
      ).toBe(true);
    });
  });

  describe('validate', () => {
    it('a row', () => {
      const data = [
        `FISE;Tarja Helena;Specimen-Pirex;061154-922D;Kirsinkatu;15150;Lahti;arja.pirex@ara.fi;0400123456;2;21.3.2019`
      ];

      expect(
        R.compose(
          R.all(Either.isRight),
          R.flatten,
          R.map(
            R.compose(
              R.values,
              LaatijaUploadUtils.validate,
              LaatijaUploadUtils.parse
            )
          ),
          LaatijaUploadUtils.deserialize
        )(data)
      ).toBe(true);
    });

    it('invalid row', () => {
      const data = [
        `FISE;Tarja Helena;Specimen-Pirex;061154-922D;Kirsinkatu;15150a;Lahti;arja.pirex@ara.fi;0400123456;2;21.3.2019`
      ];

      expect(
        R.compose(
          R.all(Either.isRight),
          R.flatten,
          R.map(
            R.compose(
              R.values,
              LaatijaUploadUtils.validate,
              LaatijaUploadUtils.parse
            )
          ),
          LaatijaUploadUtils.deserialize
        )(data)
      ).toBe(false);
    });

    it('multiple valid rows', () => {
      const data = [
        `FISE;Tarja Helena;Specimen-Pirex;061154-922D;Kirsinkatu;15150;Lahti;arja.pirex@ara.fi;0400123456;2;21.3.2019\nFISE;Arja Helena;Specimen-Pirex;061154-922D;Kirsinkatu;15150;Lahti;arja.pirex@ara.fi;0400123456;2;21.3.2019\nFISE;Sari Helena;Specimen-Pirex;061154-922D;Kirsinkatu;15150;Lahti;arja.pirex@ara.fi;0400123456;2;21.3.2019`
      ];

      expect(
        R.compose(
          R.all(Either.isRight),
          R.flatten,
          R.map(
            R.compose(
              R.values,
              LaatijaUploadUtils.validate,
              LaatijaUploadUtils.parse
            )
          ),
          LaatijaUploadUtils.deserialize
        )(data)
      ).toBe(true);
    });

    it('multiple invalid valid rows', () => {
      const data = [
        `FISE;Tarja Helena;Specimen-Pirex;061154-922123;Kirsinkatu;15150;Lahti;arja.pirex@ara.fi;0400123456;2;21.3.2019\nFISE;Arja Helena;Specimen-Pirex;061154-922D;Kirsinkatu;15150a;Lahti;arja.pirex@ara.fi;0400123456;2;21.3.2019\nFISE;Sari Helena;Specimen-Pirex;061154-922D;Kirsinkatu;15150;Lahti;arja.pirex@ara.fi;0400123456;a;21.3.2019`
      ];

      expect(
        R.compose(
          R.all(Either.isRight),
          R.flatten,
          R.map(
            R.compose(
              R.values,
              LaatijaUploadUtils.validate,
              LaatijaUploadUtils.parse
            )
          ),
          LaatijaUploadUtils.deserialize
        )(data)
      ).toBe(false);
    });
  });
});
