const fs = require('fs');
const Promise = require('promise');

const COGNITOCLIENTID = process.env.COGNITOCLIENTID || 'dev';

console.info('Reading config-template');
const CONFIG = require('./src/config-template');

const newConfig = { ...CONFIG, ...{ COGNITOCLIENTID } };

new Promise((resolve, reject) => {
  console.info('Writing config');
  fs.writeFile(
    './public/build/config.js',
    `window.CONFIG = ${JSON.stringify(newConfig)};`,
    'utf-8',
    err => {
      if (err) reject(err);
      resolve();
    }
  );
})
  .then(() => process.exit(0))
  .catch(err => {
    console.error(err);
    process.exit(1);
  });
