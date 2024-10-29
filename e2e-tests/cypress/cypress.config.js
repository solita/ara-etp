const { defineConfig } = require('cypress');

module.exports = defineConfig({
  viewportHeight: 900,
  viewportWidth: 1440,
  defaultCommandTimeout: 15000,
  video: true,
  e2e: {
    backendUrl: 'http://localhost:3444',
    publicUrl: 'http://localhost:5059',
    baseUrl: 'https://localhost:3009',
    setupNodeEvents(on, config) {}
  }
});
