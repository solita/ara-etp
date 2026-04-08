const { defineConfig } = require('cypress');
const fs = require('fs');
const path = require('path');
const {
  extractFrontendConstraints
} = require('./cypress/support/extract-frontend-schema');

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
        /**
         * Extract frontend schema constraints for a given ET version.
         * Returns an array of { property, constraint } objects.
         */
        extractFrontendSchemaConstraints(version) {
          return extractFrontendConstraints(version);
        }
      });

      return config;
    }
  }
});
