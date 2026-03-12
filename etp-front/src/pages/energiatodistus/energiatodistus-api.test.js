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

  // Test 2.2: luokittelut has all expected classification keys
  it('given the luokittelut object, when checking all expected keys, then all classification keys are present', () => {
    // given
    const luokittelutObject = api.luokittelut;

    // when
    const keys = Object.keys(luokittelutObject);

    // then — should include the new ilmastoselvitysLaadintaperusteet alongside existing keys
    expect(keys).toContain('lammonjako');
    expect(keys).toContain('lammitysmuoto');
    expect(keys).toContain('ilmanvaihtotyypit');
    expect(keys).toContain('postinumerot');
    expect(keys).toContain('kielisyys');
    expect(keys).toContain('laatimisvaiheet');
    expect(keys).toContain('patevyydet');
    expect(keys).toContain('ilmastoselvitysLaadintaperusteet');
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
