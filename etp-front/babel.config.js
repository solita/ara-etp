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
    ['@babel/plugin-transform-runtime', { corejs: 3 }],
    [
      'babel-plugin-module-resolver',
      {
        extensions: ['.svelte', '.js'],
        alias: {
          '@Pages': './src/pages',
          '@Component': './src/components',
          '@Utility': './src/utils',
          '@Language': './src/language',
          '@': './src'
        }
      }
    ]
  ],
  sourceType: 'unambiguous'
};
