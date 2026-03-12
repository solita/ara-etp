import { expect, describe, it } from '@jest/globals';
import * as R from 'ramda';

import * as empty from './empty';
import * as EtApi from './energiatodistus-api';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';

describe('Serialisointi/deserialisointi - tayttaa-aplus/a0-vaatimukset:', () => {
  describe('Deserialisointi 2026-API-vastauksesta', () => {
    it('deserialisoi tayttaa-aplus-vaatimukset: true oikein', () => {
      // Given: API palauttaa 2026-todistuksen jossa tayttaa-aplus-vaatimukset on true
      const apiResponse = {
        versio: 2026,
        perustiedot: {
          'tayttaa-aplus-vaatimukset': true,
          'tayttaa-a0-vaatimukset': false
        }
      };

      // When: deserialisoidaan
      const result = EtApi.deserialize(apiResponse);

      // Then: kenttä deserialisoituu oikein
      expect(result.perustiedot['tayttaa-aplus-vaatimukset']).toBe(true);
    });

    it('deserialisoi tayttaa-a0-vaatimukset: false oikein', () => {
      // Given: API palauttaa 2026-todistuksen jossa tayttaa-a0-vaatimukset on false
      const apiResponse = {
        versio: 2026,
        perustiedot: {
          'tayttaa-aplus-vaatimukset': false,
          'tayttaa-a0-vaatimukset': false
        }
      };

      // When: deserialisoidaan
      const result = EtApi.deserialize(apiResponse);

      // Then: kenttä deserialisoituu oikein
      expect(result.perustiedot['tayttaa-a0-vaatimukset']).toBe(false);
    });

    it('deserialisoi puuttuvat boolean-kentät oletusarvoilla (mergeEmpty)', () => {
      // Given: API palauttaa 2026-todistuksen jossa boolean-kentät puuttuvat (vanha data)
      const apiResponse = {
        versio: 2026,
        perustiedot: {}
      };

      // When: deserialisoidaan - mergeEmpty täyttää oletusarvot empty.energiatodistus2026():sta
      const result = EtApi.deserialize(apiResponse);

      // Then: kentät saavat oletusarvon false
      expect(result.perustiedot['tayttaa-aplus-vaatimukset']).toBe(false);
      expect(result.perustiedot['tayttaa-a0-vaatimukset']).toBe(false);
    });
  });

  describe('Serialisointi 2026-todistuksesta', () => {
    it('serialisoi molemmat boolean-kentät true-arvoilla oikein', () => {
      // Given: 2026-todistus jossa molemmat boolean-kentät ovat true
      const et = empty.energiatodistus2026();
      et.perustiedot['tayttaa-aplus-vaatimukset'] = true;
      et.perustiedot['tayttaa-a0-vaatimukset'] = true;

      // When: serialisoidaan backendille
      const result = EtApi.serialize(et);

      // Then: boolean-kentät ovat mukana serialisoidussa datassa
      expect(result.perustiedot['tayttaa-aplus-vaatimukset']).toBe(true);
      expect(result.perustiedot['tayttaa-a0-vaatimukset']).toBe(true);
    });
  });

  describe('Round-trip serialisointi/deserialisointi', () => {
    it('serialize + deserialize round-trip säilyttää boolean-kenttien arvot', () => {
      // Given: tyhjä 2026-energiatodistus
      const original = empty.energiatodistus2026();

      // When: serialisoidaan ja deserialisoidaan
      // Huom: serialize poistaa versio-kentän, joten se pitää lisätä takaisin deserializea varten
      const serialized = EtApi.serialize(original);
      const deserialized = EtApi.deserialize({
        ...serialized,
        versio: 2026
      });

      // Then: tayttaa-aplus-vaatimukset ja tayttaa-a0-vaatimukset säilyvät
      expect(deserialized.perustiedot['tayttaa-aplus-vaatimukset']).toBe(false);
      expect(deserialized.perustiedot['tayttaa-a0-vaatimukset']).toBe(false);
    });
  });
});
