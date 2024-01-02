/**
 * @module DragDrop
 */
import * as R from 'ramda';

const preventDefault = event => event.preventDefault();

/**
 * @typedef {Object} DragDropHooks
 * @property {Function} dragStart
 * @property {Function} dragEnd
 * @property {Function} dragEnter
 * @property {Function} dragLeave
 * @property {Function} drop
 */

/**
 * @sig HTMLElement -> DragDropHooks -> Function
 * @description Svelte use: directive for attaching drag&drop functionality to given HTMLElement. Returns a function to call while unmounting the element.
 */
export const dragdrop = (
  node,
  { dragStart, dragEnd, dragEnter, dragLeave, drop }
) => {
  const dragover = preventDefault;

  // dragged element events
  node.addEventListener('dragstart', dragStart);
  node.addEventListener('dragend', dragEnd);

  // target element events
  node.addEventListener('dragenter', dragEnter);
  node.addEventListener('dragleave', dragLeave);
  node.addEventListener('drop', drop);
  node.addEventListener('dragover', dragover);

  return {
    destroy() {
      node.removeEventListener('dragstart', dragStart);
      node.removeEventListener('dragend', dragEnd);

      node.removeEventListener('dragenter', dragEnter);
      node.removeEventListener('dragleave', dragLeave);
      node.removeEventListener('dragover', dragover); //preventDefault
      node.removeEventListener('drop', drop);
    }
  };
};
