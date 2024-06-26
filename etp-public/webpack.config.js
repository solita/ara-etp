const { format } = require('date-fns/format');
const { CleanWebpackPlugin } = require('clean-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const BundleAnalyzerPlugin =
  require('webpack-bundle-analyzer').BundleAnalyzerPlugin;

const GenerateJsonPlugin = require('generate-json-webpack-plugin');

const path = require('path');

const mode = process.env.NODE_ENV || 'development';
const prod = mode === 'production';

module.exports = {
  entry: {
    bundle: ['./src/main.js']
  },
  resolve: {
    extensions: ['.mjs', '.js', '.svelte', '.css'],
    alias: {
      '@Component': path.resolve(__dirname, 'src/components'),
      '@Localization': path.resolve(__dirname, 'src/localization'),
      '@Router': path.resolve(__dirname, 'src/router'),
      '@Page': path.resolve(__dirname, 'src/pages'),
      '@Asset': path.resolve(__dirname, 'assets'),
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
    filename: '[name].[contenthash].js'
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
        exclude: /node_modules/,
        include: [path.resolve(__dirname, 'src'), /svelte/],
        use: ['babel-loader']
      },
      {
        test: /\.svelte$/,
        include: [path.resolve(__dirname, 'src'), /svelte/],
        use: [
          'babel-loader',
          {
            loader: 'svelte-loader',
            options: {
              emitCss: true,
              immutable: true,
              legacy: true
            }
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
      },
      {
        test: /\.(png|jpe?g|webp|tiff?)$/i,
        type: 'asset/resource',
        generator: {
          filename: '[name][ext]'
        },
        use: [
          {
            loader: 'webpack-image-resize-loader',
            options: {
              width: 400
            }
          }
        ]
      },
      {
        test: /\.svg$/,
        type: 'asset/resource',
        loader: 'svgo-loader',
        options: {
          removeTitle: true,
          convertColors: { shorthex: false },
          convertPathData: false
        }
      },
      {
        test: /\.pdf$/,
        use: [
          {
            loader: 'file-loader',
            options: {
              name: '[name].[ext]'
            }
          }
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
    new GenerateJsonPlugin('config.json', {
      showTestEnvNotification: true,
      privateSiteUrl: 'https://localhost:3000'
    }),
    new MiniCssExtractPlugin({
      filename: '[name].[contenthash].css'
    }),
    new HtmlWebpackPlugin({
      title: 'Energiatodistusrekisteri',
      template: './src/template.html',
      favicon: './assets/favicon.png'
    })
    // uncomment to see treeview of generated bundle after build
    // new BundleAnalyzerPlugin()
  ],
  devtool: prod ? false : 'source-map',
  devServer: {
    client: {
      overlay: false // Hide compiler warnings on client
    },
    port: process.env.WEBPACK_PORT || 5050,
    historyApiFallback: true,
    proxy: [
      {
        context: ['/api'],
        target: process.env.WEBPACK_PROXY_TARGET || `http://localhost:8080`,
        secure: false,
        changeOrigin: true
      }
    ]
  }
};
