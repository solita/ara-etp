export const deserialize = el => {
  const fd = new FormData(el);
  const result = {};
  fd.forEach((value, key) => (result[key] = value));

  return result;
};
