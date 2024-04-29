import { format } from 'date-fns/format';

import { CleanWebpackPlugin } from 'clean-webpack-plugin';
import HtmlWebpackPlugin from 'html-webpack-plugin';
import MiniCssExtractPlugin from 'mini-css-extract-plugin';
import GenerateJsonPlugin from 'generate-json-webpack-plugin';
import CopyWebpackPlugin from 'copy-webpack-plugin';
import { BundleAnalyzerPlugin } from 'webpack-bundle-analyzer';

import path, { dirname } from 'path';
import { fileURLToPath } from 'url';

const mode = process.env.NODE_ENV || 'development';
const prod = mode === 'production';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

export default {
  entry: {
    bundle: ['./src/main.js']
  },
  resolve: {
    extensions: ['.mjs', '.js', '.svelte', '.css'],
    alias: {
      '@Pages': path.resolve(__dirname, 'src/pages'),
      '@Component': path.resolve(__dirname, 'src/components'),
      '@Utility': path.resolve(__dirname, 'src/utils'),
      '@Language': path.resolve(__dirname, 'src/language'),
      '@': path.resolve(__dirname, 'src'),
      svelte: path.resolve('node_modules', 'svelte/src/runtime')
    },
    mainFields: ['svelte', 'browser', 'module', 'main'],
    // https://github.com/sveltejs/svelte-loader?tab=readme-ov-file#resolveconditionnames
    conditionNames: ['svelte', 'browser', 'import']
  },
  output: {
    path: path.resolve(__dirname, 'public'),
    publicPath: '/',
    filename: '[name].[contenthash].js',
    hashFunction: 'sha512'
  },
  optimization: {
    moduleIds: 'deterministic',
    runtimeChunk: { name: 'runtime' },
    splitChunks: {
      chunks: 'all',
      minSize: 0,
      minChunks: 1,
      automaticNameDelimiter: '_',
      cacheGroups: {
        vendors: false,
        libs: {
          test: /[\\/]node_modules[\\/]/,
          priority: -10
        },
        polyfills: {
          test: /core-js/,
          name: 'polyfills',
          priority: 10
        }
      }
    }
  },
  module: {
    rules: [
      {
        test: /\.m?js$/,
        resolve: {
          fullySpecified: false
        },
        exclude: /node_modules/,
        include: [path.resolve(__dirname, 'src'), /svelte/],
        use: ['babel-loader']
      },
      {
        test: /\.svelte$/,
        resolve: {
          fullySpecified: false
        },
        include: [path.resolve(__dirname, 'src'), /svelte/],
        use: [
          'babel-loader',
          {
            loader: 'svelte-loader',
            options: { immutable: true, legacy: true, emitCss: true }
          }
        ]
      },
      {
        test: /\.css$/,
        use: [
          /**
           * MiniCssExtractPlugin doesn't support HMR.
           * For developing, use 'style-loader' instead.
           * */
          prod ? MiniCssExtractPlugin.loader : 'style-loader',
          'css-loader',
          'postcss-loader'
        ]
      }
    ]
  },
  mode,
  plugins: [
    new CleanWebpackPlugin(),
    new GenerateJsonPlugin('version.json', {
      version: `${prod ? 'build' : 'dev'} - ${format(
        Date.now(),
        'yyyy-MM-dd-HH-mm'
      )}`
    }),
    new MiniCssExtractPlugin({
      filename: '[name].[contenthash].css'
    }),
    new HtmlWebpackPlugin({
      title: 'Ara - Energiatodistuspalvelu',
      template: './src/template.html',
      favicon: './assets/favicon.png'
    }),
    new CopyWebpackPlugin({
      patterns: [{ from: 'assets/images', to: 'images' }]
    }),
    new CopyWebpackPlugin({
      patterns: [{ from: 'assets/errorpages', to: 'errorpages' }]
    })
    // uncomment to see treeview of generated bundle after build
    // new BundleAnalyzerPlugin()
  ],
  devtool: prod ? false : 'source-map',
  devServer: {
    client: {
      overlay: false // Hide compiler warnings on client
    },
    setupMiddlewares: (middlewares, devServer) => {
      devServer.app.get('/config.json', (req, res) =>
        res.json({
          isDev: true,
          environment: 'dev',
          publicSiteUrl: 'https://localhost:3000'
        })
      );
      return middlewares;
    },
    headers: {
      'Content-Security-Policy':
        "default-src 'self';script-src 'self';connect-src 'self' localhost:53952;style-src 'self' 'unsafe-inline' fonts.googleapis.com cdn.quilljs.com;font-src 'self' fonts.gstatic.com;img-src 'self' data:"
    },
    server: 'https',
    // Using the spread syntax make property only be present in case its value is set to something.
    // If trying to spread a falsy value, no property will be created.
    ...(process.env.WEBPACK_HOST && { host: process.env.WEBPACK_HOST }),
    ...(process.env.WEBPACK_ALLOWED_HOSTS && {
      allowedHosts: [`${process.env.WEBPACK_ALLOWED_HOSTS}`]
    }),
    port: process.env.WEBPACK_PORT || 3000,
    proxy: [
      {
        context: ['/api'],
        target: process.env.WEBPACK_PROXY_TARGET || `http://localhost:8080`,
        secure: false,
        changeOrigin: true,
        xfwd: true
      }
    ]
  }
};
