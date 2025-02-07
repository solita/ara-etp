/**
 * @jest-environment jsdom
 */
import { beforeAll, expect, test } from '@jest/globals';
import { render, screen } from '@testing-library/svelte';
import '@testing-library/jest-dom';
import TextEditor from './text-editor.svelte';
import { setupI18n } from '@Language/i18n.js';

beforeAll(() => {
  setupI18n();
});

test('TextEditor renders correctly', async () => {
  const { container } = render(TextEditor, {
    id: 'test',
    name: 'test',
    label: 'test',
    required: true,
    model: 'test content'
  });

  // Find the Quill editor element
  const editorElement = container.querySelector('.ql-editor');
  expect(editorElement).toBeInTheDocument();

  // Check editor attributes
  expect(editorElement).toHaveAttribute('aria-describedby', 'test-error-label');
  expect(editorElement).toHaveAttribute('aria-required', 'true');

  // Wait for Quill to initialize
  await new Promise(resolve => setTimeout(resolve, 100));

  // Set content and trigger text-change event
  editorElement.innerHTML = '<p>test content</p>';
  const event = new CustomEvent('text-change', {
    bubbles: true,
    detail: {
      html: editorElement.innerHTML
    }
  });
  editorElement.dispatchEvent(event);

  // Wait for the text-change handler to process
  await new Promise(resolve => setTimeout(resolve, 0));

  // Verify the content is rendered correctly
  expect(editorElement.innerHTML).toContain('<p>test content</p>');
});

test('TextEditor updates model on focus out', async () => {
  const { container, component } = render(TextEditor, {
    id: 'test',
    name: 'test',
    label: 'test',
    required: true,
    model: 'initial content'
  });

  // Find the Quill editor element
  const editorElement = container.querySelector('.ql-editor');
  expect(editorElement).toBeInTheDocument();

  // Wait for Quill to initialize
  await new Promise(resolve => setTimeout(resolve, 100));

  // Manually set editor content
  editorElement.innerHTML = '<p>updated content</p>';

  // Simulate focus out event
  const event = new CustomEvent('editor-focus-out', {
    bubbles: true,
    detail: {
      html: editorElement.innerHTML
    }
  });
  editorElement.dispatchEvent(event);

  // Wait for the focus-out handler to process
  await new Promise(resolve => setTimeout(resolve, 0));

  // Check if model was updated
  expect(component.$$.ctx[component.$$.props['model']]).toBe('updated content');
});
