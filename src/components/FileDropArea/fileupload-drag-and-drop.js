import * as R from 'ramda';

const preventDefault = event => event.preventDefault();

export const fileupload = (node, { setFiles, toggleHighlight }) => {
  const dragenter = _ => toggleHighlight(true);

  const dragleave = _ => toggleHighlight(false);

  const drop = event => {
    toggleHighlight(false);

    R.compose(
      setFiles,
      R.path(['dataTransfer', 'files']),
      R.tap(preventDefault)
    )(event);
  };

  const dragover = preventDefault;

  node.addEventListener('dragenter', dragenter);
  node.addEventListener('dragleave', dragleave);
  node.addEventListener('drop', drop);
  node.addEventListener('dragover', dragover);

  return {
    destroy() {
      node.removeEventListener('dragenter', dragenter);
      node.removeEventListener('dragleave', dragleave);
      node.removeEventListener('drop', drop);
      node.removeEventListener('dragover', dragover);
    }
  };
};
