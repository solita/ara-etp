const fs = require('fs');
const Promise = require('promise');

new Promise((resolve, reject) => {
  console.info('Reading index-template');
  fs.readFile('./src/index-template.html', 'utf-8', (err, html) => {
    if (err) reject(err);
    resolve(html);
  });
})
  .then(html => {
    return new Promise((resolve, reject) => {
      console.info('Writing index');
      fs.writeFile(
        './public/index.html',
        html.replace('$jshash', '').replace('$csshash', ''),
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
