/**
 * @jest-environment jsdom
 */

import { render, screen } from '@testing-library/svelte';

import H1 from './H1.svelte';

test('H1 renders correctly', () => {
  render(H1, { text: 'Hello World!', dataCy: 'heading' });

  const heading = screen.queryByText(/Hello/iu);

  expect(heading).toBeInTheDocument();
});
