const fs = require('fs');
const crypto = require('crypto');
const Promise = require('promise');

const jsHash = crypto.createHash('md5');
const cssHash = crypto.createHash('md5');
const configHash = crypto.createHash('md5');

Promise.all([
  new Promise((resolve, reject) => {
    console.log('Reading HTML-template');
    fs.readFile('./src/index-template.html', 'utf-8', (err, data) => {
      if (err) reject(err);
      resolve(data);
    });
  }),
  new Promise((resolve, reject) => {
    console.log('Hashing js');
    fs.readFile('./public/build/bundle.js', (err, data) => {
      if (err) reject(err);
      resolve(jsHash.update(data).digest('base64'));
    });
  }),
  new Promise((resolve, reject) => {
    console.log('Hashing css');
    fs.readFile('./public/build/bundle.css', (err, data) => {
      if (err) reject(err);
      resolve(cssHash.update(data).digest('base64'));
    });
  }),
  new Promise((resolve, reject) => {
    console.log('Hashing configs');
    fs.readFile('./public/build/config.js', (err, data) => {
      if (err) reject(err);
      resolve(configHash.update(data).digest('base64'));
    });
  })
])
  .then(([html, jsHash, cssHash, configHash]) => {
    console.info('Writing hash-signatures to resources');
    return new Promise((resolve, reject) => {
      fs.writeFile(
        './public/index.html',
        html
          .replace('$jshash', `?hash=${jsHash}`)
          .replace('$csshash', `?hash=${cssHash}`)
          .replace('$confighash', `?hash=${configHash}`),
        'utf-8',
        err => {
          if (err) reject(err);
          resolve();
        }
      );
    });
  })
  .then(() => process.exit(0))
  .catch(err => {
    console.error(err);
    process.exit(1);
  });
