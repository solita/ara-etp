import Quill from 'quill';
import { ImageDrop } from 'quill-image-drop-module';
import MagicUrl from 'quill-magic-url';
import DOMPurify from 'dompurify';

Quill.register('modules/imageDrop', ImageDrop);
Quill.register('modules/magicUrl', MagicUrl);

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

  const container = node.getElementsByClassName('ql-editor')[0];

  const handler = (delta, oldDelta, source) => {
    node.dispatchEvent(
      new CustomEvent('text-change', {
        bubbles: true,
        detail: {
          html: container.innerHTML
        }
      })
    );
  };

  q.on('text-change', handler);

  return {
    update: ({html, _}) => q.clipboard.dangerouslyPasteHTML(DOMPurify.sanitize(html)),
    destroy: _ => q.off('text-change', handler)
  };
};
