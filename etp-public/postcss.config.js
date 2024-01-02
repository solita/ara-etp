const postcssPresetEnv = require('postcss-preset-env');
const cssnano = require('cssnano');

const production = process.env.NODE_ENV === 'production';

module.exports = {
  plugins: [
    require('tailwindcss'),
    postcssPresetEnv(),
    require('autoprefixer'),
    ...(production ? [cssnano('default')] : [])
  ]
};
