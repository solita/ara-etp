const fs = require('fs');
const path = require('path');
const R = require('ramda');
const prettier = require('prettier');

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

const diff = (fi, sv) => {
  const fiKeys = flattenKeys('', fi);
  const svKeys = flattenKeys('', sv);

  return [R.difference(fiKeys, svKeys), R.difference(svKeys, fiKeys)];
};

const cleanSv = R.curry((svDiff, sv) =>
  R.reduce(
    (acc, key) => R.compose(R.dissocPath(R.__, acc), R.split('.'))(key),
    sv,
    svDiff
  )
);

const fillFromFi = R.curry((fi, fiDiff, sv) =>
  R.reduce(
    (acc, key) =>
      R.compose(
        R.converge(R.assocPath(R.__, R.__, acc), [
          R.identity,
          R.compose(R.concat(R.__, ' SV?'), R.path(R.__, fi))
        ]),
        R.split('.')
      )(key),
    sv,
    fiDiff
  )
);

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
  .then(([fi, sv]) => {
    const [fiDiff, svDiff] = diff(fi, sv);
    return R.compose(fillFromFi(fi, fiDiff), cleanSv(svDiff))(sv);
  })
  .then(sv => {
    return new Promise((resolve, reject) =>
      fs.writeFile(
        path.resolve(__dirname, 'src/language/sv.json'),
        prettier.format(JSON.stringify(sv), { parser: 'json' }),
        err => {
          if (err) {
            return reject(err);
          }
          return resolve('ok');
        }
      )
    );
  });
