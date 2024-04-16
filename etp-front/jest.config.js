/**
 * For a detailed explanation regarding each configuration property, visit:
 * https://jestjs.io/docs/configuration
 */

/** @type {import('jest').Config} */
const config = {
  clearMocks: true,
  moduleFileExtensions: ['js', 'svelte', 'mjs'],
  reporters: ['default', ['jest-junit', { outputName: 'test_report.xml' }]],
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
  moduleNameMapper: {
    '^@Pages/(.*)$': '<rootDir>/src/pages/$1',
    '^@Component/(.*)$': '<rootDir>/src/components/$1',
    '^@Utility/(.*)$': '<rootDir>/src/utils/$1',
    '^@Language/(.*)$': '<rootDir>/src/language/$1',
    '^@/(.*)$': '<rootDir>/src/$1',
  },
  extensionsToTreatAsEsm: ['.svelte'],
  collectCoverageFrom: [
    'src/**/*.{js,svelte}',
    '!**/node_modules/**',
    '!**/*.stories.js'
  ]
};

module.exports = config;
