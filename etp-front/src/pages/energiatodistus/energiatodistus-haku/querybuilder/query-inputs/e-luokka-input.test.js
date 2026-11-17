/**
 * @jest-environment jsdom
 */
import { expect, test } from '@jest/globals';
import { render, screen } from '@testing-library/svelte';

import ELuokkaInput from './e-luokka-input.svelte';

// --- Section 9: E-luokan hakusuodatin tests ---

test('renders 8 e-luokka options including A+ and no A0', () => {
  // given
  const props = {
    values: [[]],
    nameprefix: 'test',
    index: 0
  };

  // when
  render(ELuokkaInput, props);

  // then — 8 checkbox elements (A+, A, B, C, D, E, F, G)
  const checkboxes = screen.getAllByRole('checkbox');
  expect(checkboxes).toHaveLength(8);
  expect(checkboxes.map(cb => cb.value)).not.toContain('A0');
});

test('options are in correct order: A+, A, B, C, D, E, F, G', () => {
  // given
  const expectedOrder = ['A+', 'A', 'B', 'C', 'D', 'E', 'F', 'G'];
  const props = {
    values: [[]],
    nameprefix: 'test',
    index: 0
  };

  // when
  render(ELuokkaInput, props);

  // then
  const checkboxes = screen.getAllByRole('checkbox');
  checkboxes.forEach((checkbox, i) => {
    expect(checkbox.value).toBe(expectedOrder[i]);
  });
});

test('A+ value is bindable like other classes', () => {
  // given
  const props = {
    values: [['A+', 'A']],
    nameprefix: 'test',
    index: 0
  };

  // when
  render(ELuokkaInput, props);

  // then — A+ and A are part of the checked group
  const checkboxes = screen.getAllByRole('checkbox');
  const checkedValues = checkboxes.filter(cb => cb.checked).map(cb => cb.value);
  expect(checkedValues).toContain('A+');
  expect(checkedValues).toContain('A');
});
