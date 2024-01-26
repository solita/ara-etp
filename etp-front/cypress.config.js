const { defineConfig } = require('cypress')

module.exports = defineConfig({
  viewportHeight: 900,
  viewportWidth: 1440,
  video: false,
  e2e: {
    setupNodeEvents(on, config) {},
    supportFile: false,
  },
})
