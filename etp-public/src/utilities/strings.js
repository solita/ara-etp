export const hashCode = txt => {
  let hash = 0;

  for (let i = 0; i < txt.length; i++) {
    const ccode = txt.charCodeAt(i);
    hash = (hash << 5) - hash + ccode;
    hash = hash & hash; // Convert to 32bit integer
  }

  return hash;
};
