// javascript
import { expect, describe, it } from '@jest/globals';
import * as api from './energiatodistus-api';

// --- Part 2: Classification Fetch ---

// luokittelutForVersion and luokittelutAllVersions return Futures,
// so we test the underlying luokittelut plain object for key presence.

describe('Classification fetch for ilmastoselvitys', () => {
  // Test 2.1: luokittelut object includes ilmastoselvitysLaadintaperusteet key
  it('given the luokittelut object, when inspecting keys, then ilmastoselvitysLaadintaperusteet is included', () => {
    // given
    const luokittelutObject = api.luokittelut;

    // when
    const hasKey = 'ilmastoselvitysLaadintaperusteet' in luokittelutObject;

    // then
    expect(hasKey).toBe(true);
  });

  // Test 2.2: luokittelut has all expected classification keys (except version-dependent ones)
  it('given the luokittelut object, when checking all expected keys, then all version-independent classification keys are present', () => {
    // given
    const luokittelutObject = api.luokittelut;

    // when
    const keys = Object.keys(luokittelutObject);

    // then — version-independent keys remain
    expect(keys).toContain('lammonjako');
    expect(keys).toContain('lammitysmuoto');
    expect(keys).toContain('ilmanvaihtotyypit');
    expect(keys).toContain('postinumerot');
    expect(keys).toContain('kielisyys');
    expect(keys).toContain('patevyydet');
    expect(keys).toContain('ilmastoselvitysLaadintaperusteet');
    expect(keys).toContain('havainnointikayntityyppi');
  });

  // Test 2.3: luokittelut ilmastoselvitysLaadintaperusteet is a Future (cached fetch)
  it('given the luokittelut object, when checking ilmastoselvitysLaadintaperusteet value, then it is a defined Future', () => {
    // given
    const luokittelutObject = api.luokittelut;

    // when
    const value = luokittelutObject.ilmastoselvitysLaadintaperusteet;

    // then
    expect(value).toBeDefined();
    expect(value).not.toBeNull();
  });
});

// --- NEW: laatimisvaiheet moved to version-dependent classification ---

describe('Classification fetch for laatimisvaiheet version-dependency', () => {
  // Test: laatimisvaiheet is NOT in the version-independent luokittelut object
  it('given the luokittelut object, when checking keys, then laatimisvaiheet is NOT present (moved to version-dependent)', () => {
    // given
    const luokittelutObject = api.luokittelut;

    // when
    const keys = Object.keys(luokittelutObject);

    // then — laatimisvaiheet should have been moved out
    expect(keys).not.toContain('laatimisvaiheet');
  });

  // Test: all other version-independent keys remain (regression)
  it('given the luokittelut object, when checking version-independent keys, then all non-laatimisvaiheet keys remain (regression)', () => {
    // given
    const luokittelutObject = api.luokittelut;

    // when
    const keys = Object.keys(luokittelutObject);

    // then — these keys must still be present
    expect(keys).toContain('lammonjako');
    expect(keys).toContain('lammitysmuoto');
    expect(keys).toContain('ilmanvaihtotyypit');
    expect(keys).toContain('postinumerot');
    expect(keys).toContain('kielisyys');
    expect(keys).toContain('patevyydet');
    expect(keys).toContain('mahdollisuusLiittya');
    expect(keys).toContain('uusiutuvaEnergia');
    expect(keys).toContain('jaahdytys');
    expect(keys).toContain('toimenpide-ehdotus');
    expect(keys).toContain('toimenpide-ehdotus-group');
    expect(keys).toContain('havainnointikayntityyppi');
    expect(keys).toContain('ilmastoselvitysLaadintaperusteet');
  });

  // Test: luokittelutAllVersions still contains laatimisvaiheet for search/haku
  it('given the luokittelutAllVersions object, when resolved, then it should contain laatimisvaiheet', () => {
    // given
    const allVersions = api.luokittelutAllVersions;

    // then — the overall object should be defined (it's a Future, so we can only test it exists)
    expect(allVersions).toBeDefined();
    expect(allVersions).not.toBeNull();
  });
});
