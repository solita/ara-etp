const { defineConfig } = require('cypress');

module.exports = defineConfig({
  viewportHeight: 900,
  viewportWidth: 1440,
  defaultCommandTimeout: 15000,
  video: true,
  e2e: {
    setupNodeEvents(on, config) {},
    baseUrl: 'https://localhost:3009'
  }
});
