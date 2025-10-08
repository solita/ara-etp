import { expect, describe, it } from '@jest/globals';

import * as empty from '@Pages/energiatodistus/empty.js';
import * as PPPApi from './perusparannuspassi-api.js';

describe('Serialization and deserialization of empty PPP: ', () => {
  it('should serialize and deserialize to the same object', () => {
    const original = empty.perusparannuspassi(42);
    const deserialized = PPPApi.deserialize(PPPApi.serialize(original));

    expect(deserialized).toEqual(original);
  });
});
