import { expect, describe, it } from '@jest/globals';

import * as empty from './empty';

describe('energiatodistus2026 oletusarvot - tayttaa-aplus/a0-vaatimukset:', () => {
  // Given: tyhjä 2026-energiatodistus
  const et2026 = empty.energiatodistus2026();

  it('perustiedot sisältää tayttaa-aplus-vaatimukset kentän arvolla false', () => {
    // When: tarkistetaan perustiedot-osion kentät
    // Then: tayttaa-aplus-vaatimukset on false
    expect(et2026.perustiedot['tayttaa-aplus-vaatimukset']).toBe(false);
  });

  it('perustiedot sisältää tayttaa-a0-vaatimukset kentän arvolla false', () => {
    // When: tarkistetaan perustiedot-osion kentät
    // Then: tayttaa-a0-vaatimukset on false
    expect(et2026.perustiedot['tayttaa-a0-vaatimukset']).toBe(false);
  });
});

describe('energiatodistus2018 ei sisällä tayttaa-aplus/a0-vaatimukset kenttiä:', () => {
  // Given: tyhjä 2018-energiatodistus
  const et2018 = empty.energiatodistus2018();

  it('perustiedot EI sisällä tayttaa-aplus-vaatimukset kenttää', () => {
    // When: tarkistetaan perustiedot-osion kentät
    // Then: kenttää ei ole
    expect(et2018.perustiedot).not.toHaveProperty('tayttaa-aplus-vaatimukset');
  });

  it('perustiedot EI sisällä tayttaa-a0-vaatimukset kenttää', () => {
    // When: tarkistetaan perustiedot-osion kentät
    // Then: kenttää ei ole
    expect(et2018.perustiedot).not.toHaveProperty('tayttaa-a0-vaatimukset');
  });
});

describe('energiatodistus2013 ei sisällä tayttaa-aplus/a0-vaatimukset kenttiä:', () => {
  // Given: tyhjä 2013-energiatodistus
  const et2013 = empty.energiatodistus2013();

  it('perustiedot EI sisällä tayttaa-aplus-vaatimukset kenttää', () => {
    // When: tarkistetaan perustiedot-osion kentät
    // Then: kenttää ei ole
    expect(et2013.perustiedot).not.toHaveProperty('tayttaa-aplus-vaatimukset');
  });

  it('perustiedot EI sisällä tayttaa-a0-vaatimukset kenttää', () => {
    // When: tarkistetaan perustiedot-osion kentät
    // Then: kenttää ei ole
    expect(et2013.perustiedot).not.toHaveProperty('tayttaa-a0-vaatimukset');
  });
});
