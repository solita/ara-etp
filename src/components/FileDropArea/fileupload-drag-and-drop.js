import * as R from 'ramda';

const preventDefault = event => event.preventDefault();

export const fileupload = (node, {update, updateUi}) => {
  const dragenter = _ => updateUi(R.assoc('highlight', true));

  const dragleave = _ => updateUi(R.assoc('highlight', false));

  const drop = event => {
    updateUi(R.assoc('highlight', false));
    update(
        R.compose(
          R.assoc('files'),
          R.path(['dataTransfer', 'files']),
          R.tap(preventDefault)
        )(event),
      );
  };

  const dragover = preventDefault;

  node.addEventListener('dragenter', dragenter);
  node.addEventListener('dragleave', dragleave);
  node.addEventListener('drop', drop);
  node.addEventListener('dragover', dragover);
};