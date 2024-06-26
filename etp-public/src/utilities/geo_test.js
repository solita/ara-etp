import { expect, describe, it } from '@jest/globals';
import * as Geo from './geo';

describe('GeoUtils:', () => {
  describe('isLabelMatch', () => {
    it('should return true when -fi label matches', () => {
      const item = { 'label-fi': 'A', 'label-sv': 'B' };
      expect(Geo.isLabelMatch('A')(item)).toBe(true);
    });

    it('should return true when -fi label matches', () => {
      const item = { 'label-fi': 'A', 'label-sv': 'B' };
      expect(Geo.isLabelMatch('B')(item)).toBe(true);
    });

    it('should return false when neither label matches', () => {
      const item = { 'label-fi': 'A', 'label-sv': 'B' };
      expect(Geo.isLabelMatch('C')(item)).toBe(false);
    });
  });

  describe('finToimintaalue', () => {
    it('should return found toiminta-alue with given id', () => {
      const toimintaalueet = [{ id: 1 }, { id: 2 }, { id: 3 }];
      expect(Geo.findToimintaalue(toimintaalueet, 3)).toEqual({ id: 3 });
    });

    it('should return undefined toiminta-alue with not found id', () => {
      const toimintaalueet = [{ id: 1 }, { id: 2 }, { id: 3 }];
      expect(Geo.findToimintaalue(toimintaalueet, 4)).toEqual(undefined);
    });
  });

  describe('findToimintaalueetByMaakunta', () => {
    it('should return set with matching id', () => {
      const expected = new Set([1]);
      const toimintaalueet = [{ id: 1, 'label-fi': 'a', 'label-sv': 'b' }];
      expect(Geo.findToimintaalueetByMaakunta('a', toimintaalueet)).toEqual(
        expected
      );
    });

    it('should return set with matching ids', () => {
      const expected = new Set([1, 2]);
      const toimintaalueet = [
        { id: 1, 'label-fi': 'a', 'label-sv': 'b' },
        { id: 2, 'label-fi': 'b', 'label-sv': 'a' }
      ];
      expect(Geo.findToimintaalueetByMaakunta('b', toimintaalueet)).toEqual(
        expected
      );
    });

    it('should return empty set when maakunta not found', () => {
      const expected = new Set([]);
      const toimintaalueet = [
        { id: 1, 'label-fi': 'a', 'label-sv': 'b' },
        { id: 2, 'label-fi': 'b', 'label-sv': 'a' }
      ];
      expect(Geo.findToimintaalueetByMaakunta('c', toimintaalueet)).toEqual(
        expected
      );
    });
  });

  describe('findToimintaalueIdsByKuntaIdt', () => {
    it('should return set with matching ids', () => {
      const kuntaIds = new Set([1, 2, 3]);
      const kunnat = [
        { id: 1, 'toimintaalue-id': 1 },
        { id: 2, 'toimintaalue-id': 2 },
        { id: 3, 'toimintaalue-id': 3 },
        { id: 4, 'toimintaalue-id': 4 }
      ];

      const expected = new Set([1, 2, 3]);

      expect(Geo.findToimintaalueIdsByKuntaIds(kuntaIds, kunnat)).toEqual(
        expected
      );
    });

    it('should empty set with no matching ids', () => {
      const kuntaIds = new Set([5, 6]);
      const kunnat = [
        { id: 1, 'toimintaalue-id': 1 },
        { id: 2, 'toimintaalue-id': 2 },
        { id: 3, 'toimintaalue-id': 2 },
        { id: 4, 'toimintaalue-id': 4 }
      ];

      const expected = new Set([]);

      expect(Geo.findToimintaalueIdsByKuntaIds(kuntaIds, kunnat)).toEqual(
        expected
      );
    });
  });

  describe('findToimintaalueIdsByKunta', () => {
    it('should return set with matching kunta', () => {
      const kunta = 'a';
      const kunnat = [
        { id: 1, 'label-fi': 'a', 'label-sv': 'b', 'toimintaalue-id': 1 },
        { id: 2, 'label-fi': 'b', 'label-sv': 'a', 'toimintaalue-id': 2 },
        { id: 3, 'label-fi': 'as', 'label-sv': 'd', 'toimintaalue-id': 2 },
        { id: 4, 'label-fi': 'e', 'label-sv': 'f', 'toimintaalue-id': 4 }
      ];

      const expected = new Set([1, 2]);

      expect(Geo.findToimintaalueIdsByKunta(kunta, kunnat)).toEqual(expected);
    });

    it('should return empty set with no matching kunta', () => {
      const kunta = 'x';
      const kunnat = [
        { id: 1, 'label-fi': 'a', 'label-sv': 'b', 'toimintaalue-id': 1 },
        { id: 2, 'label-fi': 'b', 'label-sv': 'a', 'toimintaalue-id': 2 },
        { id: 3, 'label-fi': 'as', 'label-sv': 'd', 'toimintaalue-id': 2 },
        { id: 4, 'label-fi': 'e', 'label-sv': 'f', 'toimintaalue-id': 4 }
      ];

      const expected = new Set([]);

      expect(Geo.findToimintaalueIdsByKunta(kunta, kunnat)).toEqual(expected);
    });
  });

  describe('findKuntaIdsByPostitoimipaikka', () => {
    it('should return set with matching postitoimipaikka', () => {
      const postitoimipaikka = 'a';
      const postinumerot = [
        { id: 1, 'label-fi': 'a', 'label-sv': 'b', 'kunta-id': 1 },
        { id: 2, 'label-fi': 'b', 'label-sv': 'a', 'kunta-id': 2 },
        { id: 3, 'label-fi': 'as', 'label-sv': 'd', 'kunta-id': 2 },
        { id: 4, 'label-fi': 'e', 'label-sv': 'f', 'kunta-id': 4 }
      ];

      const expected = new Set([1, 2]);

      expect(
        Geo.findKuntaIdsByPostitoimipaikka(postitoimipaikka, postinumerot)
      ).toEqual(expected);
    });
    it('should return empty set with no matching postitoimipaikka', () => {
      const postitoimipaikka = 'x';
      const postinumerot = [
        { id: 1, 'label-fi': 'a', 'label-sv': 'b', 'kunta-id': 1 },
        { id: 2, 'label-fi': 'b', 'label-sv': 'a', 'kunta-id': 2 },
        { id: 3, 'label-fi': 'as', 'label-sv': 'd', 'kunta-id': 2 },
        { id: 4, 'label-fi': 'e', 'label-sv': 'f', 'kunta-id': 4 }
      ];

      const expected = new Set([]);

      expect(
        Geo.findKuntaIdsByPostitoimipaikka(postitoimipaikka, postinumerot)
      ).toEqual(expected);
    });
  });

  describe('findKuntaIdsByPostinumero', () => {
    it('should return set with matching postinumero', () => {
      const postinumero = 3;
      const postinumerot = [
        { id: 1, 'label-fi': 'a', 'label-sv': 'b', 'kunta-id': 1 },
        { id: 2, 'label-fi': 'b', 'label-sv': 'a', 'kunta-id': 2 },
        { id: 3, 'label-fi': 'as', 'label-sv': 'd', 'kunta-id': 2 },
        { id: 4, 'label-fi': 'e', 'label-sv': 'f', 'kunta-id': 4 }
      ];

      const expected = new Set([2]);

      expect(Geo.findKuntaIdsByPostinumero(postinumero, postinumerot)).toEqual(
        expected
      );
    });
    it('should return empty set with no matching postitoimipaikka', () => {
      const postinumero = 5;
      const postinumerot = [
        { id: 1, 'label-fi': 'a', 'label-sv': 'b', 'kunta-id': 1 },
        { id: 2, 'label-fi': 'b', 'label-sv': 'a', 'kunta-id': 2 },
        { id: 3, 'label-fi': 'as', 'label-sv': 'd', 'kunta-id': 2 },
        { id: 4, 'label-fi': 'e', 'label-sv': 'f', 'kunta-id': 4 }
      ];

      const expected = new Set([]);

      expect(Geo.findKuntaIdsByPostinumero(postinumero, postinumerot)).toEqual(
        expected
      );
    });
  });

  describe('findToimintaalueIds', () => {
    const postinumerot = [
      { id: 1, 'label-fi': 'pn1-fi', 'label-sv': 'pn1-sv', 'kunta-id': 10 },
      { id: 2, 'label-fi': 'pn2-fi', 'label-sv': 'pn2-sv', 'kunta-id': 20 },
      { id: 3, 'label-fi': 'pn3-fi', 'label-sv': 'pn3-sv', 'kunta-id': 20 },
      { id: 33, 'label-fi': 'pn3b-fi', 'label-sv': 'pn3b-sv', 'kunta-id': 30 },
      { id: 4, 'label-fi': 'pn4-fi', 'label-sv': 'pn4-sv', 'kunta-id': 40 }
    ];

    const kunnat = [
      {
        id: 10,
        'label-fi': 'kn10-fi',
        'label-sv': 'kn10-sv',
        'toimintaalue-id': 100
      },
      {
        id: 20,
        'label-fi': 'kn20-fi',
        'label-sv': 'kn20-sv',
        'toimintaalue-id': 200
      },
      {
        id: 30,
        'label-fi': 'kn30-fi',
        'label-sv': 'kn30-sv',
        'toimintaalue-id': 300
      },
      {
        id: 40,
        'label-fi': 'kn40-fi',
        'label-sv': 'kn40-sv',
        'toimintaalue-id': 400
      }
    ];

    const toimintaalueet = [
      {
        id: 100,
        'label-fi': 'ta100-fi',
        'label-sv': 'ta100-sv'
      },
      {
        id: 200,
        'label-fi': 'ta200-fi',
        'label-sv': 'ta200-sv'
      },
      {
        id: 300,
        'label-fi': 'ta300-fi',
        'label-sv': 'ta300-sv'
      },
      {
        id: 400,
        'label-fi': 'ta400-fi',
        'label-sv': 'ta400-sv'
      }
    ];
    it('should return set with postinumero', () => {
      const haku = 3;
      const expected = new Set([200]);

      expect(
        Geo.findToimintaalueIds(haku, toimintaalueet, kunnat, postinumerot)
      ).toEqual(expected);
    });
  });
});
