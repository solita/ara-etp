/**
 * @jest-environment jsdom
 */

import { beforeAll, describe, expect, it } from '@jest/globals';
import { fireEvent, render } from '@testing-library/svelte';
import * as Maybe from '@Utility/maybe-utils';
import * as empty from '@Pages/energiatodistus/empty';
import * as schema from '@Pages/energiatodistus/schema';
import { setupI18n } from '@Language/i18n';

import PppPerustiedot from './ppp-perustiedot.svelte';

beforeAll(() => {
  localStorage.setItem('locale', 'fi-FI');
  setupI18n();
});

const renderPppPerustiedot = a0MerkintaId => {
  const perusparannuspassi = empty.perusparannuspassi(442);
  perusparannuspassi['passin-perustiedot']['a0-merkinta-id'] =
    Maybe.Some(a0MerkintaId);

  return render(PppPerustiedot, {
    perusparannuspassi,
    schema: schema.perusparannuspassi,
    a0Merkinnat: [{ id: 0 }, { id: 1 }, { id: 2 }, { id: 3 }]
  });
};

describe('PPP A0 marking', () => {
  it('offers Ei and A0_korjattu but excludes A0 and A0_MPR', async () => {
    const { container } = renderPppPerustiedot(1);
    const selectButton = container.querySelector(
      '[id="passin-perustiedot.a0-merkinta-id"]'
    );

    expect(selectButton.textContent).toContain('A0, Rakennus täyttää');
    expect(selectButton.getAttribute('tabindex')).toBe('0');
    await fireEvent.click(selectButton);

    const options = Array.from(container.querySelectorAll('li.dropdownitem'));
    expect(options.map(option => option.textContent.trim())).toEqual([
      'Ei, Rakennus ei täytä päästöttömälle rakennukselle asetettuja vaatimuksia',
      'A0korjattu, Rakennus täyttää korjatun päästöttömän rakennuksen vaatimukset'
    ]);
  });

  it('displays an existing basic A0 value even though it is not selectable', () => {
    const { container } = renderPppPerustiedot(1);
    const selectButton = container.querySelector(
      '[id="passin-perustiedot.a0-merkinta-id"]'
    );

    expect(selectButton.textContent).toContain(
      'A0, Rakennus täyttää uuden päästöttömän rakennuksen vaatimukset'
    );
  });

  it('uses the shared subscript formatting for A0_korjattu', async () => {
    const { container } = renderPppPerustiedot(2);
    const selectButton = container.querySelector(
      '[id="passin-perustiedot.a0-merkinta-id"]'
    );

    expect(selectButton.querySelector('sub')?.textContent).toBe('korjattu');
    await fireEvent.click(selectButton);

    const correctedOption = container.querySelectorAll('li.dropdownitem')[1];
    expect(correctedOption.querySelector('sub')?.textContent).toBe('korjattu');
  });
});
