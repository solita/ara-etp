const fs = require('fs');
const Promise = require('promise');

const format = require('date-fns/format');

const production = process.env.NODE_ENV !== 'development';

const version = `${production ? 'build' : 'dev'}-${format(
  Date.now(),
  'yyyy-MM-dd'
)}`;

new Promise((resolve, reject) => {
  fs.writeFile(
    './public/build/version.json',
    JSON.stringify({ version }),
    'utf-8',
    err => {
      if (err) reject(err);
      resolve();
    }
  );
});
