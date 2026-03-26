/**
 * @jest-environment jsdom
 */
import { expect, describe, it, beforeAll } from '@jest/globals';
import { render, screen } from '@testing-library/svelte';
import { setupI18n } from '@Language/i18n';

import HavainnointikayntiInput from './havainnointikayntityyppi-input.svelte';

describe('HavainnointikayntiInput', () => {
  beforeAll(() => {
    setupI18n();
  });
  const mockLuokittelut = {
    havainnointikayntityypit: [
      { id: 0, 'label-fi': 'Ei valittu', 'label-sv': 'Inte vald' },
      { id: 1, 'label-fi': 'Paikan päällä', 'label-sv': 'På plats' },
      {
        id: 2,
        'label-fi': 'Etähavainnointikäynti',
        'label-sv': 'Fjärrobservation'
      }
    ]
  };

  it('renders without errors when given valid props', () => {
    // Given: valid props with havainnointikayntityypit luokittelut
    const props = {
      values: [0],
      nameprefix: 'test',
      index: 0,
      luokittelut: mockLuokittelut
    };

    // When: rendering the component
    // Then: it should render without throwing
    expect(() => render(HavainnointikayntiInput, props)).not.toThrow();
  });

  it('passes luokittelu havainnointikayntityypit to generic luokittelu-input', () => {
    // Given: valid props with havainnointikayntityypit luokittelut data
    const props = {
      values: [0],
      nameprefix: 'test',
      index: 0,
      luokittelut: mockLuokittelut
    };

    // When: rendering the component
    const { container } = render(HavainnointikayntiInput, props);

    // Then: a hidden input is present (the Select component renders one)
    const input = container.querySelector('input[name="test_value_0"]');
    expect(input).toBeInTheDocument();
  });

  it('shows correct options from luokittelut prop', () => {
    // Given: valid props with 3 havainnointikayntityypit options
    const props = {
      values: [0],
      nameprefix: 'test',
      index: 0,
      luokittelut: mockLuokittelut
    };

    // When: rendering the component
    const { container } = render(HavainnointikayntiInput, props);

    // Then: the selected value (id=0 -> "Ei valittu") is displayed
    const selectedDiv = container.querySelector('[data-cy="test_value_0"]');
    expect(selectedDiv).toBeInTheDocument();
    expect(selectedDiv.textContent.trim()).toBe('Ei valittu');
  });
});
