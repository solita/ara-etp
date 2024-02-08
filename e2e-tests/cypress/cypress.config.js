const { defineConfig } = require('cypress')

module.exports = defineConfig({
  viewportHeight: 900,
  viewportWidth: 1440,
  defaultCommandTimeout: 15000,
  video: false,
  e2e: {
    setupNodeEvents(on, config) {},
    supportFile: false,
    baseUrl: 'https://localhost:3009',
  },
})
