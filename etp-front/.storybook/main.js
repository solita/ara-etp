module.exports = {
  stories: ['../src/**/*.stories.js'],
  staticDirs: ['../assets'],
  addons: [
    '@storybook/addon-actions',
    '@storybook/addon-links',
    '@storybook/addon-a11y',
    '@storybook/addon-viewport'
  ],

  framework: {
    name: '@storybook/svelte-vite',
    options: {}
  },

  core: {
    disableTelemetry: true
  },
}
