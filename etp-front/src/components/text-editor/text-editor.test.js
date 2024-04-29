/**
 * @jest-environment jsdom
 */
import { beforeAll, expect, test } from '@jest/globals';
import { render, screen } from '@testing-library/svelte';

import TextEditor from './text-editor.svelte';
import { setupI18n } from '@Language/i18n.js';

beforeAll(() => {
  setupI18n();
});

test('TextEditor renders correctly', () => {
  render(TextEditor, {
    id: 'test',
    name: 'test',
    label: 'test',
    required: true,
    model: 'test content'
  });

  const content = screen.getByText('test content');
  expect(content).toBeInTheDocument();
});
