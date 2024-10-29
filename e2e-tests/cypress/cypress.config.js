const { defineConfig } = require('cypress');
const pg = require('pg');

module.exports = defineConfig({
  viewportHeight: 900,
  viewportWidth: 1440,
  defaultCommandTimeout: 15000,
  video: true,
  e2e: {
    backendUrl: 'http://localhost:3444',
    publicUrl: 'http://localhost:5059',
    baseUrl: 'https://localhost:3009',
    setupNodeEvents(on, config) {
      on('task', {
        // applicationName needs to be parsable by our database audit system. For example -6@something.
        executeQuery({ query, applicationName }) {
          const client = new pg.Client({
            connectionString: `postgresql://etp_app:etp@localhost:5444/etp_dev?search_path=etp&application_name=${applicationName}`
          });

          return client
            .connect()
            .then(() => client.query(query))
            .then(result => {
              return result.rows;
            })
            .catch(err => {
              throw err;
            })
            .finally(() => {
              client.end();
            });
        }
      });
    }
  }
});
