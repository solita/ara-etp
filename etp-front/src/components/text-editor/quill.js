import Quill from 'quill';
import { ImageDrop } from 'quill-image-drop-module';
import MagicUrl from 'quill-magic-url';
import * as Objects from '@Utility/objects';

Quill.register('modules/imageDrop', ImageDrop);
Quill.register('modules/magicUrl', MagicUrl);

const dispatchEvent = (name, node, editor) =>
  node.dispatchEvent(
    new CustomEvent(name, {
      bubbles: true,
      detail: {
        html: editor.innerHTML
      }
    })
  );

/**
 * Quill editor wrapper
 * @param node - the node to attach the editor to, a HTMLElement object
 * @param html - function to convert to HTML
 * @param toolbar - toolbar configuration
 * @param keyboard - keyboard configuration
 * @param id - id of the input, used to set aria-describedby attribute
 * @param required - used to set aria-required attribute
 * @param onEditorSetup - callback for when the editor is setup. This enables
 *                        the calling Svelte component to update if the inserted
 *                        value is valid or not.
 * @returns {{update: (function({html: *, _: *}): any), destroy: *}}
 */
export const quill = (
  node,
  { html, toolbar, keyboard, id, required, onEditorSetup }
) => {
  const q = new Quill(node, {
    modules: {
      imageDrop: false,
      magicUrl: true,
      toolbar,
      keyboard
    },
    placeholder: '',
    theme: 'snow' // or 'bubble'
  });

  q.setContents(q.clipboard.convert(html), 'silent');

  const root = node.parentElement;
  const editor = node.getElementsByClassName('ql-editor')[0];

  const textChange = (delta, oldDelta, source) =>
    dispatchEvent('text-change', node, editor);

  q.on('text-change', textChange);

  // add href for link action anchor elements
  // so that they are visible in focusout.relatedTarget
  const action = Objects.requireNotNil(
    root.getElementsByClassName('ql-action')[0],
    'Link tool action anchor does not found.'
  );
  action.href = '';
  const remove = Objects.requireNotNil(
    root.getElementsByClassName('ql-remove')[0],
    'Link tool remove anchor does not found.'
  );
  remove.href = '';

  const focusout = event => {
    if (!root.contains(event.relatedTarget) && event.target !== action) {
      dispatchEvent('editor-focus-out', node, editor);
    }
  };
  root.addEventListener('focusout', focusout);

  /* Here the code assumes that quill will keep making
   * the editable div with this class */
  const editorElement = node.querySelector('.ql-editor');
  if (id !== undefined) {
    editorElement.setAttribute('aria-describedby', `${id}-error-label`);
  }

  if (required !== undefined) {
    editorElement.setAttribute('aria-required', required);
  }

  if (onEditorSetup !== undefined) {
    onEditorSetup(editorElement);
  }

  return {
    update: ({ html, _ }) => q.setContents(q.clipboard.convert(html), 'silent'),
    destroy: _ => {
      q.off('text-change', textChange);
      root.removeEventListener('focusout', focusout);
    }
  };
};
