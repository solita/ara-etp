import fs from 'fs';
import svelte from 'rollup-plugin-svelte';
import resolve from '@rollup/plugin-node-resolve';
import commonjs from '@rollup/plugin-commonjs';
import livereload from 'rollup-plugin-livereload';
import { terser } from 'rollup-plugin-terser';
import json from '@rollup/plugin-json';
import sveltePreprocess from 'svelte-preprocess';
import babel from 'rollup-plugin-babel';
import cleaner from 'rollup-plugin-cleaner';
import alias from '@rollup/plugin-alias';

import path from 'path';

const production = !process.env.ROLLUP_WATCH;

export default {
  input: 'src/main.js',
  output: {
    sourcemap: !production,
    format: 'iife',
    name: 'app',
    file: 'public/build/bundle.js'
  },
  moduleContext: id => {
    // In order to match native module behaviour, Rollup sets `this`
    // as `undefined` at the top level of modules. Rollup also outputs
    // a warning if a module tries to access `this` at the top level.
    // The following modules use `this` at the top level and expect it
    // to be the global `window` object, so we tell Rollup to set
    // `this = window` for these modules.
    const thisAsWindowForModules = [
      'node_modules/intl-messageformat/lib/core.js',
      'node_modules/intl-messageformat/lib/compiler.js',
      'node_modules/intl-messageformat/lib/formatters.js',
      'node_modules/intl-format-cache/lib/index.js',
      'node_modules/intl-messageformat-parser/lib/normalize.js',
      'node_modules/intl-messageformat-parser/lib/skeleton.js',
      'node_modules/intl-messageformat-parser/lib/parser.js'
    ];

    if (
      thisAsWindowForModules.some(id_ =>
        id
          .trimRight()
          .replace(/\\/g, '/')
          .endsWith(id_)
      )
    ) {
      return 'window';
    }
  },
  plugins: [
    production &&
      cleaner({
        targets: ['./public/build/']
      }),
    json(),
    svelte({
      // enable run-time checks when not in production
      dev: !production,
      // promise to never mutate objects
      immutable: true,
      // we'll extract any component CSS out into
      // a separate file — better for performance
      css: css => {
        css.write('./public/build/bundle.css', !production);
      },
      preprocess: sveltePreprocess({
        postcss: true
      })
    }),

    alias({
      entries: {
        '@Component': path.resolve(__dirname, 'src/components'),
        '@Utility': path.resolve(__dirname, 'src/utils'),
        '@Language': path.resolve(__dirname, 'src/language'),
        '@': path.resolve(__dirname, 'src')
      }
    }),

    // If you have external dependencies installed from
    // npm, you'll most likely need these plugins. In
    // some cases you'll need additional configuration —
    // consult the documentation for details:
    // https://github.com/rollup/rollup-plugin-commonjs
    resolve({
      extensions: ['.svelte', '.js'],
      browser: true,
      dedupe: importee =>
        importee === 'svelte' || importee.startsWith('svelte/')
    }),
    commonjs(),

    production &&
      babel({
        runtimeHelpers: true,
        extensions: ['.js', '.mjs', '.html', '.svelte'],
        include: [
          'src/**',
          'node_modules/svelte/**',
          'node_modules/svelte-i18n/**',
          'node_modules/svelte-spa-router/**'
        ],
        ...require('./babel.config.js')
      }),

    // In dev mode, call `npm run start` once
    // the bundle has been generated
    !production && serve(),

    // Watch the `public` directory and refresh the
    // browser on changes when not in production
    !production &&
      livereload({
        watch: 'public',
        https: {
          key: fs.readFileSync('./keys/localhost.key'),
          cert: fs.readFileSync('./keys/localhost.crt')
        }
      }),

    // If we're building for production (npm run build
    // instead of npm run dev), minify
    production && terser()
  ],
  watch: {
    clearScreen: false
  }
};

function serve() {
  let started = false;

  return {
    writeBundle() {
      if (!started) {
        started = true;

        require('child_process').spawn('npm', ['run', 'start', '--', '--dev'], {
          stdio: ['ignore', 'inherit', 'inherit'],
          shell: true
        });

        require('child_process').spawn('npm', ['run', 'proxy'], {
          stdio: ['ignore', 'inherit', 'inherit'],
          shell: true
        });
      }
    }
  };
}
