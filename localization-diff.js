const fs = require('fs');
const path = require('path');
const R = require('ramda');

const flattenKeys = (path, obj) =>
  R.compose(
    R.flatten,
    R.reduce(
      (acc, key) => [
        ...acc,
        ...(R.type(obj[key]) === 'String'
          ? [`${path}${path.length ? '.' : ''}${key}`]
          : flattenKeys(`${path}${path.length ? '.' : ''}${key}`, obj[key]))
      ],
      []
    ),
    R.keys
  )(obj);

const diff = (obj1, obj2) => {
  const obj1Keys = flattenKeys('', obj1);
  const obj2Keys = flattenKeys('', obj2);

  return [R.difference(obj1Keys, obj2Keys), R.difference(obj2Keys, obj1Keys)];
};

Promise.all([
  new Promise((resolve, reject) =>
    fs.readFile(
      path.resolve(__dirname, 'src/language/fi.json'),
      'utf8',
      (err, data) => {
        if (err) {
          return reject(err);
        }
        return resolve(data);
      }
    )
  ).then(JSON.parse),
  new Promise((resolve, reject) =>
    fs.readFile(
      path.resolve(__dirname, 'src/language/sv.json'),
      'utf8',
      (err, data) => {
        if (err) {
          return reject(err);
        }
        return resolve(data);
      }
    )
  ).then(JSON.parse)
])
  .then(([fi, sv]) => diff(fi, sv))
  .then(([fiDiff, svDiff]) => {
    console.log('Only in fi.json', fiDiff);
    console.log('Only in sv.json', svDiff);
  });
