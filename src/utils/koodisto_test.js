import { assert } from 'chai';
import * as Maybe from './maybe-utils';
import * as Either from './either-utils';
import * as Koodisto from './koodisto';
import * as Locale from '@Language/locale-utils';

describe('Koodisto', () => {
  describe('findFromKoodistoById', () => {
    it('should return found koodi', () => {
      const id = 1;
      const koodisto = [{ id: 0 }, { id: 1 }, { id: 2 }];

      const expected = Maybe.of({ id: 1 });

      assert.deepEqual(expected, Koodisto.findFromKoodistoById(id, koodisto));
    });

    it('should return None when koodi is not found', () => {
      const id = 3;
      const koodisto = [{ id: 0 }, { id: 1 }, { id: 2 }];

      const expected = Maybe.None();

      assert.deepEqual(expected, Koodisto.findFromKoodistoById(id, koodisto));
    });
  });

  describe('koodiLocale', () => {
    it('should format with given locale', () => {
      const labelLocale = Locale.label('fi');
      const koodi = Either.Right(
        Maybe.of({ id: 1, 'label-fi': 'fi', 'label-sv': 'sv' })
      );

      assert.equal('fi', Koodisto.koodiLocale(labelLocale, koodi));
    });
  });
});
