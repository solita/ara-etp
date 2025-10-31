import { expect, describe, it } from '@jest/globals';

import * as empty from '@Pages/energiatodistus/empty.js';
import * as PPPApi from './perusparannuspassi-api.js';
import * as Maybe from '@Utility/maybe-utils';

describe('Serialization and deserialization of empty PPP: ', () => {
  it('should serialize and deserialize to the same object', () => {
    const original = empty.perusparannuspassi(42);
    const deserialized = PPPApi.deserialize(PPPApi.serialize(original));

    expect(deserialized).toEqual(original);
  });
});

describe('Serialization and deserialization of PPP / toimenpide-ehdotukset: ', () => {
  it('should put values at the beginning of the list.', () => {
    const original = empty.perusparannuspassi(42);

    const original1 = [
      Maybe.None(),
      Maybe.Some(1),
      Maybe.None(),
      Maybe.Some(2),
      Maybe.None(),
      Maybe.None()
    ];
    const expected1 = [
      Maybe.Some(1),
      Maybe.Some(2),
      Maybe.None(),
      Maybe.None(),
      Maybe.None(),
      Maybe.None()
    ];

    const original2 = [
      Maybe.None(),
      Maybe.Some(4),
      Maybe.None(),
      Maybe.None(),
      Maybe.None(),
      Maybe.Some(5)
    ];
    const expected2 = [
      Maybe.Some(4),
      Maybe.Some(5),
      Maybe.None(),
      Maybe.None(),
      Maybe.None(),
      Maybe.None()
    ];

    const expected3 = [
      Maybe.None(),
      Maybe.None(),
      Maybe.None(),
      Maybe.None(),
      Maybe.None(),
      Maybe.None()
    ];
    const expected4 = [
      Maybe.None(),
      Maybe.None(),
      Maybe.None(),
      Maybe.None(),
      Maybe.None(),
      Maybe.None()
    ];

    // Mutate original
    original.vaiheet[0].toimenpiteet['toimenpide-ehdotukset'] = original1;
    original.vaiheet[1].toimenpiteet['toimenpide-ehdotukset'] = original2;

    const deserialized = PPPApi.deserialize(PPPApi.serialize(original));

    const recieved1 =
      deserialized.vaiheet[0].toimenpiteet['toimenpide-ehdotukset'];
    const recieved2 =
      deserialized.vaiheet[1].toimenpiteet['toimenpide-ehdotukset'];
    const recieved3 =
      deserialized.vaiheet[2].toimenpiteet['toimenpide-ehdotukset'];
    const recieved4 =
      deserialized.vaiheet[3].toimenpiteet['toimenpide-ehdotukset'];

    expect(recieved1).toEqual(expected1);
    expect(recieved2).toEqual(expected2);
    expect(recieved3).toEqual(expected3);
    expect(recieved4).toEqual(expected4);
  });
});
