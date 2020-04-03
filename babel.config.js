const path = require('path');

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
    '@babel/plugin-syntax-dynamic-import',
    [
      '@babel/plugin-transform-runtime',
      {
        useESModules: false
      }
    ],
    [
      'babel-plugin-module-resolver',
      {
        extensions: ['.svelte', '.js'],
        alias: {
          '@Component': path.resolve(__dirname, 'src/components'),
          '@Utility': path.resolve(__dirname, 'src/utils'),
          '@Language': path.resolve(__dirname, 'src/language'),
          '@': path.resolve(__dirname, 'src')
        }
      }
    ]
  ]
};
