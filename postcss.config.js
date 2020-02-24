const autoprefixer = require('autoprefixer')();
const purgecss = require('@fullhuman/postcss-purgecss')({
  content: ['./src/**/*.svelte', './src/index-template.html'],

  whitelistPatterns: [/svelte-/],

  defaultExtractor: content => content.match(/[A-Za-z0-9-_:/]+/g) || []
});

const production = !process.env.ROLLUP_WATCH;

module.exports = {
  plugins: [
    require('tailwindcss'),
    ...(production ? [purgecss, autoprefixer] : [])
  ]
};
