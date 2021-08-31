import Quill from 'quill';
import { ImageDrop } from 'quill-image-drop-module';
import MagicUrl from 'quill-magic-url';
import DOMPurify from 'dompurify';
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

export const quill = (node, {html, toolbar}) => {
  const q = new Quill(node, {
    modules: {
      imageDrop: false,
      magicUrl: true,
      toolbar: toolbar
    },
    placeholder: '',
    theme: 'snow' // or 'bubble'
  });

  q.clipboard.dangerouslyPasteHTML(DOMPurify.sanitize(html));

  const root = node.parentElement;
  const editor = node.getElementsByClassName('ql-editor')[0];

  const textChange = (delta, oldDelta, source) =>
    dispatchEvent('text-change', node, editor);

  q.on('text-change', textChange);

  // add href for link action anchor elements
  // so that they are visible in focusout.relatedTarget
  const action = Objects.requireNotNil(
    root.getElementsByClassName('ql-action')[0],
    "Link tool action anchor does not found.");
  action.href="";
  const remove = Objects.requireNotNil(
    root.getElementsByClassName('ql-remove')[0],
    "Link tool remove anchor does not found.");
  remove.href="";

  const focusout = event => {
    if (!root.contains(event.relatedTarget) &&
        event.target !== action) {
      dispatchEvent('editor-focus-out', node, editor);
    }
  }
  root.addEventListener('focusout', focusout);

  return {
    update: ({html, _}) => q.clipboard.dangerouslyPasteHTML(DOMPurify.sanitize(html)),
    destroy: _ => {
      q.off('text-change', textChange);
      root.removeEventListener('focusout', focusout);
    }
  };
};
