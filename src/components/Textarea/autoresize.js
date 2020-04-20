import * as R from 'ramda';

const resize = (min, max, element) => {
  const fontSize = parseInt(
    window
      .getComputedStyle(document.body)
      .getPropertyValue('font-size')
      .match(/\d+/)[0]
  );

  element.style.height = 0;

  const newSize = R.clamp(fontSize * min, fontSize * max, element.scrollHeight);
  element.style.height = `${newSize}px`;
};

export const autoresize = (node, params) => {
  const [min, max] = params;

  const resizer = event => {
    resize(min, max, event.target);
  };

  node.addEventListener('input', resizer);

  resize(min, max, node);

  return { destroy: () => node.removeEventListener('input', resizer) };
};
