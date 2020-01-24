import * as R from 'ramda';

export const fileupload = (node, update) => {
  const dragenter = _ => update(R.assoc('highlight', true));

  const dragleave = _ => update(R.assoc('highlight', false));

  const drop = event => {
    event.preventDefault();
    update(
      R.compose(
        R.assoc('highlight', false),
        R.compose(
          R.assoc('files'),
          R.path(['dataTransfer', 'files']),
          R.tap(event => event.preventDefault())
        )(event)
      )
    );
  };

  const dragover = event => event.preventDefault();

  node.addEventListener('dragenter', dragenter);
  node.addEventListener('dragleave', dragleave);
  node.addEventListener('drop', drop);
  node.addEventListener('dragover', dragover);

  return {
    destroy() {
      node.removeEventListener('dragenter');
      node.removeEventListener('dragleave');
      node.removeEventListener('drop');
      node.removeEventListener('dragover');
    }
  }
}