const path = require('path');

module.exports = {resolve: {alias: {
  '@Component': path.resolve(__dirname, 'src/components'),
  '@Utility': path.resolve(__dirname, 'src/utils'),
  '@Language': path.resolve(__dirname, 'src/language'),
  '@': path.resolve(__dirname, 'src')}}};