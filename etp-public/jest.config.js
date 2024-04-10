/**
 * For a detailed explanation regarding each configuration property, visit:
 * https://jestjs.io/docs/configuration
 */

/** @type {import('jest').Config} */
const config = {
  clearMocks: true,
  moduleFileExtensions: ['js', 'svelte', 'mjs'],
  setupFilesAfterEnv: ['<rootDir>/jest_setup.js'],
  testMatch: [
    '**/__tests__/**/*.[jt]s?(x)',
    '**/?(*[._])+(spec|test).[tj]s?(x)',
    '**/?(*[._])+(spec|test).mjs'
  ],
  transform: {
    '\\.[jt]s?$': 'babel-jest',
    '^.+\\.svelte$': ['svelte-jester', { preprocess: true }]
  },
  extensionsToTreatAsEsm: ['.svelte'],
  collectCoverageFrom: [
    'src/**/*.{js,svelte}',
    '!**/node_modules/**',
    '!**/*.stories.js'
  ]
};

module.exports = config;
