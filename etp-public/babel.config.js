module.exports = {
  presets: [
    [
      '@babel/preset-env',
      {
        useBuiltIns: 'usage',
        corejs: 3
      }
    ]
  ],
  plugins: [
    ['@babel/plugin-transform-runtime', { corejs: 3, useESModules: false }],
    [
      'babel-plugin-module-resolver',
      {
        extensions: ['.svelte', '.js'],
        alias: {
          '@Component': './src/components',
          '@Localization': './src/localization',
          '@Router': './src/router',
          '@Page': './src/pages',
          '@Asset': './assets',
          '@': './src'
        }
      }
    ]
  ],
  sourceType: 'unambiguous'
};
