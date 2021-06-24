import Quill from 'quill';
import { ImageDrop } from 'quill-image-drop-module';
import MagicUrl from 'quill-magic-url';
import DOMPurify from 'dompurify';

Quill.register('modules/imageDrop', ImageDrop);
Quill.register('modules/magicUrl', MagicUrl);

export const quill = (node, html = '') => {
  const q = new Quill(node, {
    modules: {
      imageDrop: false,
      magicUrl: true,
      toolbar: [
        [{ header: [1, 2, 3, false] }],
        [{ list: 'ordered' }, { list: 'bullet' }],
        [{ script: 'sub' }, { script: 'super' }],
        ['bold', 'italic', 'underline'],
        ['link'],
        ['clean']
      ]
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

  return () => q.off('text-change', handler);
};
