import { expect, describe, it } from '@jest/globals';
import { overrideLabels } from './laatimisvaihe-labels';

// The label override function takes a version and a list of laatimisvaihe entries
// and returns the list with overridden labels for pre-2026 versions.
// For version 2026, labels are passed through unchanged.
// For versions < 2026 (2018, 2013), IDs 0 and 1 get shortened labels.

describe('Laatimisvaihe label overrides:', () => {
  // --- NEW: label overrides for version 2018 ---

  it('given version 2018 and ID 0, when applying label override, then label_fi is "Rakennuslupa"', () => {
    // given
    const entries = [
      {
        id: 0,
        'label-fi': 'Rakennuslupavaihe, uudisrakennus',
        'label-sv': 'Bygglov'
      }
    ];
    const version = 2018;

    // when
    const result = overrideLabels(version, entries);

    // then
    expect(result[0]['label-fi']).toBe('Rakennuslupa');
    expect(result[0]['label-sv']).toBe('Bygglov');
  });

  it('given version 2018 and ID 1, when applying label override, then label_fi is "Käyttöönotto"', () => {
    // given
    const entries = [
      {
        id: 1,
        'label-fi': 'Käyttöönottovaihe, uudisrakennus',
        'label-sv': 'Införandet'
      }
    ];
    const version = 2018;

    // when
    const result = overrideLabels(version, entries);

    // then
    expect(result[0]['label-fi']).toBe('Käyttöönotto');
    expect(result[0]['label-sv']).toBe('Införandet');
  });

  it('given version 2018 and ID 2, when applying label override, then labels are unchanged', () => {
    // given
    const entries = [
      {
        id: 2,
        'label-fi': 'Olemassa oleva rakennus',
        'label-sv': 'Befintlig byggnad'
      }
    ];
    const version = 2018;

    // when
    const result = overrideLabels(version, entries);

    // then
    expect(result[0]['label-fi']).toBe('Olemassa oleva rakennus');
    expect(result[0]['label-sv']).toBe('Befintlig byggnad');
  });

  // --- NEW: label overrides for version 2026 (passthrough) ---

  it('given version 2026, when applying label override, then all labels are unchanged', () => {
    // given
    const entries = [
      {
        id: 0,
        'label-fi': 'Rakennuslupavaihe, uudisrakennus',
        'label-sv': 'Bygglov'
      },
      {
        id: 1,
        'label-fi': 'Käyttöönottovaihe, uudisrakennus',
        'label-sv': 'Införandet'
      },
      {
        id: 2,
        'label-fi': 'Olemassa oleva rakennus',
        'label-sv': 'Befintlig byggnad'
      },
      {
        id: 3,
        'label-fi': 'Rakennuslupavaihe, laajamittainen perusparannus',
        'label-sv': 'Rakennuslupavaihe, laajamittainen perusparannus (sv)'
      },
      {
        id: 4,
        'label-fi': 'Käyttöönottovaihe, laajamittainen perusparannus',
        'label-sv': 'Käyttöönottovaihe, laajamittainen perusparannus (sv)'
      }
    ];
    const version = 2026;

    // when
    const result = overrideLabels(version, entries);

    // then — all labels should be passed through without changes
    entries.forEach((entry, i) => {
      expect(result[i]['label-fi']).toBe(entry['label-fi']);
      expect(result[i]['label-sv']).toBe(entry['label-sv']);
    });
  });

  // --- NEW: label override applied to a full list for version 2018 ---

  it('given version 2018 and a list of entries, when applying overrides, then only IDs 0 and 1 are overridden', () => {
    // given
    const entries = [
      {
        id: 0,
        'label-fi': 'Rakennuslupavaihe, uudisrakennus',
        'label-sv': 'Bygglov'
      },
      {
        id: 1,
        'label-fi': 'Käyttöönottovaihe, uudisrakennus',
        'label-sv': 'Införandet'
      },
      {
        id: 2,
        'label-fi': 'Olemassa oleva rakennus',
        'label-sv': 'Befintlig byggnad'
      }
    ];
    const version = 2018;

    // when
    const result = overrideLabels(version, entries);

    // then
    expect(result[0]['label-fi']).toBe('Rakennuslupa');
    expect(result[1]['label-fi']).toBe('Käyttöönotto');
    expect(result[2]['label-fi']).toBe('Olemassa oleva rakennus');
  });

  // --- Edge case: function does not mutate original entries ---

  it('given a list of entries, when applying overrides, then original entries are not mutated', () => {
    // given
    const entries = [
      {
        id: 0,
        'label-fi': 'Rakennuslupavaihe, uudisrakennus',
        'label-sv': 'Bygglov'
      }
    ];
    const originalLabel = entries[0]['label-fi'];
    const version = 2018;

    // when
    overrideLabels(version, entries);

    // then — original entry should not be modified
    expect(entries[0]['label-fi']).toBe(originalLabel);
  });
});
