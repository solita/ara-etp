const { defineConfig } = require('cypress');
const pg = require('pg');

module.exports = defineConfig({
  viewportHeight: 900,
  viewportWidth: 1440,
  defaultCommandTimeout: 15000,
  video: true,
  e2e: {
    baseUrl: 'https://localhost:3009',
    setupNodeEvents(on, config) {
      on('task', {
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
