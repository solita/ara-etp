module.exports = {
  stories: ['../src/**/*.stories.js'],
  staticDirs: ['../assets'],
  addons: [
    '@storybook/addon-actions',
    '@storybook/addon-links',
    '@storybook/addon-a11y',
    '@storybook/addon-viewport',
    '@storybook/addon-webpack5-compiler-babel'
  ],

  framework: {
    name: '@storybook/svelte-webpack5',
    options: {}
  },

  core: {
    disableTelemetry: true
  },
}
