/**
 * @jest-environment jsdom
 */
import { expect, describe, it } from '@jest/globals';
import { render, screen } from '@testing-library/svelte';

import HavainnointikayntiInput from './havainnointikayntityyppi-input.svelte';

describe('HavainnointikayntiInput', () => {
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
    render(HavainnointikayntiInput, props);

    // Then: the select element should be present with options from havainnointikayntityypit
    const select = screen.getByRole('listbox');
    expect(select).toBeInTheDocument();
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
    render(HavainnointikayntiInput, props);

    // Then: all 3 options should be rendered
    const options = screen.getAllByRole('option');
    expect(options).toHaveLength(3);
  });
});
