export const customClick = (node, cb) => {
  node.addEventListener('click', cb);

  return {
    destroy() {
      node.removeEventListener('click', cb);
    }
  };
};
