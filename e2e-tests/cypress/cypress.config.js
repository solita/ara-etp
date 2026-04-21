const { defineConfig } = require('cypress');
const fs = require('fs');
const path = require('path');

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
      // Load ET2026 field definitions CSV so tests can generate
      // describe/it blocks synchronously at parse time.
      const csvPath = path.resolve(
        __dirname,
        'cypress/e2e/laatija/energiatodistus-2026-fields.csv'
      );
      try {
        config.env.ET2026_FIELDS_CSV = fs.readFileSync(csvPath, 'utf-8');
      } catch {
        // CSV not present — tests that depend on it will skip gracefully.
        config.env.ET2026_FIELDS_CSV = '';
      }
      return config;
    }
  }
});
